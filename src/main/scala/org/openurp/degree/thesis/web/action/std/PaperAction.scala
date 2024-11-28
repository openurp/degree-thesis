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

package org.openurp.degree.thesis.web.action.std

import jakarta.servlet.http.Part
import org.beangle.commons.file.digest.Sha1
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.view.{Status, View}
import org.openurp.base.model.AuditStatus
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.SmsService

import java.io.{FileInputStream, FileOutputStream}
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

class PaperAction extends WriterSupport {

  var smsService: Option[SmsService] = None

  def index(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    val draftStageTime = plan.getStageTime(Stage.ThesisDraftSubmit)
    val finalStageTime = plan.getStageTime(Stage.ThesisFinalSubmit)
    if (draftStageTime.timeSuitable(Instant.now) == 0) {
      var message = ""
      val deadline = writer.getOrCreateDeadline(Stage.ThesisDraftSubmit)
      deadline.endAt match {
        case None => message = "请于 " + draftStageTime.endOn + "前完成撰写并提交"
        case Some(endAt) =>
          val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
          message = "请于 " + endAt.atZone(ZoneId.systemDefault()).format(formatter) + "前完成撰写并提交"
      }
      put("message", message)
      if (deadline.delayCount > 0) {
        put("delay", "你的论文已延期 " + deadline.delayCount + "次 ,多次延期将影响到论文最终成绩")
      }
    }

    put("draftStageTime", draftStageTime)
    put("finalStageTime", finalStageTime)
    put("writer", writer)
    val file = entityDao.findBy(classOf[ThesisPaper], "writer", writer).headOption
    put("file", file)
    val submissions = entityDao.findBy(classOf[PaperSubmission], "writer", writer)
    put("submissions", submissions.sortBy(_.updatedAt))
    forward()
  }

  def uploadForm(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    put("displayFinalized", false) //初始不显示终稿
    put("defaultFinalized", false)
    put("writer", writer)
    val msg = checkStageTime(plan, writer)
    if (msg == null) forward() else redirect("index", msg)
  }

  private def checkStageTime(plan: DepartPlan, writer: Writer): String = {
    val draftStageTime = plan.getStageTime(Stage.ThesisDraftSubmit)
    val finalStageTime = plan.getStageTime(Stage.ThesisFinalSubmit)
    val now = Instant.now()

    //先检查初稿时间，有可能初稿和终稿时间有重叠
    if (draftStageTime.timeSuitable(now) == 0) {
      put("stageTime", draftStageTime)
      put("displayFinalized", true)
      null
    } else if (finalStageTime.timeSuitable(now) == 0) {
      put("stageTime", finalStageTime)
      put("displayFinalized", true)

      val paper = entityDao.first(OqlBuilder.from(classOf[ThesisPaper], "f").where("f.writer=:writer", writer))
      val msg =
        if paper.isEmpty then "没有提交论文初稿，无法提交论文终稿。"
        else if (paper.isDefined && paper.get.advisorPassed.getOrElse(false)) "论文终稿已审核通过，无需提交。"
        else null

      paper foreach { p =>
        put("defaultFinalized", p.finalized)
      }
      msg
    } else {
      put("displayFinalized", true)
      put("defaultFinalized", true)
      put("stageTime", finalStageTime)
      val checks = entityDao.findBy(classOf[CopyCheck], "writer", writer)
      if (checks.isEmpty) {
        "不在提交论文时间段内，尚不能提交。"
      } else {
        if checks.exists(_.passed) then "不在提交论文时间段内，尚不能提交。"
        else null
      }
    }
  }

  def upload(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    val msg = checkStageTime(plan, writer)
    if (msg != null) return redirect("index", msg)

    val paper = entityDao.first(OqlBuilder.from(classOf[ThesisPaper], "f").where("f.writer=:writer", writer))
      .getOrElse(new ThesisPaper)
    val parts = getAll("file", classOf[Part])

    if (parts.nonEmpty && parts.head.getSize > 0) {
      val part = parts.head
      val blob = EmsApp.getBlobRepository(true)

      val localFile = java.io.File.createTempFile("paper", "pdf")
      IOs.copy(part.getInputStream, new FileOutputStream(localFile))

      val finalized = getBoolean("finalized", false)
      val sha1sum = Sha1.digest(localFile)
      val submission = new PaperSubmission
      submission.writer = writer
      submission.title = writer.thesisTitle.getOrElse("--")
      submission.updatedAt = Instant.now
      submission.sha1sum = sha1sum
      submission.finalized = finalized

      val submissionFileName = sha1sum + "." + Strings.substringAfterLast(part.getSubmittedFileName, ".")
      val meta1 = blob.upload("/" + writer.season.id.toString + "/paper_submission/",
        new FileInputStream(localFile), submissionFileName, writer.std.code + " " + writer.std.name)
      submission.filePath = meta1.filePath
      entityDao.saveOrUpdate(submission)

      if (null != paper.filePath && paper.filePath.startsWith("/")) {
        blob.remove(paper.filePath)
      }
      val fileName = part.getSubmittedFileName
      val storeName = s"${writer.std.code}." + Strings.substringAfterLast(fileName, ".")
      val meta = blob.upload("/" + writer.season.id.toString + "/paper/",
        new FileInputStream(localFile), storeName, writer.std.code + " " + writer.std.name)

      paper.filePath = meta.filePath
      paper.name = storeName
      paper.submitAt = Instant.now
      paper.status = AuditStatus.Submited //覆盖之前的结论
      paper.sha1sum = sha1sum
      paper.writer = writer
      paper.finalized = finalized
      if (!finalized) { //没有定稿，仍在初稿阶段的仍算作初稿
        val draftStageTime = plan.getStageTime(Stage.ThesisDraftSubmit)
        if (draftStageTime.timeSuitable(Instant.now) == 0) {
          paper.draftPath = Some(paper.filePath)
        }
      }
      paper.title = writer.thesisTitle.getOrElse("--")
      entityDao.saveOrUpdate(paper)

      val reviews = entityDao.findBy(classOf[ThesisReview], "writer", writer).headOption
      if (reviews.isEmpty) {
        val review = new ThesisReview
        review.writer = writer
        entityDao.saveOrUpdate(review)
      }
      businessLogger.info(s"上传了论文$fileName", writer.id, Map("file" -> fileName))
      if (paper.finalized && paper.writer.advisor.nonEmpty) {
        val mobile = paper.writer.advisor.get.mobile
        if (mobile.nonEmpty && mobile.get.length == 11) {
          smsService foreach { sms =>
            val template = s"${paper.writer.advisor.get.name}老师您好,你指导的学生${paper.writer.name}，提交了定稿论文，待审核请知悉。" +
              s"访问论文系统${Ems.base}/degree/thesis/advisor/paper/auditForm?paper.id=${paper.id}，直接审核即可。"
            sms.send(template, mobile.get -> paper.writer.advisor.get.name)
          }
        }
      }
    }
    redirect("index", "info.save.success")
  }

  /** 下载论文附件
   *
   * @return
   */
  def attachment(): View = {
    val writer = getWriter
    val file = entityDao.findBy(classOf[ThesisPaper], "writer", writer).headOption
    file match {
      case None => Status.NotFound
      case Some(f) => download(f.filePath)
    }
  }

  /** 下载论文初稿附件
   *
   * @return
   */
  def draft(): View = {
    val writer = getWriter
    val file = entityDao.findBy(classOf[ThesisPaper], "writer", writer).headOption
    file match {
      case None => Status.NotFound
      case Some(f) => f.draftPath match
        case None => Status.NotFound
        case Some(draftPath) => download(draftPath)
    }
  }

  def submission(): View = {
    val sub = entityDao.find(classOf[PaperSubmission], getLongId("submission"))
    sub match {
      case None => Status.NotFound
      case Some(s) =>
        val writer = getWriter
        if (writer == s.writer) download(s.filePath)
        else Status.NotFound
    }
  }

  /** 下载教师修订附件
   *
   * @return
   */
  def advisorRevision(): View = {
    val writer = getWriter
    val submissionId = getLongId("submission")
    val file = entityDao.findBy(classOf[PaperSubmission], "writer" -> writer, "id" -> submissionId).headOption
    file match {
      case None => Status.NotFound
      case Some(f) =>
        f.revisionPath match
          case None => Status.NotFound
          case Some(p) => download(p)
    }
  }

  private def download(filePath: String): View = {
    val path = EmsApp.getBlobRepository(true).url(filePath)
    val response = ActionContext.current.response
    response.sendRedirect(path.get.toString)
    null
  }
}
