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
import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.collection.Collections
import org.beangle.commons.file.zip.Zipper
import org.beangle.commons.io.{Files, IOs}
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.EmsApp
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.view.{Status, Stream, View}
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}
import org.beangle.webmvc.support.helper.QueryHelper
import org.openurp.base.model.{AuditStatus, Department, Project}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{Writer, *}
import org.openurp.degree.thesis.service.DeferService
import org.openurp.degree.thesis.service.doc.ThesisDocGenerator
import org.openurp.degree.thesis.web.helper.PaperDownloadHelper
import org.openurp.starter.web.support.ProjectSupport

import java.io.*
import java.time.Instant

class PaperAction extends RestfulAction[ThesisPaper], ProjectSupport, ExportSupport[ThesisPaper] {
  val stageDirNames = Map(Stage.Proposal -> "proposal", Stage.OralDefense -> "oralDefense")
  var deferService: DeferService = _
  var businessLogger: WebBusinessLogger = _

  override def index(): View = {
    given project: Project = getProject

    put("departs", getDeparts)
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateOn desc")
    put("seasons", entityDao.search(gQuery))
    forward()
  }

  override protected def getQueryBuilder: OqlBuilder[ThesisPaper] = {
    given project: Project = getProject

    val query = super.getQueryBuilder
    QueryHelper.dateBetween(query, null, "submitAt", "uploadBeginOn", "uploadEndOn")
    getBoolean("needRecheck") foreach { needRecheck =>
      val a = if needRecheck then "not" else ""
      query.where(a + " exists(from " + classOf[CopyCheck].getName + " c where c.writer=thesisPaper.writer and c.passed=true)")
    }
    query.where("thesisPaper.writer.std.state.department in(:departs)", getDeparts)
    query
  }

  def missing(): View = {
    val seasonId = getLong("thesisPaper.writer.season.id").get
    var departId: String = ""
    getInt("thesisPaper.writer.std.state.department.id") match {
      case Some(id) => departId = id.toString
      case None =>
    }
    redirect(to(classOf[WriterAction], "absence", s"&absence=paper&orderBy=writer.std.code&writer.season.id=${seasonId}&writer.std.state.department.id=${departId}"), null)
  }

  override def search(): View = {
    val papers = entityDao.search(getQueryBuilder)
    put("thesisPapers", papers)
    forward()
  }

  def updateAdvisorPassed(): View = {
    val papers = entityDao.find(classOf[ThesisPaper], getLongIds("thesisPaper"))
    val advisorPassed = getBoolean("passed", true)
    papers.foreach(_.advisorPassed = Some(advisorPassed))
    entityDao.saveOrUpdate(papers)
    redirect("search", "设置成功")
  }

  /** 下载封面
   *
   * @return
   */
  def cover(): View = {
    val papers = entityDao.find(classOf[ThesisPaper], getLongIds("thesisPaper"))
    val season = papers.head.writer.season
    val depart = papers.head.writer.department
    val dir = ThesisDocGenerator.getDepartFolder(season.id, depart.id, "cover")
    papers foreach { paper =>
      val writer = paper.writer
      val is = ThesisDocGenerator.genCover(writer)
      val file = new FileOutputStream(new File(dir + Files./ + s"${writer.std.code}论文封面.docx"))
      IOs.copy(is, file)
      IOs.close(file)
    }
    val targetZip = ThesisDocGenerator.getDepartZipFile(season.id, depart.id, "cover")
    Zipper.zip(new File(dir), targetZip)
    val fileName = season.name + "_" + depart.name + " 封面.zip"
    Stream(targetZip, MediaTypes.ApplicationZip, fileName)
  }

  /** 下载选定的论文
   */
  def doc(): View = {
    val papers = entityDao.find(classOf[ThesisPaper], getLongIds("thesisPaper"))
    if (papers.size == 1) {
      val path = EmsApp.getBlobRepository(true).url(papers.head.filePath)
      val response = ActionContext.current.response
      response.sendRedirect(path.get.toString)
      null
    } else {
      val season = papers.head.writer.season
      val depart = papers.head.writer.department
      val dir = new File(ThesisDocGenerator.getDepartFolder(season.id, depart.id, "tmp"))

      PaperDownloadHelper.download(dir, papers)
      val targetZip = ThesisDocGenerator.getDepartZipFile(season.id, depart.id, "tmp")
      Zipper.zip(dir, targetZip)
      val fileName = season.name + "_" + papers.head.writer.code + s"等${papers.size}篇论文.zip"
      Stream(targetZip, MediaTypes.ApplicationZip, fileName)
    }
  }

  /** 统计各个学院的论文数量
   *
   * @return
   */
  def stat(): View = {
    given project: Project = getProject

    val seasonId = getLong("thesisPaper.writer.season.id").get
    val query = OqlBuilder.from[Array[Any]](classOf[Writer].getName, "writer")
    query.where("writer.season.id=:seasonId", seasonId)
    query.where("writer.std.state.department in(:departs)", getDeparts)
    query.select("writer.std.state.department.id,writer.std.state.department.code,writer.std.state.department.name,count(*)")
    query.groupBy("writer.std.state.department.id,writer.std.state.department.code,writer.std.state.department.name")
    query.orderBy("writer.std.state.department.code")
    val writers = entityDao.search(query)

    val query2 = OqlBuilder.from[Array[Any]](classOf[ThesisPaper].getName, "paper")
    query2.where("paper.writer.season.id=:seasonId", seasonId)
    query2.where("paper.writer.std.state.department in(:departs)", getDeparts)
    query2.where("paper.finalized=true and paper.advisorPassed=true")
    query2.select("paper.writer.std.state.department.id,count(*)")
    query2.groupBy("paper.writer.std.state.department.id,paper.writer.std.state.department.name")
    val papers = entityDao.search(query2).map(x => (x(0), x(1))).toMap

    val zipTimes = Collections.newMap[Int, java.util.Date]
    papers foreach { p =>
      val departId = p(0).asInstanceOf[Number].intValue()
      val f = ThesisDocGenerator.getDepartZipFile(seasonId, departId, "paper")
      if (f.exists()) {
        zipTimes.put(departId, new java.util.Date(f.lastModified()))
      }
    }
    put("writers", writers)
    put("papers", papers)
    put("zipTimes", zipTimes)
    put("seasonId", seasonId)
    forward()
  }

  def genZip(): View = {
    val seasonId = getLongId("season")
    val departmentId = getIntId("department")
    val query2 = OqlBuilder.from(classOf[ThesisPaper], "paper")
    query2.where("paper.writer.season.id=:seasonId", seasonId)
    query2.where("paper.writer.std.state.department.id=:departId", departmentId)
    query2.where("paper.finalized=true and paper.advisorPassed=true")
    val papers = entityDao.search(query2)
    val dir = ThesisDocGenerator.getDepartFolder(seasonId, departmentId, "paper")
    PaperDownloadHelper.download(new File(dir), papers)
    val targetZip = ThesisDocGenerator.getDepartZipFile(seasonId, departmentId, "paper")
    Zipper.zip(new File(dir), targetZip)
    redirect("stat", "thesisPaper.writer.season.id=" + seasonId, "生成成功")
  }

  def downloadZip(): View = {
    val seasonId = getLongId("season")
    val departmentId = getIntId("department")
    val file = ThesisDocGenerator.getDepartZipFile(seasonId, departmentId, "paper")
    val depart = entityDao.get(classOf[Department], departmentId)
    val season = entityDao.get(classOf[GraduateSeason], seasonId)
    val fileName = season.name + "_" + depart.name + " 论文.zip"
    Stream(file, MediaTypes.ApplicationZip, fileName)
  }

  /** 更新上传
   *
   */
  def uploadForm(): View = {
    val writer =
      getLong("thesisPaper.id") match {
        case None =>
          getLong("writer.id") match {
            case None => null.asInstanceOf[Writer]
            case Some(writerId) => entityDao.get(classOf[Writer], writerId)
          }
        case Some(paperId) =>
          val paper = entityDao.get(classOf[ThesisPaper], paperId)
          put("thesisPaper", paper)
          paper.writer
      }
    if (null == writer) Status.NotFound
    else
      put("writer", writer)
      forward()
  }

  def upload(): View = {
    val writer = entityDao.get(classOf[Writer], getLongId("writer"))
    val paper = entityDao.first(OqlBuilder.from(classOf[ThesisPaper], "f").where("f.writer=:writer", writer))
      .getOrElse(new ThesisPaper)
    val title = get("thesisPaper.title", "--")
    populate(paper, "thesisPaper")
    if (paper.persisted) {
      paper.writer = writer
    }
    paper.excellent = getBoolean("thesisPaper.excellent").getOrElse(false)
    writer.thesisTitle = Some(title)
    val parts = getAll("paper_file", classOf[Part])

    if (parts.nonEmpty && parts.head.getSize > 0) {
      val part = parts.head
      val blob = EmsApp.getBlobRepository(true)
      if (null != paper.filePath && paper.filePath.startsWith("/")) {
        blob.remove(paper.filePath)
      }
      val fileName = part.getSubmittedFileName
      val storeName = s"${writer.std.code}." + Strings.substringAfterLast(fileName, ".")
      val meta = blob.upload("/" + writer.season.id.toString + "/paper/",
        part.getInputStream, storeName, writer.std.code + " " + writer.std.name)
      paper.filePath = meta.filePath
      paper.name = storeName
      paper.submitAt = Instant.now
      paper.status = AuditStatus.Submited
      paper.writer = writer
      paper.title = writer.thesisTitle.getOrElse("--")
      entityDao.saveOrUpdate(paper)

      val msg = s"上传了论文$fileName"
      businessLogger.info(msg, writer.id, Map("file" -> fileName))
    }
    entityDao.saveOrUpdate(paper, paper.writer)
    uploadSingleDoc(paper, "proposal_file", Stage.Proposal)
    uploadSingleDoc(paper, "defense_file", Stage.OralDefense)

    //更新论文抽检
    val checks = entityDao.findBy(classOf[ThesisCheck], "writer", writer)
    if (checks.size == 1) {
      checks foreach { c =>
        c.title = paper.title
        if (paper.language.nonEmpty) c.language = paper.language
        if (paper.keywords.nonEmpty) c.keywords = paper.keywords
        if (paper.researchField.nonEmpty) c.researchField = paper.researchField
      }
      entityDao.saveOrUpdate(checks)
    }
    redirect("search", "info.save.success")
  }

  private def uploadSingleDoc(paper: ThesisPaper, partName: String, stage: Stage): Unit = {
    val parts = getAll(partName, classOf[Part])
    val writer = paper.writer
    if (parts.nonEmpty && parts.head.getSize > 0) {
      val part = parts.head
      val blob = EmsApp.getBlobRepository(true)
      val storeName = s"${writer.std.code}." + Strings.substringAfterLast(part.getSubmittedFileName, ".")

      val dd = entityDao.findBy(classOf[ThesisDoc], "writer" -> writer, "stage" -> stage).headOption.getOrElse(new ThesisDoc)
      dd.writer = writer
      dd.stage = stage
      if (null != dd.filePath && dd.filePath.startsWith("/")) blob.remove(dd.filePath)
      val meta = blob.upload("/" + writer.season.id.toString + s"/${stageDirNames(stage)}/",
        part.getInputStream, storeName, writer.std.code + " " + writer.std.name)
      dd.fileExt = meta.mediaType
      dd.filePath = meta.filePath
      dd.updatedAt = Instant.now
      entityDao.saveOrUpdate(dd)
    }
  }
}
