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

import org.beangle.data.dao.OqlBuilder
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.RestfulAction
import org.openurp.base.model.Department
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.*
import org.openurp.starter.web.support.ProjectSupport

import java.time.{Duration, Instant, LocalTime, ZoneId}

class PlanAction extends RestfulAction[ThesisPlan], ProjectSupport {

  override protected def simpleEntityName: String = "plan"

  override def index(): View = {
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateIn desc")
    put("seasons", entityDao.search(gQuery))
    forward()
  }

  override def editSetting(plan: ThesisPlan): Unit = {
    getLong("plan.season.id") foreach { seasonId =>
      plan.season = entityDao.get(classOf[GraduateSeason], seasonId)
    }
    put("stages", Stage.values)
  }

  override protected def saveAndRedirect(plan: ThesisPlan): View = {
    val plans = plan.departPlans.map(x => (x.department, x)).toMap
    Stage.values foreach { stage =>
      if (stage.subCount == 0) {
        getInstant(s"${stage.id}_beginOn") foreach { begin =>
          val end = getEndAt(stage)
          plan.times.find(x => x.stage == stage) match {
            case None => plan.times.addOne(StageTime(stage, begin, end))
            case Some(st) =>
              st.beginAt = begin
              st.endAt = end
          }
        }
      }
    }
    plan.project = getProject
    plan.beginOn = plan.times.map(_.beginAt).min.atZone(ZoneId.systemDefault()).toLocalDate
    plan.endOn = plan.times.map(_.endAt).max.atZone(ZoneId.systemDefault()).toLocalDate

    entityDao.saveOrUpdate(plan)
    val departQuery = OqlBuilder.from[Department](classOf[Writer].getName, "w")
    departQuery.where("w.season=:season", plan.season)
    departQuery.select("distinct w.std.state.department")
    val departs = entityDao.search(departQuery)
    departs foreach { depart =>
      plans.get(depart) match {
        case None =>
          val dp = new DepartPlan
          dp.department = depart
          dp.thesisPlan = plan
          dp.status = PlanStatus.待学院修改
          Stage.values foreach { stage =>
            if (stage.subCount == 0) {
              getInstant(s"${stage.id}_beginOn") foreach { beginAt =>
                dp.addTime(stage, beginAt, getEndAt(stage))
              }
            }
          }
          entityDao.saveOrUpdate(dp)
        case Some(dp) =>
          if (dp.status == PlanStatus.待学院修改) {
            Stage.values foreach { stage =>
              if (stage.subCount == 0) {
                getInstant(s"${stage.id}_beginOn") foreach { beginAt =>
                  dp.addTime(stage, beginAt, getEndAt(stage))
                }
              }
            }
            entityDao.saveOrUpdate(dp)
          }
      }
    }
    redirect("search", "info.save.success")
  }

  def print(): View = {
    getLong("id") foreach { id => put("plan", entityDao.get(classOf[DepartPlan], id)) }
    forward()
  }

  def check(): View = {
    put("Passed", PlanStatus.审查通过)
    put("Rejected", PlanStatus.审查未通过)
    getLong("id") foreach { id => put("departPlan", entityDao.get(classOf[DepartPlan], id)) }
    forward()
  }

  def removeDepartPlan(): View = {
    val departPlan = entityDao.get(classOf[DepartPlan], getLongId("departPlan"))
    if (departPlan.status != PlanStatus.审查通过) {
      entityDao.remove(departPlan)
    }
    redirect("search", "plan.season.id=" + departPlan.thesisPlan.season.id.toString, "删除成功")
  }

  private def addTime(departPlan: DepartPlan, stage: Stage, begin: Instant, end: Instant): Unit = {
    departPlan.times.find(x => x.stage == stage) match {
      case None => departPlan.times.addOne(StageTime(stage, begin, end))
      case Some(st) =>
        if (st.beginOn != begin || st.endOn != end) {
          departPlan.times.subtractOne(st)
          departPlan.times.addOne(StageTime(stage, begin, end))
        }
    }
  }

  private def getEndAt(stage: Stage): Instant = {
    var endTime = getInstant(s"${stage.id}_endOn").get.atZone(ZoneId.systemDefault())
    if (endTime.getHour == 0) {
      endTime = endTime.plusDays(1).minusMinutes(1)
    }
    endTime.toInstant
  }

  def saveCheck(): View = {
    val plan = populateEntity(classOf[DepartPlan], "departPlan")
    Stage.values foreach { stage =>
      if (stage.subCount == 0) {
        getInstant(s"${stage.id}_beginOn") foreach { beginOn =>
          addTime(plan, stage, beginOn, getEndAt(stage))
        }
      }
    }
    if plan.status != PlanStatus.审查通过 then plan.status = PlanStatus.待学校审查
    entityDao.saveOrUpdate(plan)
    redirect("search", "plan.season.id=" + plan.thesisPlan.season.id.toString, "操作成功")
  }

  def submit(): View = {
    val plan = populateEntity(classOf[DepartPlan], "plan")
    Stage.values foreach { stage =>
      if (stage.subCount == 0) {
        plan.addTime(stage, getInstant(s"${stage.id}_beginOn").orNull, getEndAt(stage))
      }
    }
    if plan.status != PlanStatus.审查通过 then plan.status = PlanStatus.待学校审查
    entityDao.saveOrUpdate(plan)
    redirect("info", "id=" + plan.id, "info.save.success")
  }

  def cloneLast(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("plan.season"))
    val q = OqlBuilder.from(classOf[ThesisPlan], "plan")
    q.where("plan.season.graduateIn < :graduateIn", season.graduateIn)
    q.orderBy("plan.season.graduateIn desc")

    val lastPlan = entityDao.first(q)
    if (lastPlan.nonEmpty) {
      val last = lastPlan.get
      val duration = Duration.between(last.season.graduateIn.atDay(1).atTime(LocalTime.MIN), season.graduateIn.atDay(1).atTime(LocalTime.MIN))
      val plan = new ThesisPlan
      plan.season = season
      plan.project = last.project
      last.times foreach { time =>
        plan.times.addOne(new StageTime(time.stage, time.beginAt.plus(duration), time.endAt.plus(duration)))
      }
      plan.beginOn = plan.times.map(_.beginAt).min.atZone(ZoneId.systemDefault()).toLocalDate
      plan.endOn = plan.times.map(_.endAt).max.atZone(ZoneId.systemDefault()).toLocalDate

      val departQuery = OqlBuilder.from[Department](classOf[Writer].getName, "w")
      departQuery.where("w.season=:season", plan.season)
      departQuery.select("distinct w.std.state.department")
      val departs = entityDao.search(departQuery)
      departs foreach { depart =>
        val dp = new DepartPlan
        dp.department = depart
        dp.thesisPlan = plan
        dp.status = PlanStatus.待学院修改
        plan.times.foreach { x =>
          dp.addTime(x.stage, x.beginAt, x.endAt)
        }
        plan.departPlans.addOne(dp)
      }
      entityDao.saveOrUpdate(plan)

      redirect("search", s"从${last.season.name}复制成功，请检查调整")
    } else {
      redirect("search", "找不到上届的时间设置")
    }
  }
}
