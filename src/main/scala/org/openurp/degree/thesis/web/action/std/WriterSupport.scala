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

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.security.Securities
import org.beangle.webmvc.support.ActionSupport
import org.openurp.base.model.Department
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{DepartPlan, Writer}
import org.openurp.degree.thesis.service.ThesisPlanService

abstract class WriterSupport extends ActionSupport {
  var entityDao: EntityDao = _
  var thesisPlanService: ThesisPlanService = _
  var businessLogger: WebBusinessLogger = _

  protected def getWriter: Writer = {
    val plan = thesisPlanService.getPlan()
    var writers:Iterable[Writer]= null
    if(plan.nonEmpty){
      val query = OqlBuilder.from(classOf[Writer], "w")
      query.where("w.std.code=:code and w.season = :season", Securities.user, plan.get.season)
      writers = entityDao.search(query)
    }
    if (null==writers || writers.isEmpty) {
      val query = OqlBuilder.from(classOf[Writer], "w")
      query.where("w.std.code=:code", Securities.user)
      query.orderBy("w.season.graduateIn desc")
      entityDao.search(query).head
    } else {
      writers.head
    }
  }
}
