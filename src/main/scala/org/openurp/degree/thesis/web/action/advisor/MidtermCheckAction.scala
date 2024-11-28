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

import org.beangle.data.dao.EntityDao
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.{Status, View}
import org.openurp.base.model.AuditStatus
import org.openurp.degree.thesis.model.*

class MidtermCheckAction extends AdvisorSupport {

  def index(): View = {
    val writers = getWriters
    val midtermChecks = entityDao.findBy(classOf[MidtermCheck], "writer", writers).map(x => (x.writer, x)).toMap
    put("midtermChecks", midtermChecks)
    put("writers", writers)
    put("stage", Stage.MidtermCheck)
    forward()
  }

  def check(): View = {
    val writer = entityDao.get(classOf[Writer], getLong("writer.id").getOrElse(0l))
    val check = entityDao.findBy(classOf[MidtermCheck], "writer", writer).headOption
    put("midtermCheck", check)
    put("writer", writer)
    put("items", entityDao.getAll(classOf[MidtermCheckItem]))
    forward()
  }

  def save(@param("id") id: String): View = {
    val midtermCheck = entityDao.get(classOf[MidtermCheck], id.toLong)
    val items = entityDao.getAll(classOf[MidtermCheckItem])
    items foreach { item =>
      val detail = midtermCheck.getDetail(item).getOrElse(new MidtermCheckDetail)
      if (!detail.persisted) {
        detail.check = midtermCheck
        detail.item = item
        midtermCheck.details += detail
      }
      val status = getBoolean(s"item${item.id}_passed", true)
      detail.passed = status
      detail.auditOpinion = get("item" + item.id + "_bz")
    }
    val auditStatus = midtermCheck.advisorAuditStatus
    if (auditStatus != AuditStatus.Blank) {
      if (auditStatus == AuditStatus.Passed) {
        midtermCheck.status = AuditStatus.Passed
      } else {
        midtermCheck.status = AuditStatus.RejectedByAdvisor
      }
    }
    entityDao.saveOrUpdate(midtermCheck)
    redirect("index", "info.save.success")
  }

  def info(): View = {
    val c = entityDao.findBy(classOf[MidtermCheck], "writer.id", getLongId("writer")).headOption
    c match
      case Some(midtermCheck) =>
        put("midtermCheck", midtermCheck)
        put("writer", midtermCheck.writer)
        forward()
      case None => Status.NotFound
  }

}
