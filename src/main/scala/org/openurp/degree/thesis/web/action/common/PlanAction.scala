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

package org.openurp.degree.thesis.web.action.common

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.security.Securities
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.View
import org.openurp.base.model.{Department, User}
import org.openurp.degree.thesis.model.{Advisor, DepartPlan}
import org.openurp.degree.thesis.service.ThesisPlanService

class PlanAction extends ActionSupport {
  var entityDao: EntityDao = _

  var thesisPlanService: ThesisPlanService = _

  def index(): View = {
    put("plans", findPlan())
    forward()
  }

  def print(): View = {
    put("plan", findPlan().headOption)
    forward()
  }

  private def findPlan(): Seq[DepartPlan] = {
    getLong("id") match {
      case Some(id) => Seq(entityDao.get(classOf[DepartPlan], id))
      case None =>
        val user = entityDao.findBy(classOf[User], "code", Securities.user).head
        val advisors = entityDao.findBy(classOf[Advisor], "teacher.staff.code", Securities.user)
        var departs: collection.Seq[Department] = null
        if (advisors.isEmpty) {
          departs = List(user.department)
        } else {
          departs = advisors.head.departs
        }

        val query = OqlBuilder.from(classOf[DepartPlan], "plan")
        query.where("plan.department in(:departs)", departs)
        query.where("plan.thesisPlan=:thesisPlan", thesisPlanService.getPlan().head)
        //        query.where("plan.status=:status", PlanStatus.审查通过)
        entityDao.search(query)
    }
  }
}
