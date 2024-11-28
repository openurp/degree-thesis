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
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.support.action.EntityAction
import org.beangle.webmvc.view.View
import org.openurp.base.model.{AuditStatus, Project}
import org.openurp.degree.thesis.model.SubjectApply.{Final, Round1, Round2}
import org.openurp.degree.thesis.model.{Subject, SubjectApply, Writer}
import org.openurp.degree.thesis.service.ThesisPlanService
import org.openurp.starter.web.support.ProjectSupport

import scala.collection.mutable
import scala.util.Random

class SubjectApplyAction extends ActionSupport, EntityAction[SubjectApply], DepartSupport, ProjectSupport {
  var entityDao: EntityDao = _

  var businessLogger: WebBusinessLogger = _

  var thesisPlanService: ThesisPlanService = _

  def index(): View = {
    forward()
  }

  def search(): View = {
    given project: Project = getProject

    val departs = thesisPlanService.getPlanDeparts(getDeparts)
    val season = thesisPlanService.getPlan().get.season
    val query = OqlBuilder.from(classOf[SubjectApply], "apply")
    query.where("apply.writer.season=:season", season)
    val round = getInt("round", Round1)
    val departMatched = getBoolean("departMatched")
    round match {
      case Round1 =>
        query.where("apply.first.depart in(:departs)", departs)
          .orderBy("apply.first.advisor.teacher.name,first.name,apply.writer.std.code")
        if (departMatched.nonEmpty && !departMatched.get)
          query.where("apply.writer.std.state.department not in elements(apply.first.advisor.departs)")
      case Round2 =>
        query.where("apply.second.depart in(:departs)", departs)
          .orderBy("apply.second.advisor.teacher.name,second.name,apply.writer.std.code")
      case Final =>
        query.where("apply.last.depart in(:departs)", departs)
          .orderBy("writer.std.state.major.id,apply.writer.std.code")
    }
    put("round", round)
    query.orderBy("apply.writer.std.state.department.code,apply.writer.std.code")
    populateConditions(query)
    put("applies", entityDao.search(query))
    if round == Final then forward("lastResults") else forward()
  }

  def absence(): View = {
    given project: Project = getProject

    val departs = getDeparts
    val season = thesisPlanService.getPlan().get.season
    val round = getInt("round", Round1)
    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.season=:season", season)
    query.where("writer.std.state.department in(:departs)", departs)
    query.orderBy("writer.std.code")

    round match {
      case Round1 =>
        query.where("not exists(from " + classOf[SubjectApply].getName +
          " apply where apply.writer=writer and apply.first is not null)")
      case Round2 =>
        query.where("not exists(from " + classOf[SubjectApply].getName +
            " apply where apply.writer=writer and apply.second is not null)")
          .where("not exists(from " + classOf[SubjectApply].getName +
            " apply where apply.writer=writer and apply.last is not null)")
      case Final => query.where("not exists(from " + classOf[SubjectApply].getName +
        " apply where apply.writer=writer and apply.last is not null)")
    }
    put("round", round)
    put("writers", entityDao.search(query))
    if round == Final then forward("lastAbsence") else forward()
  }

  def remainder(): View = {
    given project: Project = getProject

    val departs = getDeparts
    val season = thesisPlanService.getPlan().get.season
    val query = OqlBuilder.from(classOf[Subject], "s")
    query.where("s.season=:season", season)
    query.where("s.depart in(:departs) and s.status=:status", departs, AuditStatus.Passed)
    query.where("not exists(from " + classOf[SubjectApply].getName + " apply where apply.last=s)")
    query.orderBy("s.depart.code,s.advisor.teacher.staff.code,s.name")
    val subjects = entityDao.search(query)
    put("subjects", subjects)

    val query2 = OqlBuilder.from(classOf[Writer], "writer")
    query2.where("writer.std.state.department in(:departs) ", departs)
    query2.where("writer.season=:season", season)
    query2.where("not exists(from " + classOf[SubjectApply].getName + " sa where sa.writer=writer and sa.last is not null)")
    val applies = entityDao.search(query2)
    val writerMap = applies.groupBy(x => x.department).map(x => (x._1, x._2.sortBy(_.code)))
    put("writerMap", writerMap)
    forward()
  }

  def assignSetting(): View = {
    given project: Project = getProject

    val plan = thesisPlanService.getPlan().get
    val departs = getDeparts
    val writer = entityDao.get(classOf[Writer], getLongId("writer"))
    val query = OqlBuilder.from(classOf[Subject], "s")
    query.where("s.season=:season", plan.season)
    query.where(":major in elements(s.majors)", writer.major)
    query.where("s.depart =:depart and s.status=:status", writer.department, AuditStatus.Passed)
    query.where("not exists(from " + classOf[SubjectApply].getName + " apply where apply.last=s)")
    val subjects = entityDao.search(query)

    put("writer", writer)
    put("subjects", subjects)
    forward()
  }

  def choose(): View = {
    val round = getInt("round", Round1)
    val apply = entityDao.get(classOf[SubjectApply], getLongId("subjectApply"))
    val subject = entityDao.get(classOf[Subject], getLongId("subject"))
    val exists = entityDao.findBy(classOf[SubjectApply], "last", subject)
    if (exists.isEmpty) {
      val writer = apply.writer
      apply.last = Some(subject)
      apply.writer.thesisTitle = Some(subject.name)
      apply.writer.researchField = subject.researchField
      apply.writer.advisor = Some(subject.advisor)
      entityDao.saveOrUpdate(apply, apply.writer)
      val msg = s"论文题目指定：通过了${writer.std.name}的论文题目：${subject.name}"
      businessLogger.info(msg, apply.id, Map("writer.std.code" -> writer.std.code, "writer.advisor.teacher.name" -> subject.advisor.teacher.name))
      redirect("search", "round=" + round, "选题成功")
    } else {
      redirect("search", "round=" + round, "该题目已经有人选择")
    }
  }

  def cancel(): View = {
    val round = getInt("round", Round1)
    val apply = entityDao.get(classOf[SubjectApply], getLongId("subjectApply"))
    val writer = apply.writer
    apply.last match {
      case None =>
        redirect("search", "round=" + round, "尚未确定，无需取消")
      case Some(subject) =>
        apply.last = None
        writer.thesisTitle = None
        writer.researchField = None
        writer.advisor = None
        entityDao.saveOrUpdate(apply, apply.writer)
        val msg = s"论文题目指定：取消了${writer.std.name}的论文题目"
        businessLogger.info(msg, apply.id, Map("writer.std.code" -> writer.std.code, "writer.advisor.teacher.name" -> subject.advisor.teacher.name))
        redirect("search", "round=" + round, "取消选题成功")
    }
  }

  def assign(): View = {
    val writer = entityDao.get(classOf[Writer], getLongId("writer"))
    val query = OqlBuilder.from(classOf[SubjectApply], "apply")
    query.where("apply.writer=:writer", writer)
    val applyOption = entityDao.search(query).headOption

    getLong("subjectId") match {
      case None => applyOption foreach { apply => apply.last = None; entityDao.saveOrUpdate(apply) }
      case Some(subjectId) =>
        val subject = entityDao.get(classOf[Subject], subjectId)
        val exists = entityDao.findBy(classOf[SubjectApply], "last", subject)
        if (exists.nonEmpty) {
          return redirect("assignSetting", "writer.id=" + writer.id, "该题目已经有人选择")
        }
        if (!subject.majors.contains(writer.major)) {
          return redirect("assignSetting", "writer.id=" + writer.id, "专业不匹配")
        }
        val apply = applyOption match {
          case Some(apply) => apply.last = Some(subject); apply
          case None => new SubjectApply(writer, subject)
        }
        writer.thesisTitle = Some(subject.name)
        writer.researchField = subject.researchField
        writer.advisor = Some(subject.advisor)
        entityDao.saveOrUpdate(apply, writer)
        val msg = s"论文题目指定：通过了${writer.std.name}的论文题目：${subject.name}"
        businessLogger.info(msg, apply.id, Map("writer.std.code" -> writer.std.code, "writer.advisor.teacher.name" -> subject.advisor.teacher.name))
    }
    val next = get("next", "absence")
    redirect(next, "round=" + Final, "info.save.success")
  }

  def shuffleSetting(): View = {
    given project: Project = getProject

    val season = thesisPlanService.getPlan().get.season
    val departs = thesisPlanService.getPlanDeparts(getDeparts)

    //自动产生空的申请记录
    val noApplyWriterQuery = OqlBuilder.from(classOf[Writer], "w")
    noApplyWriterQuery.where("w.season = :season and w.std.state.department in(:departs)", season, departs)
    noApplyWriterQuery.where("not exists(from " + classOf[SubjectApply].getName + " sa where sa.writer=w)")
    val noApplyWriters = entityDao.search(noApplyWriterQuery)
    val generateApplies = noApplyWriters.map { x =>
      val apply = new SubjectApply()
      apply.writer = x
      apply.currentRound = Round1
      apply
    }
    if (generateApplies.nonEmpty) entityDao.saveOrUpdate(generateApplies)

    val subjectQuery = OqlBuilder.from[Array[Any]](classOf[Subject].getName, "s")
    subjectQuery.where("s.season = :season and s.depart in(:departs)", season, departs)
    subjectQuery.where("not exists(from " + classOf[SubjectApply].getName + " apply where apply.last=s)")
    subjectQuery.select("s.depart.id,count(*)")
    subjectQuery.groupBy("s.depart.id")
    val freeSubjectCountMap = entityDao.search(subjectQuery).map(x => (x(0).toString, x(1))).toMap

    val rounds = Set(Round1, Round2, Final)
    val needShuffledMap = new mutable.HashMap[String, Map[String, Any]]
    rounds foreach { round =>
      val applyQuery = OqlBuilder.from[Array[Any]](classOf[SubjectApply].getName, "apply")
      applyQuery.where("apply.writer.season = :season and apply.writer.std.state.department in(:departs)", season, departs)
      applyQuery.where("apply.last is null")
      round match {
        case Round1 => applyQuery.where("apply.first is not null")
        case Round2 => applyQuery.where("apply.second is not null")
        case Final =>
      }
      applyQuery.select("apply.writer.std.state.department.id,count(*)")
      applyQuery.groupBy("apply.writer.std.state.department.id")
      val count = entityDao.search(applyQuery).map(x => (x(0).toString, x(1))).toMap
      needShuffledMap.put(round.toString, count)
    }

    put("freeSubjectCountMap", freeSubjectCountMap)
    put("needShuffledMap", needShuffledMap)
    put("departs", departs)
    forward()
  }

  def shuffle(): View = {
    given project: Project = getProject

    val departs = getDeparts
    val season = thesisPlanService.getPlan().get.season
    val round = getInt("round", Round1)
    val departId = getInt("departId", 0)
    val applyQuery = OqlBuilder.from(classOf[SubjectApply], "apply")
    applyQuery.where("apply.writer.season=:season", season)
    applyQuery.where("apply.writer.std.state.department in(:departs)", departs)
    applyQuery.where("apply.writer.std.state.department.id =:departId", departId)
    applyQuery.where("apply.last is null")
    round match {
      case Round1 => applyQuery.where("apply.first is not null")
      case Round2 => applyQuery.where("apply.second is not null")
      case Final => applyQuery.where("apply.last is null")
    }
    val applies = Random.shuffle(entityDao.search(applyQuery))
    val subjectQuery = OqlBuilder.from(classOf[Subject], "s")
    subjectQuery.where("s.season=:season", season)
    subjectQuery.where("s.depart in(:departs)", departs)
    subjectQuery.where("s.depart.id = :departId", departId)
    subjectQuery.where("not exists(from " + classOf[SubjectApply].getName + " apply where apply.last=s)")
    val freeSubjects = new mutable.HashSet[Subject]().addAll(entityDao.search(subjectQuery))
    round match {
      case Round1 =>
        applies foreach { apply =>
          if (freeSubjects.contains(apply.first.get)) {
            choose(apply, apply.first.get, SubjectApply.Round1) //表示第一轮抽中，无需进入第二轮
            freeSubjects.subtractAll(apply.last)
          } else {
            apply.currentRound = SubjectApply.Round2
          }
        }
      case Round2 =>
        applies foreach { apply =>
          if (freeSubjects.contains(apply.second.get)) {
            choose(apply, apply.second.get, SubjectApply.Round2) //表示第二轮抽中
            freeSubjects.subtractAll(apply.last)
          } else {
            apply.currentRound = SubjectApply.Final
          }
        }
      case Final =>
        applies foreach { apply =>
          val majorMatched = freeSubjects.filter(s => s.majors.contains(apply.writer.std.major))
          if (majorMatched.nonEmpty) {
            val randomOne = majorMatched.head
            choose(apply, randomOne, SubjectApply.Final)
            freeSubjects.subtractOne(randomOne)
          }
        }
    }
    entityDao.saveOrUpdate(applies)
    redirect("shuffleSetting", "抽签完成")
  }

  def choose(apply: SubjectApply, subject: Subject, round: Int): Unit = {
    apply.last = Some(subject)
    apply.currentRound = round
    val writer = apply.writer
    writer.thesisTitle = apply.last.map(_.name)
    writer.researchField = apply.last.flatMap(_.researchField)
    writer.advisor = Some(apply.last.get.advisor)
  }
}
