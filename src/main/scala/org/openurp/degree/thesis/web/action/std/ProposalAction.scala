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

import org.beangle.data.dao.OqlBuilder
import org.beangle.webmvc.view.View
import org.openurp.base.model.AuditStatus
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.web.helper.StageChecker

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, ZoneId}

class ProposalAction extends WriterSupport {

  def index(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    val proposal = entityDao.findBy(classOf[Proposal], "writer", writer).headOption
    val deadline = writer.getOrCreateDeadline(Stage.Proposal)

    var message = ""
    val stageTime = plan.getStageTime(Stage.Proposal)
    deadline.endAt match {
      case None => message = "请于 " + stageTime.endOn + "前完成撰写并提交"
      case Some(endAt) =>
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        message = "请于 " + endAt.atZone(ZoneId.systemDefault()).format(formatter) + "前完成撰写并提交"
    }
    put("message", message)
    if (deadline.delayCount > 0) {
      put("delay", "你的开题报告已延期 " + deadline.delayCount + "次 ,多次延期将影响到论文最终成绩");
    }
    put("writer", writer)
    put("proposal", proposal)
    forward()
  }

  def edit(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    new StageChecker(entityDao, plan).check(writer, Stage.Proposal, false) match {
      case Some(msg) => return redirect("index", msg)
      case None =>
    }
    val proposal = entityDao.findBy(classOf[Proposal], "writer", writer).headOption match {
      case Some(p) => p
      case None => val p = new Proposal
        p.writer = writer
        p
    }
    put("writer", writer)
    put("proposal", proposal)
    forward()
  }

  def info(): View = {
    val writer = getWriter
    val proposal = entityDao.findBy(classOf[Proposal], "writer", writer).headOption
    put("writer", writer)
    put("proposal", proposal)
    forward()
  }

  def submit(): View = {
    val writer = getWriter
    //check(writer) foreach { v => return v }

    val proposalOption = entityDao.findBy(classOf[Proposal], "writer", writer).headOption
    val proposal = proposalOption.getOrElse(new Proposal)
    proposal.writer = writer
    proposal.submitAt = Instant.now
    proposal.meanings = get("proposal.meanings", "")
    proposal.conditions = get("proposal.conditions", "")
    proposal.outline = get("proposal.outline", "")
    proposal.references = get("proposal.references", "")
    proposal.methods = get("proposal.methods", "")

    val justSave = getBoolean("justSave", true)
    if (!justSave) {
      proposal.status = AuditStatus.Submited
      val deadline = writer.getOrCreateDeadline(Stage.Proposal)
      deadline.submitAt = Some(Instant.now)
      entityDao.saveOrUpdate(proposal, deadline)
    } else {
      proposal.status = AuditStatus.Draft
      entityDao.saveOrUpdate(proposal)
    }
    redirect("index", "info.save.success")
  }
}
