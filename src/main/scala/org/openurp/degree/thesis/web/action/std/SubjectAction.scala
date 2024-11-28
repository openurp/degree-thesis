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
import org.openurp.degree.thesis.model.SubjectApply.*

import java.time.Instant

class SubjectAction extends WriterSupport {

  def index(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    put("plan", plan)
    put("writer", writer)

    if (writer.mobile.isEmpty) {
      forward("contact")
    } else {
      put("StageRound1", Stage.SubjectChosenRound1)
      put("StageRound2", Stage.SubjectChosenRound2)

      val query = OqlBuilder.from(classOf[SubjectApply], "c")
      query.where("c.writer=:me", writer)
      val apply = entityDao.first(query)
      put("apply", apply)

      val query2 = OqlBuilder.from(classOf[Proposal], "p")
      query2.where("p.writer=:me", writer)
      put("hasProposal", entityDao.first(query2).nonEmpty)
      forward()
    }
  }

  private def checkTime(round: Int, plans: Option[DepartPlan]): Option[View] = {
    if (plans.isEmpty) return Some(forward("not_intime"))
    val plan = plans.get

    val today = Instant.now
    round match {
      case Round1 =>
        val stageTime = plan.getStageTime(Stage.SubjectChosenRound1)
        if (stageTime.timeSuitable(today) != 0) {
          put("begin", stageTime.beginOn)
          put("end", stageTime.endOn)
          return Some(forward("not_intime"))
        }
      case Round2 =>
        val stageTime = plan.getStageTime(Stage.SubjectChosenRound2)
        if (stageTime.timeSuitable(today) != 0) {
          put("begin", stageTime.beginOn)
          put("end", stageTime.endOn)
          return Some(forward("not_intime"))
        }
    }
    None
  }

  def candinates(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer)
    if (plan.isEmpty) {
      forward("not_intime")
    } else {
      val season = plan.head.thesisPlan.season
      put("plan", plan)
      val round = getInt("round", Round1)
      checkTime(round, plan) foreach { v => return v }
      val subjectQuery = OqlBuilder.from(classOf[Subject], "s")
      subjectQuery.where("s.depart=:depart", writer.department)
      subjectQuery.where("s.status = :status", AuditStatus.Passed)
      subjectQuery.where(":major in elements(s.majors)", writer.major)
      subjectQuery.where("s.season=:season", season)
      subjectQuery.where("not exists(from " + classOf[SubjectApply].getName + " apply where apply.last=s)")
      val candinates = entityDao.search(subjectQuery)
      put("candinates", candinates)
      val stat = OqlBuilder.from[Array[Any]](classOf[SubjectApply].getName, "apply")
      stat.where("apply.writer.season=:season", season)
      round match {
        case Round1 => stat.where("apply.first is not null").groupBy("apply.first.id").select("apply.first.id,count(*)")
        case Round2 => stat.where("apply.second is not null").groupBy("apply.second.id").select("apply.second.id,count(*)")
        case Final => stat.where("apply.last is not null").groupBy("apply.last.id").select("apply.last.id,count(*)")
        case _ => stat.where("1=0")
      }
      val stats = entityDao.search(stat).map(x => x(0) -> x(1)).toMap
      put("applyStats", stats)
      forward(if round == Round1 then "candinates_cx" else "candinates_bx")
    }
  }

  def applies(): View = {
    val round = getInt("round", SubjectApply.Round1)
    val query = OqlBuilder.from(classOf[SubjectApply], "apply")
    val subjectId = getLong("subjectId").getOrElse(0l)
    round match {
      case Round1 => query.where("apply.first.id=:subjectId", subjectId)
      case Round2 => query.where("apply.second.id=:subjectId", subjectId)
      case Final => query.where("apply.last.id=:subjectId", subjectId)
      case _ => query.where("1=0")
    }
    put("applies", entityDao.search(query))
    forward()
  }

  def doApply(): View = {
    val writer = getWriter
    val round = getInt("round", SubjectApply.Round1)
    val plans = thesisPlanService.getDepartPlan(writer)
    checkTime(round, plans) foreach { v => return v }
    val query = OqlBuilder.from(classOf[SubjectApply], "xz")
    query.where("xz.writer=:me", writer)
    val apply = entityDao.search(query).headOption match {
      case Some(apply) =>
        if apply.currentRound == 0 then apply.currentRound = round
        apply
      case None =>
        val ap = new SubjectApply()
        ap.currentRound = round
        ap
    }

    val subjectId = getLong("subjectId").getOrElse(0l)
    val subject = entityDao.get(classOf[Subject], subjectId)
    if (apply.last.isEmpty) {
      apply.writer = writer
      if round == Round1 then apply.first = Some(subject)
      else apply.second = Some(subject)
    }
    entityDao.saveOrUpdate(apply)
    redirect("index", "info.save.success")
  }

  def edit(): View = {
    val writer = getWriter
    put("writer", writer)
    val apply = entityDao.findBy(classOf[SubjectApply], "writer", writer)
    apply foreach { a =>
      put("subject", a.last)
    }
    forward()
  }

  def save(): View = {
    val writer = getWriter
    get("thesisTitle") match {
      case None =>
      case Some(newTitle) =>
        val oldTitle = writer.thesisTitle.getOrElse("--")
        if (oldTitle != newTitle) {
          val sameTitles = entityDao.findBy(classOf[Writer], "season" -> writer.season, "thesisTitle" -> newTitle)
          if (sameTitles.nonEmpty) return redirect("index", "论文题目重复")
        }
        writer.thesisTitle = Some(newTitle)

        val field = get("researchField")
        field foreach { f => writer.researchField = field }
        val papers = entityDao.findBy(classOf[ThesisPaper], "writer", writer)
        if (papers.size == 1) {
          papers.foreach { p =>
            p.title = newTitle
            if (field.nonEmpty) p.researchField = field
          }
        }
        entityDao.saveOrUpdate(writer)
        entityDao.saveOrUpdate(papers)
        val msg = s"修改论文题目:改为${newTitle}"
        businessLogger.info(msg, writer.id, Map("oldTitle" -> oldTitle))
    }
    redirect("index", "题目修改成功")
  }

  def saveProfile(): View = {
    val writer = getWriter
    writer.mobile = get("mobile")
    writer.email = get("email")
    entityDao.saveOrUpdate(writer)
    redirect("index", "保存成功")
  }
}
