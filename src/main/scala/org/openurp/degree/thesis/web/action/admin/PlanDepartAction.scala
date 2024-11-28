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

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.EntityAction
import org.openurp.base.model.Project
import org.openurp.degree.thesis.model.{DepartPlan, PlanStatus, Stage, ThesisPlan}
import org.openurp.starter.web.support.ProjectSupport

import java.time.{Instant, ZoneId}

class PlanDepartAction extends ActionSupport, EntityAction[DepartPlan], ProjectSupport {
  var entityDao: EntityDao = _

  def index(): View = {
    given project: Project = getProject

    var thesisPlans = entityDao.getAll(classOf[ThesisPlan])
    thesisPlans = thesisPlans.sortBy(_.beginOn).reverse
    val curThesisPlan = entityDao.get(classOf[ThesisPlan], getLong("plan.thesisPlan.id").getOrElse(thesisPlans.head.id))
    val query = OqlBuilder.from(classOf[DepartPlan], "plan")
    query.where("plan.department in(:departs)", getDeparts)
    query.where("plan.thesisPlan =:thesisPlan", curThesisPlan)
    val plans = entityDao.search(query)
    put("plans", plans)
    put("curThesisPlan", curThesisPlan)
    put("thesisPlans", thesisPlans)
    forward()
  }

  def save(): View = {
    val plan = populateEntity(classOf[DepartPlan], "plan")
    plan.status = PlanStatus.待学院修改
    Stage.values foreach { stage =>
      if (stage.subCount == 0) {
        getInstant(s"${stage.id}_beginOn") foreach { beginOn =>
          plan.addTime(stage, beginOn, getEndAt(stage))
        }
      }
    }
    entityDao.saveOrUpdate(plan)
    redirect("index", "info.save.success")
  }

  def edit(): View = {
    put("stages", Stage.values)
    put("plan", entityDao.find(classOf[DepartPlan], getLongId("plan")))
    forward()
  }

  def submit(): View = {
    val plan = populateEntity(classOf[DepartPlan], "plan")
    if plan.status != PlanStatus.审查通过 then plan.status = PlanStatus.待学校审查
    entityDao.saveOrUpdate(plan)
    redirect("index", "info.save.success")
  }

  def print(): View = {
    getLong("id") foreach { id => put("plan", entityDao.get(classOf[DepartPlan], id)) }
    forward()
  }

  private def getEndAt(stage: Stage): Instant = {
    var endTime = getInstant(s"${stage.id}_endOn").get.atZone(ZoneId.systemDefault())
    if (endTime.getHour == 0) {
      endTime = endTime.plusDays(1).minusMinutes(1)
    }
    endTime.toInstant
  }

  override protected def simpleEntityName: String = "plan"
}
