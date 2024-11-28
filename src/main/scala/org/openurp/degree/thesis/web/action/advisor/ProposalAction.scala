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

import org.beangle.webmvc.view.{Status, View}
import org.openurp.base.model.AuditStatus
import org.openurp.degree.thesis.model.*

import java.time.Instant

class ProposalAction extends AdvisorSupport {

  def index(): View = {
    val advisor = getAdvisor
    val writers = getWriters
    val proposals = entityDao.findBy(classOf[Proposal], "writer", writers).map(x => (x.writer, x)).toMap
    put("proposals", proposals)
    put("writers", writers)
    put("historyWriters", getHistoryWriters)
    put("stage", Stage.Proposal)
    forward()
  }

  def info(): View = {
    val c = entityDao.findBy(classOf[Proposal], "writer.id", getLongId("writer")).headOption
    c match
      case Some(proposal) =>
        put("proposal", proposal)
        put("passed", proposal.status != AuditStatus.Rejected)
        put("writer", proposal.writer)
        forward()
      case None => Status.NotFound
  }

  def audit(): View = {
    val advisor = getAdvisor
    val proposal = entityDao.get(classOf[Proposal], getLong("proposal.id").get)
    if (proposal.writer.advisor.contains(advisor) && proposal.writer.thesisTitle.nonEmpty) {
      val status = getBoolean("status", true)
      if status then
        proposal.status = AuditStatus.Passed
        proposal.confirmAt = Some(Instant.now)
      else
        proposal.status = AuditStatus.Rejected
        proposal.confirmAt = None

      proposal.advisorOpinion = get("advisorOpinion")
      entityDao.saveOrUpdate(proposal)
      redirect("index", "info.save.success")
    } else {
      redirect("index", "请评阅自己的学生")
    }
  }
}
