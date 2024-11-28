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
import org.apache.commons.compress.archivers.zip.ZipFile
import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{Strings, SystemInfo, Throwables}
import org.beangle.data.dao.OqlBuilder
import org.beangle.doc.excel.schema.ExcelSchema
import org.beangle.doc.transfer.importer.ImportSetting
import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.view.{Status, Stream, View}
import org.beangle.webmvc.support.action.{ExportSupport, ImportSupport, RestfulAction}
import org.openurp.base.model.Project
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{CopyCheck, Writer}
import org.openurp.degree.thesis.service.ThesisPlanService
import org.openurp.degree.thesis.web.helper.CopyCheckImportListener
import org.openurp.starter.web.support.ProjectSupport

import java.io.*
import scala.jdk.javaapi.CollectionConverters.asScala

class CopyCheckAction extends RestfulAction[CopyCheck], ImportSupport[CopyCheck], ExportSupport[CopyCheck], ProjectSupport {

  var thesisPlanService: ThesisPlanService = _

  override protected def indexSetting(): Unit = {
    given project: Project = getProject

    val departs = getDeparts
    put("departs", departs)

    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateOn desc")
    put("seasons", entityDao.search(gQuery))
  }

  protected override def configImport(setting: ImportSetting): Unit = {
    val season = thesisPlanService.getPlan().get.season
    setting.listeners = List(new CopyCheckImportListener(season, entityDao))
  }

  @response
  def downloadTemplate(): Any = {
    val schema = new ExcelSchema()
    val sheet = schema.createScheet("数据模板")
    sheet.title("论文结果检测模板")
    sheet.remark("特别说明：\n1、不可改变本表格的行列结构以及批注，否则将会导入失败！\n2、必须按照规格说明的格式填写。\n3、可以多次导入，重复的信息会被新数据更新覆盖。\n4、保存的excel文件名称可以自定。")
    sheet.add("学号", "writer.std.code").length(20).required().remark("≤20位")
    sheet.add("总字数", "copyCheck.wordCount").integer()
    sheet.add("去除引用文献复制比", "copyCheck.copyRatio").decimal("0.000%")
    sheet.add("检测日期", "copyCheck.checkOn").date().required()
    sheet.add("是否通过", "copyCheck.passed").bool().required()
    sheet.add("是否为复检结果", "copyCheck.recheck").bool().required()
    val os = new ByteArrayOutputStream()
    schema.generate(os)
    Stream(new ByteArrayInputStream(os.toByteArray), MediaTypes.ApplicationXlsx, "检测结果模板.xlsx")
  }

  def uploadReportForm(): View = {
    forward()
  }

  def uploadReport(): View = {
    getAll("zipfile", classOf[Part]) foreach { zipFile =>
      val tmpFile = new File(SystemInfo.tmpDir + "/report" + System.currentTimeMillis())
      IOs.copy(zipFile.getInputStream, new FileOutputStream(tmpFile))
      processZip(tmpFile, "GBK")
    }
    redirect("search", "info.save.success")
  }

  private def processZip(zipfile: File, encoding: String): Int = {
    val file: ZipFile = if (null == encoding) new ZipFile(zipfile)
    else new ZipFile(zipfile, encoding)
    var i = 0
    try {
      val en = file.getEntries()
      asScala(en) foreach { ze =>
        i = i + 1
        if (!ze.isDirectory) {
          val thesisName = if (ze.getName.contains("/")) Strings.substringAfterLast(ze.getName, "/") else ze.getName
          if (thesisName.indexOf(".") < 1) {
            logger.warn(thesisName + " format is error")
          } else {
            findCopyCheck(thesisName) match {
              case Some(check) =>
                val target = new File(Ems.home + EmsApp.path).getParent + "/report/" + thesisName
                val targetFile = new File(target)
                targetFile.getParentFile.mkdirs()
                IOs.copy(file.getInputStream(ze), new FileOutputStream(target))
                check.report = Some(target)
                entityDao.saveOrUpdate(check)
              case None => logger.warn("Cannot find user info of " + thesisName)
            }
          }
        }
      }
      file.close()
    } catch {
      case e: IOException => Throwables.propagate(e)
    }
    i
  }

  @mapping("report/{id}")
  def report(@param("id") id: String): View = {
    entityDao.find(classOf[CopyCheck], id.toLong) match {
      case Some(check) =>
        check.report match {
          case Some(r) => Stream(new File(r))
          case None => Status.NotFound
        }
      case None => Status.NotFound
    }
  }

  private def findCopyCheck(code: String): Option[CopyCheck] = {
    var idx1 = code.indexOf(' ')
    var idx2 = code.indexOf('+')
    if idx1 == -1 then idx1 = code.length
    if idx2 == -1 then idx2 = code.length

    val idx = Math.min(idx1, idx2)
    var usercode = code.substring(0, idx)
    if (usercode.contains(".")) {
      usercode = Strings.substringBefore(usercode, ".")
    }
    val writers = entityDao.findBy(classOf[Writer], "std.code", usercode)
    if (writers.isEmpty) {
      println(s"cannot find writer :${usercode}")
      None
    } else {
      val checks = entityDao.findBy(classOf[CopyCheck], "writer", writers.head)
      checks.headOption
    }
  }
}
