/*
 * Copyright (C) 2014, The OpenURP Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openurp.degree.thesis.web.action.admin

import jakarta.servlet.http.Part
import org.beangle.commons.collection.Collections
import org.beangle.commons.file.zip.Zipper
import org.beangle.commons.io.{Files, IOs}
import org.beangle.commons.lang.{Strings, SystemInfo}
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.EmsApp
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.view.{Stream, View}
import org.beangle.webmvc.support.action.RestfulAction
import org.openurp.base.model.Project
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.web.helper.SignatureHelper
import org.openurp.starter.web.support.ProjectSupport

import java.io.{ByteArrayInputStream, File, FileOutputStream}

/** 签名信息
 */
class SignatureAction extends RestfulAction[Signature], ProjectSupport {

  var businessLogger: WebBusinessLogger = _

  override protected def indexSetting(): Unit = {
    given project: Project = getProject

    put("departs", getDeparts)
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateIn desc")
    put("seasons", entityDao.search(gQuery))
  }

  def init(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("signature.writer.season"))
    val q = OqlBuilder.from(classOf[ThesisPaper], "paper")
    q.where("paper.writer.season=:season", season)
    q.where(s"not exists(from ${classOf[Signature].getName} a where a.writer=paper.writer)")
    val papers = entityDao.search(q)
    val archives = papers.map(x => new Signature(x.writer))
    entityDao.saveOrUpdate(archives)
    redirect("search", "初始化成功")
  }

  override protected def getQueryBuilder: OqlBuilder[Signature] = {
    given project: Project = getProject

    val query = super.getQueryBuilder
    query.where("signature.writer.std.state.department in(:departs)", getDeparts)
    getBoolean("writerSigned") foreach { signed =>
      if signed then query.where("signature.writerUrl is not null")
      else query.where("signature.writerUrl is null")
    }
    getBoolean("advisorSigned") foreach { signed =>
      if signed then query.where("signature.advisorUrl is not null")
      else query.where("signature.advisorUrl is null")
    }
    query
  }

  @mapping(value = "{id}")
  override def info(@param("id") id: String): View = {
    val signature = entityDao.get(classOf[Signature], id.toLong)
    signature.writerUrl foreach { url =>
      put("writer_signature", SignatureHelper.readBase64(url))
    }
    signature.advisorUrl foreach { url =>
      put("advisor_signature", SignatureHelper.readBase64(url))
    }
    put("signature", signature)
    forward()
  }

  /** 上传指导教师签名图片(png,jpg,jpeg)
   *
   * @return
   */
  def uploadAdvisorSignature(): View = {
    val advisor = entityDao.get(classOf[Advisor], getLongId("advisor"))
    val season = entityDao.get(classOf[GraduateSeason], getLongId("signature.writer.season"))
    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.advisor=:advisor", advisor)
    query.where("writer.thesisTitle is not null")
    query.where("writer.season=:season", season)
    query.orderBy("writer.std.code")
    val writers = entityDao.search(query)

    val parts = getAll("signature_file", classOf[Part])
    if (parts.nonEmpty && parts.head.getSize > 0) {
      val blob = EmsApp.getBlobRepository(true)
      val first = entityDao.findBy(classOf[Signature], "writer", writers).find(_.advisorUrl.nonEmpty)
      first foreach { s =>
        s.advisorUrl foreach { url => blob.remove(url) }
      }
      val part = parts.head

      val code = SignatureHelper.toBase64(part.getInputStream, part.getSubmittedFileName)
      val ext = Strings.substringAfterLast(part.getSubmittedFileName, ".")
      val sign = blob.upload("/" + season.id.toString + s"/signature/",
        new ByteArrayInputStream(code.getBytes), advisor.code + s".${ext}.txt", advisor.code + " " + advisor.name)

      for (writer <- writers) {
        val signature = entityDao.findBy(classOf[Signature], "writer", writer).headOption.getOrElse(new Signature(writer))
        signature.advisorUrl = Some(sign.filePath)
        entityDao.saveOrUpdate(signature)
      }
      businessLogger.info(s"上传了${advisor.name}的签名图片", advisor.id, Map.empty)
    }
    redirect("search", "上传成功")
  }

  def download(): View = {
    val signatures = entityDao.find(classOf[Signature], getLongIds("signature"))
    val blob = EmsApp.getBlobRepository(true)
    val advisorSignatures = Collections.newMap[Advisor, String]
    signatures foreach { s =>
      if s.advisorUrl.nonEmpty then
        advisorSignatures.put(s.writer.advisor.get, s.advisorUrl.get)
    }
    val docRoot = new File(SystemInfo.tmpDir + "/signatures" + System.currentTimeMillis())
    docRoot.mkdirs()
    val innerFiles = Collections.newBuffer[File]
    advisorSignatures foreach { sig =>
      blob.url(sig._2) foreach { url =>
        val advisor = sig._1
        val fileName = advisor.code + "_" + advisor.name + ".png"
        val localFile = new File(docRoot.getAbsolutePath + Files./ + fileName)
        val bytes = SignatureHelper.readBase64toBytes(sig._2)
        IOs.copy(new ByteArrayInputStream(bytes), new FileOutputStream(localFile))
        if (localFile.exists()) innerFiles.addOne(localFile)
      }
    }
    val zipFile = new File(SystemInfo.tmpDir + Files./ + s"签名${innerFiles.size}人.zip")
    Zipper.zip(docRoot, innerFiles, zipFile, "utf-8")
    Files.travel(docRoot, f => f.delete())
    docRoot.delete()
    Stream(zipFile).cleanup { () => zipFile.delete() }
  }

  override protected def simpleEntityName: String = "signature"
}
