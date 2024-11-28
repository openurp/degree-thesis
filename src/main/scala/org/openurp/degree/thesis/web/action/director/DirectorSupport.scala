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

package org.openurp.degree.thesis.web.action.director

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.security.Securities
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.support.action.EntityAction
import org.openurp.base.edu.model.TeachingOffice
import org.openurp.degree.thesis.model.{Advisor, Writer}
import org.openurp.degree.thesis.service.ThesisPlanService
import org.openurp.starter.web.support.TeacherSupport

abstract class DirectorSupport extends ActionSupport {
  var entityDao: EntityDao = _
  var thesisPlanService: ThesisPlanService = _
  var businessLogger: WebBusinessLogger = _

  protected def getAdvisor: Advisor = {
    entityDao.findBy(classOf[Advisor], "teacher.staff.code", Securities.user).head
  }

  protected def getWriters: Iterable[Writer] = {
    val plan = thesisPlanService.getPlan().get
    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.advisor.teacher.staff.code = :me", Securities.user)
    query.where("writer.thesisTitle is not null")
    query.where("writer.season=:season", plan.season)
    query.orderBy("writer.std.code")
    entityDao.search(query)
  }

  def getOffices(): Seq[TeachingOffice] = {
    val query = OqlBuilder.from(classOf[TeachingOffice], "office")
    query.where("office.director.staff.code=:me", Securities.user)
    entityDao.search(query)
  }

}
