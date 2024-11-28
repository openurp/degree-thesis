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

import org.beangle.commons.lang.Enums
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.EntityAction
import org.openurp.base.model.{AuditStatus, Project}
import org.openurp.degree.thesis.model.Subject
import org.openurp.degree.thesis.service.ThesisPlanService
import org.openurp.starter.web.support.ProjectSupport

class SubjectAuditAction extends ActionSupport, EntityAction[Subject], DepartSupport, ProjectSupport {
  var entityDao: EntityDao = _
  var businessLogger: WebBusinessLogger = _
  var thesisPlanService: ThesisPlanService = _

  def index(): View = {
    put("Passed", AuditStatus.Passed)
    put("Rejected", AuditStatus.Rejected)
    put("Submited", AuditStatus.Submited)
    forward()
  }

  def search(): View = {
    given project: Project = getProject

    val departs = getDeparts
    val season = thesisPlanService.getPlan().get.season
    val status = Enums.of(classOf[AuditStatus], getInt("status", 1)).get
    val query = OqlBuilder.from(classOf[Subject], "subject")
    query.where("subject.depart in(:departs)", departs)
    query.where("subject.season=:season", season)
    query.where("subject.status =:status", status)
    val subjects = entityDao.search(query)
    put("subjects", subjects)
    forward()
  }

  def auditForm(): View = {
    val subjects = entityDao.find(classOf[Subject], getLongIds("subject"))
    put("subjects", subjects)
    forward()
  }

  def audit(): View = {
    val status = getInt("status", 0)
    val subjects = entityDao.find(classOf[Subject], getLongIds("subject"))
    put("subjects", subjects)
    val opinion = get("auditOpinion")

    val newStatus = Enums.of(classOf[AuditStatus], getInt("newStatus", 1)).getOrElse(AuditStatus.Rejected)
    subjects.foreach { subject =>
      subject.status = newStatus
      subject.auditOpinion = opinion
    }
    entityDao.saveOrUpdate(subjects)

    subjects.foreach { subject =>
      val msg =
        if newStatus == AuditStatus.Passed then
          s"论文题目审核：通过了${subject.advisor.teacher.name}的论文题目：${subject.name}"
        else
          s"论文题目审核：驳回了${subject.advisor.teacher.name}的论文题目：${subject.name}"
      businessLogger.info(msg, subject.id, Map("subject" -> subject.name))
    }
    redirect("search", "status=" + status, "info.save.success")
  }

  @mapping(value = "{id}")
  def info(@param("id") id: String): View = {
    put("subject", entityDao.get(classOf[Subject], id.toLong))
    forward()
  }
}
