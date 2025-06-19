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

import org.beangle.data.dao.OqlBuilder
import org.beangle.security.Securities
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.EntityAction
import org.openurp.base.edu.model.Major
import org.openurp.base.model.AuditStatus
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.SubjectService

import java.time.Instant
import scala.collection.mutable

class SubjectAction extends AdvisorSupport, EntityAction[Subject] {

  var subjectService: SubjectService = _

  def index(): View = {
    forward()
  }

  def search(): View = {
    put("subjects", entityDao.search(getQueryBuilder))
    forward()
  }

  override def getQueryBuilder: OqlBuilder[Subject] = {
    val advisor = getAdvisor

    put("SubjectStage", Stage.Subject)
    val query = super.getQueryBuilder
    thesisPlanService.getPlan() foreach { plan =>
      query.where("subject.season=:season", plan.season)
      val departPlans = plan.departPlans.filter(x => advisor.departs.contains(x.department))
      put("departPlans", departPlans)
      put("openedDeparts", departPlans.filter(x => x.getStageTime(Stage.Subject).timeSuitable(Instant.now) == 0).map(_.department))
    }
    query.where("subject.advisor =:me", advisor)
    query.limit(null)
  }

  def others(): View = {
    val advisor = getAdvisor
    val query = OqlBuilder.from(classOf[Subject], "subject")
    query.where("subject.depart in(:departs) and subject.advisor != :me", advisor.departs, advisor)
    query.where("subject.status=:passed", AuditStatus.Passed)
    thesisPlanService.getPlan() foreach { plan =>
      query.where("subject.season=:season", plan.season)
    }
    val subjects = entityDao.search(query)
    put("subjects", subjects)
    forward()
  }

  def history(): View = {
    val js = getAdvisor
    val plan = thesisPlanService.getPlan().get
    val query = OqlBuilder.from(classOf[Subject], "subject").where("subject.advisor=:me", js)
    query.where("subject.season.graduateIn<:graduateIn", plan.season.graduateIn)
    query.where("not exists(from " + classOf[Subject].getName +
      " s2 where s2.name=subject.name and s2.advisor=subject.advisor and s2.season.graduateOn > subject.season.graduateOn)")
    query.orderBy("subject.season.graduateOn desc")
    val lstmList = entityDao.search(query)
    put("lstmList", lstmList)

    put("majors", subjectService.getMajors(plan.season, js.departs))
    forward()
  }

  def addFromHistory(): View = {
    val js = getAdvisor
    val majors = entityDao.find(classOf[Major], getAll("majorId", classOf[Long]))
    val closedDeparts = new mutable.ArrayBuffer[String]
    val plan = thesisPlanService.getPlan().get
    getAll("lstmId") foreach { id =>
      val lstm = entityDao.get(classOf[Subject], id.toString.toLong)
      val ksList = entityDao.findBy(classOf[Subject], "season" -> plan.season, "name" -> lstm.name)
      if (ksList.isEmpty) {
        if (thesisPlanService.isOpen(plan, lstm.depart, Stage.Subject)) {
          val kt = new Subject
          kt.advisor = js
          if (js.departs.contains(lstm.depart)) {
            kt.depart = lstm.depart
          } else {
            kt.depart = js.departs.head
          }
          kt.name = lstm.name
          kt.contents = lstm.contents
          kt.status = AuditStatus.Draft
          kt.season = plan.season
          kt.majors ++= majors
          entityDao.saveOrUpdate(kt)
        } else {
          closedDeparts.addOne(lstm.depart.name)
        }
      }
    }
    if (closedDeparts.nonEmpty) redirect("search", s"${closedDeparts.mkString(",")}的开题已经关闭，添加失败")
    else redirect("search", "info.save.success")
  }

  @mapping(method = "post")
  def save(): View = {
    val subject = populateEntity()
    val plan = thesisPlanService.getPlan().get
    if (null == subject.season || !subject.season.persisted) {
      subject.season = plan.season
    }
    if (thesisPlanService.isOpen(plan, subject.depart, Stage.Subject)) {
      if(isDuplicated(subject.id, subject.season.id, subject.name)){
        redirect("search", "题目重复")
      }else{
        val majors = entityDao.find(classOf[Major], getAll("majorId", classOf[Long]))
        subject.majors.clear()
        subject.majors ++= majors
        subject.advisor = getAdvisor
        subject.status = AuditStatus.Submited
        saveOrUpdate(subject)
        redirect("search", "info.save.success")
      }
    } else {
      redirect("search", "开题已经关闭")
    }
  }

  @response
  def checkName(@param("subject_id") id: Long, @param("season_id") seasonId:Long,@param("subject.name") mc: String): Boolean = {
    !isDuplicated(id,seasonId,mc)
  }

  private def isDuplicated(subjectId:Long,seasonId:Long,name:String):Boolean={
    val query = OqlBuilder.from(classOf[Subject], "subject")
      .where("subject.season.id = :seasonId", seasonId)
      .where("subject.id != :id and subject.name = :name", subjectId, name)
    entityDao.search(query).nonEmpty
  }

  private def checkTime(plans: Option[DepartPlan]): Option[View] = {
    if (plans.isEmpty) return Some(forward("not_intime"))
    val plan = plans.get

    val today = Instant.now
    val stageTime = plan.getStageTime(Stage.Subject)
    if (stageTime.timeSuitable(today) != 0) {
      put("begin", stageTime.beginOn)
      put("end", stageTime.endOn)
      return Some(forward("not_intime"))
    }
    None
  }

  @mapping(value = "{id}/edit")
  def edit(@param("id") id: String): View = {
    val subject = entityDao.get(classOf[Subject], id.toLong)
    val js = getAdvisor
    put("majors", subjectService.getMajors(subject.season, js.departs))
    put("departs", js.departs)
    if (subject.persisted) {
      if (subject.advisor.teacher.code != Securities.user) {
        throw new RuntimeException("Cannot edit other data")
      }
    }
    put("subject", subject)
    forward()
  }

  @mapping(value = "new", view = "new,form")
  def editNew(): View = {
    val plan = thesisPlanService.getPlan()
    val subject = new Subject()
    subject.season = plan.get.season
    put("subject", subject)
    val js = getAdvisor
    put("majors", subjectService.getMajors(plan.get.season, js.departs))
    put("departs", js.departs)
    val advisor = getAdvisor
    forward()
  }

  @mapping(method = "delete")
  def remove(): View = {
    val subject = entityDao.get(classOf[Subject], getLongId("subject"))
    if (subject.advisor.code == Securities.user) {
      entityDao.remove(subject)
    }
    redirect("search", "删除成功")
  }

  @mapping(value = "{id}")
  def info(@param("id") id: String): View = {
    put("subject", entityDao.get(classOf[Subject], id.toLong))
    forward()
  }

  def applies(): View = {
    val advisor = getAdvisor
    val applies = entityDao.findBy(classOf[SubjectApply], "last.advisor", advisor)
    put("applies", applies)
    forward()
  }

}
