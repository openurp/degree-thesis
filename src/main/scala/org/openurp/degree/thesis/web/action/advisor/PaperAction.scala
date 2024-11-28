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

package org.openurp.degree.thesis.web.action.advisor

import jakarta.servlet.http.Part
import org.beangle.commons.lang.Strings
import org.beangle.ems.app.EmsApp
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.view.{Status, View}
import org.openurp.degree.thesis.model.{PaperSubmission, ThesisPaper}
import org.openurp.degree.thesis.service.SmsService

class PaperAction extends AdvisorSupport {

  var smsService: Option[SmsService] = None

  def index(): View = {
    val writers = getWriters
    val papers = entityDao.findBy(classOf[ThesisPaper], "writer", writers).map(x => (x.writer, x)).toMap
    put("papers", papers)
    put("submissions", entityDao.findBy(classOf[PaperSubmission], "writer", writers).groupBy(_.writer))
    put("writers", writers)
    forward()
  }

  def auditForm(): View = {
    val advisor = getAdvisor
    val paper = entityDao.get(classOf[ThesisPaper], getLongId("paper"))
    if (paper.writer.advisor.contains(advisor)) {
      val submissions = entityDao.findBy(classOf[PaperSubmission], "writer", paper.writer)
      put("paper", paper)
      put("submissions", submissions)
      forward()
    } else {
      redirect("index", "只能审核自己学生的论文")
    }
  }

  def audit(): View = {
    val advisor = getAdvisor
    val paper = entityDao.get(classOf[ThesisPaper], getLongId("paper"))

    if (paper.writer.advisor.contains(advisor)) {
      val writer = paper.writer
      val advisorPassed = getBoolean("passed")
      val advisorOpinion = get("opinion")
      if (paper.finalized) {
        paper.advisorPassed = advisorPassed
      }
      entityDao.saveOrUpdate(paper)

      getLong("submission.id") foreach { subId =>
        val sub = entityDao.findBy(classOf[PaperSubmission], "writer" -> writer, "id" -> subId).head
        sub.advisorOpinion = advisorOpinion
        if (sub.finalized) {
          sub.advisorPassed = advisorPassed
        }
        val parts = getAll("file", classOf[Part])
        if (parts.nonEmpty && parts.head.getSize > 0) {
          val part = parts.head
          val blob = EmsApp.getBlobRepository(true)
          val submissionFileName = System.currentTimeMillis() + "." + Strings.substringAfterLast(part.getSubmittedFileName, ".")
          val meta1 = blob.upload("/" + writer.season.id.toString + s"/advisor_revision/${advisor.id}/",
            part.getInputStream, submissionFileName, advisor.code + " " + advisor.name)
          sub.revisionPath = Some(meta1.filePath)
        } else {
          if (sub.revisionPath.nonEmpty) {
            val blob = EmsApp.getBlobRepository(true)
            blob.remove(sub.revisionPath.get)
            sub.revisionPath = None
          }
        }
        entityDao.saveOrUpdate(sub)
      }
      if advisorPassed.isEmpty then
        businessLogger.info(s"指导了${paper.writer.code} ${paper.writer.name}的论文", writer.id, Map("paper.id" -> paper.id))
      else
        businessLogger.info(s"审核了${paper.writer.code} ${paper.writer.name}的论文，结论为：${advisorPassed.get}", writer.id, Map("paper.id" -> paper.id))

      if (writer.mobile.nonEmpty && writer.mobile.get.length == 11) {
        smsService.foreach { sms =>
          val template =
            if paper.advisorPassed.isEmpty then
              s"${writer.name}同学，${advisor.name}老师给您的论文填写了指导意见，请知悉。"
            else
              s"${writer.name}同学，${advisor.name}老师审核了您的论文，结论为：${if paper.advisorPassed.get then "通过" else "不通过，修改后提交"}，请知悉。"
          sms.send(template, writer.mobile.get -> writer.name)
        }
      }
      redirect("index", "审核成功")
    } else {
      redirect("index", "只能审核自己学生的论文")
    }
  }

  def submission(): View = {
    val subs = entityDao.findBy(classOf[PaperSubmission], "writer.id", getLongId("writer"))
    put("subs", subs)
    forward()
  }

  def submissionDoc(): View = {
    val sub = entityDao.find(classOf[PaperSubmission], getLongId("submission"))
    sub match {
      case None => Status.NotFound
      case Some(s) => download(s.filePath)
    }
  }

  def doc(): View = {
    val paper = getLong("id") match {
      case None => entityDao.findBy(classOf[ThesisPaper], "writer.id", getLongId("writer")).headOption
      case Some(id) => Some(entityDao.get(classOf[ThesisPaper], id))
    }
    paper match {
      case None => Status.NotFound
      case Some(file) => download(file.filePath)
    }
  }

  private def download(filePath: String): View = {
    val path = EmsApp.getBlobRepository(true).url(filePath)
    val response = ActionContext.current.response
    response.sendRedirect(path.get.toString)
    null
  }
}
