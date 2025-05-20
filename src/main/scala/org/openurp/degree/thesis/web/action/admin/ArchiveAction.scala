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

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.collection.Collections
import org.beangle.commons.file.zip.Zipper
import org.beangle.commons.io.Files
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.doc.transfer.exporter.ExportContext
import org.beangle.ems.app.EmsApp
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}
import org.beangle.webmvc.view.{Stream, View}
import org.openurp.base.model.Project
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.doc.ThesisPdfGenerator
import org.openurp.degree.thesis.service.{SmsService, ThesisGradeSyncService}
import org.openurp.degree.thesis.web.helper.{PaperDownloadHelper, ThesisArchivePropertyExtractor}
import org.openurp.starter.web.support.ProjectSupport

import java.io.File
import java.time.ZoneId

/** 归档资料查询
 */
class ArchiveAction extends RestfulAction[ThesisArchive], ProjectSupport, ExportSupport[ThesisArchive] {

  var smsService: Option[SmsService] = None
  var thesisGradeSyncService: ThesisGradeSyncService = _

  override protected def indexSetting(): Unit = {
    given project: Project = getProject

    put("departs", getDeparts)
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateOn desc")
    put("seasons", entityDao.search(gQuery))
  }

  def init(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("archive.writer.season"))
    val q = OqlBuilder.from(classOf[ThesisPaper], "paper")
    q.where("paper.writer.season=:season", season)
    q.where(s"not exists(from ${classOf[ThesisArchive].getName} a where a.writer=paper.writer)")
    val papers = entityDao.search(q)
    val archives = papers.map(x => new ThesisArchive(x.writer))
    entityDao.saveOrUpdate(archives)
    redirect("search", "初始化成功")
  }

  override protected def getQueryBuilder: OqlBuilder[ThesisArchive] = {
    given project: Project = getProject

    val query = super.getQueryBuilder
    query.where("archive.writer.std.state.department in(:departs)", getDeparts)
    getBoolean("hasArchive") foreach { hasArchive =>
      if hasArchive then query.where("archive.uploadAt is not null")
      else query.where("archive.uploadAt is null")
    }
    get("advisorConfirmed") foreach { confirmed =>
      if (Strings.isNotBlank(confirmed)) {
        if (confirmed == "null") {
          query.where("archive.confirmed is null")
        } else {
          query.where(s"archive.confirmed=:confirmed", confirmed == "1")
        }
      }
    }
    get("archived") foreach { archived =>
      if (Strings.isNotBlank(archived)) {
        if (archived == "null") {
          query.where("archive.archived is null")
        } else {
          query.where("archive.archived=:archived", archived == "1")
        }
      }
    }
    query
  }

  def pass(): View = {
    val archives = entityDao.find(classOf[ThesisArchive], getLongIds("archive"))
    archives.foreach { archive =>
      archive.archived = Some(true)
    }
    entityDao.saveOrUpdate(archives)
    redirect("search", "操作成功")
  }

  def reject(): View = {
    val archives = entityDao.find(classOf[ThesisArchive], getLongIds("archive"))
    archives.foreach { archive =>
      archive.archived = Some(false)
      archive.confirmed = None
      archive.confirmedBy = None
      archive.confirmAt = None
      smsService foreach { sms =>
        val writer = archive.writer
        sms.send(s"${writer.std.code}的论文材料审核未通过,请登录论文系统查看反馈意见", writer.mobile.get -> writer.name)
      }
    }
    entityDao.saveOrUpdate(archives)
    redirect("search", "退回成功")
  }

  def auditSetting(): View = {
    val archive = entityDao.get(classOf[ThesisArchive], getLongId("archive"))
    val writer = archive.writer
    val paper = entityDao.findBy(classOf[ThesisPaper], "writer", writer).headOption
    val docs = entityDao.findBy(classOf[ThesisDoc], "writer", writer)
    put("writer", writer)
    put("paper", paper)
    put("docs", docs)
    put("archive", archive)
    forward()
  }

  def preview2(): View = {
    val doc = entityDao.get(classOf[ThesisDoc], getLongId("doc"))
    put("doc_url", EmsApp.getBlobRepository(true).url(doc.filePath))
    forward()
  }

  def preview(): View = {
    val doc = entityDao.get(classOf[ThesisDoc], getLongId("doc"))
    put("doc_url", EmsApp.getBlobRepository(true).url(doc.filePath))
    forward()
  }

  def audit(): View = {
    val archive = entityDao.get(classOf[ThesisArchive], getLongId("archive"))
    archive.archived = getBoolean("archive.archived")
    if (!archive.archived.getOrElse(false)) {
      archive.confirmed = None
      archive.confirmedBy = None
      archive.confirmAt = None
      smsService foreach { sms =>
        val writer = archive.writer
        sms.send(s"${writer.std.code}的论文材料审核未通过,请登录论文系统查看反馈意见", writer.mobile.get -> writer.name)
      }
    }
    archive.feedback = get("archive.feedback")
    entityDao.saveOrUpdate(archive)
    //如果同意归档则发布成绩
    if (archive.archived.getOrElse(false)) {
      entityDao.findBy(classOf[ThesisReview], "writer", archive.writer) foreach { review =>
        if !review.courseGradeSynced then thesisGradeSyncService.sync(review)
      }
    }
    val paper = entityDao.findBy(classOf[ThesisPaper], "writer", archive.writer).headOption
    redirect("auditSetting", s"&archive.id=${archive.id}", "审核完成")
  }

  def downloadDoc(): View = {
    val doc = entityDao.get(classOf[ThesisDoc], getLongId("doc"))
    val path = EmsApp.getBlobRepository(true).url(doc.filePath)
    val response = ActionContext.current.response
    response.sendRedirect(path.get.toString)
    null
  }

  def downloadArchive(): View = {
    val archive = entityDao.get(classOf[ThesisArchive], getLongId("archive"))
    val docs = entityDao.findBy(classOf[ThesisDoc], "writer", archive.writer).groupBy(_.writer)
    val writer = archive.writer
    val stdDir = ThesisPdfGenerator.getDepartFolder(writer.season.id, writer.department.id, writer.std.code)
    val zipFiles = PaperDownloadHelper.downloadDocs(new File(stdDir), docs, None)
    Stream(zipFiles.head, MediaTypes.ApplicationZip, s"${writer.code}的论文材料.zip").cleanup { () =>
      zipFiles.head.delete()
      Files.travel(new File(stdDir), f => f.delete())
      new File(stdDir).delete()
    }
  }

  def downloadArchives(): View = {
    val archives = entityDao.find(classOf[ThesisArchive], getLongIds("archive"))
    val docs = entityDao.findBy(classOf[ThesisDoc], "writer", archives.map(_.writer)).groupBy(_.writer)
    val writer = archives.head.writer
    val docDir = ThesisPdfGenerator.getDepartFolder(writer.season.id, writer.department.id, "archive")
    val zipFiles = PaperDownloadHelper.downloadDocs(new File(docDir), docs, None)
    val targetZip = ThesisPdfGenerator.getDepartZipFile(writer.season.id, writer.department.id, "archive")
    Zipper.zip(new File(docDir), targetZip)
    Stream(targetZip, MediaTypes.ApplicationZip, s"${archives.head.writer.code}等${archives.size}人的论文材料.zip").cleanup { () =>
      targetZip.delete()
      Files.travel(new File(docDir), f => f.delete())
      new File(docDir).delete()
    }
  }

  def batchUpdateForm(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("archive.writer.season"))
    put("season", season)
    forward()
  }

  def batchUpdateTime(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("archive.writer.season"))
    var messages = Collections.newBuffer[String]
    getDate("commitment.updatedAt") foreach { d =>
      val updatedAt = d.atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toInstant
      val sql = s"update ${classOf[Commitment].getName} cm set cm.updatedAt=?1 where cm.writer.season.id=?2"
      val cnt = entityDao.executeUpdate(sql, updatedAt, season.id)
      messages += s"更新了任务书${cnt}条"
    }
    getDate("proposal.confirmAt") foreach { d =>
      val confirmAt = d.atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toInstant
      val sql = s"update ${classOf[Proposal].getName} cm set cm.confirmAt=?1 where cm.writer.season.id=?2"
      val cnt = entityDao.executeUpdate(sql, confirmAt, season.id)
      messages += s"更新了开题报告${cnt}条"
    }
    getDate("thesisReview.advisorReviewAt") foreach { d =>
      val advisorReviewAt = d.atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toInstant
      val sql = s"update ${classOf[ThesisReview].getName} cm set cm.advisorReviewAt=?1 where cm.writer.season.id=?2"
      val cnt = entityDao.executeUpdate(sql, advisorReviewAt, season.id)
      messages += s"更新了导师评分${cnt}条"
    }
    getDate("thesisReview.crossReviewAt") foreach { d =>
      val crossReviewAt = d.atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toInstant
      val sql = s"update ${classOf[ThesisReview].getName} cm set cm.crossReviewAt=?1 where cm.writer.season.id=?2"
      val cnt = entityDao.executeUpdate(sql, crossReviewAt, season.id)
      messages += s"更新了交叉评阅${cnt}条"
    }
    val message = if (messages.isEmpty) "更新0条" else messages.mkString("|")
    redirect("search", s"archive.writer.season.id=${season.id}", message)
  }

  override protected def configExport(context: ExportContext): Unit = {
    context.extractor = new ThesisArchivePropertyExtractor(entityDao)
    super.configExport(context)
  }

  override protected def simpleEntityName: String = "archive"
}
