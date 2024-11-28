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

import org.beangle.commons.activation.MediaTypes
import org.beangle.data.dao.OqlBuilder
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.view.{Stream, View}
import org.openurp.base.hr.model.Teacher
import org.openurp.base.model.User
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.DefenseGroupService
import org.openurp.degree.thesis.service.doc.DefenseReport
import org.openurp.degree.thesis.web.helper.{PaperDownloadHelper, ScoreTextHelper}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.time.Instant
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class DefenseAction extends AdvisorSupport {
  var groupService: DefenseGroupService = _

  def index(): View = {
    val advisor = getAdvisor
    val myGroups = groupService.getGroups(advisor.teacher, thesisPlanService.getSeason())
    put("myGroups", myGroups)

    val groupReviews = myGroups.map { group =>
      val reviews =
        if group.writers.isEmpty then Map.empty[Writer, ThesisReview]
        else entityDao.findBy(classOf[ThesisReview], "writer", group.orderedWriters).map(x => (x.writer, x)).toMap
      (group, reviews)
    }.toMap

    put("myGroupReviews", groupReviews)
    val manageGroups = myGroups.filter(g => groupService.isGroupManager(g, advisor.teacher))
    put("manageGroups", manageGroups)
    put("isOfficeHead", groupService.isOfficeHead(advisor))
    forward()
  }

  @mapping(value = "{id}")
  def info(@param("id") id: String): View = {
    val defenseGroup = entityDao.get(classOf[DefenseGroup], id.toLong)
    put("defenseGroup", defenseGroup)
    put("writers", defenseGroup.orderedWriters)
    forward()
  }

  /** 显示本教研室的教师或者本教研室负责的答辩组中涉及到的教师
   *
   * @return
   */
  def advisors(): View = {
    val advisor = getAdvisor
    advisor.teacher.office.foreach { office =>
      val query = OqlBuilder.from(classOf[Advisor], "advisor")
      query.where("advisor.teacher.office=:office", office)
      val advisors = entityDao.search(query).map(x => x.teacher)

      val query2 = OqlBuilder.from(classOf[DefenseMember], "dm")
      query2.where("dm.group.office=:office", office)
      val members = entityDao.search(query2)
      val teachers = new mutable.ArrayBuffer[Teacher]
      teachers ++= advisors
      val groups = new mutable.HashMap[Teacher, mutable.ArrayBuffer[DefenseGroup]]
      members foreach { m =>
        if !teachers.contains(m.teacher) then teachers += m.teacher
        val userGroups = groups.getOrElseUpdate(m.teacher, new ArrayBuffer[DefenseGroup])
        userGroups += m.group
      }

      put("office", office)
      put("users", teachers)
      put("groups", groups)
    }
    forward()
  }

  /** 显示本教研室的教师所带学生的分组情况
   *
   * @return
   */
  def writers(): View = {
    val advisor = getAdvisor
    val season = thesisPlanService.getSeason()
    advisor.teacher.office.foreach { office =>
      val query = OqlBuilder.from(classOf[DefenseGroup], "dg")
      query.where("dg.season=:season", season)
      query.where("dg.office=:office", office)
      val groups = entityDao.search(query)
      val writerGroups = new mutable.HashMap[Writer, DefenseGroup]
      for (group <- groups; writer <- group.writers) {
        writerGroups.put(writer.writer, group)
      }
      val query2 = OqlBuilder.from(classOf[Writer], "writer")
      query2.where("writer.advisor.teacher.office=:office", office)
      val writers = entityDao.search(query2)
      val writers2 = writers.partition(writerGroups.contains)
      put("groups", writerGroups)
      val inGroupWriters = writers2._1.sortBy(x => writerGroups(x).idx.toString + x.advisor.get.code + x.code)
      put("inGroupWriters", inGroupWriters)
      put("noGroupWriters", writers2._2)
    }
    forward()
  }

  /** 编辑答辩组
   *
   * @param id
   * @return
   */
  @mapping(value = "{id}/edit")
  def edit(@param("id") id: String): View = {
    val defenseGroup = entityDao.get(classOf[DefenseGroup], id.toLong)
    put("defenseGroup", defenseGroup)
    put("writers", defenseGroup.orderedWriters)
    val query = OqlBuilder.from[Teacher](classOf[Advisor].getName, "advisor")
    query.orderBy("advisor.teacher.department.name,advisor.teacher.name")
    query.select("advisor.teacher")
    val teachers = entityDao.search(query).toBuffer
    val writerAdvisors = defenseGroup.writers.filter(_.writer.advisor.nonEmpty).map(_.writer.advisor.get.teacher).toSet
    teachers.subtractAll(writerAdvisors)

    //将答辩组所在学院的教师放在头部
    val myDeparts = teachers.filter(x => x.department == defenseGroup.department)
    teachers.subtractAll(myDeparts)
    teachers.prependAll(myDeparts)

    val secretaries = entityDao.findBy(classOf[User], "school" -> defenseGroup.season.project.school, "code" -> myDeparts.map(_.code))
    put("secretaries", secretaries)
    put("advisors", teachers)
    forward()
  }

  @mapping(value = "new", view = "new,form")
  def editNew(): View = {
    val advisor = getAdvisor
    val season = thesisPlanService.getPlan().get.season
    val defenseGroup = new DefenseGroup(season, advisor.teacher.department)
    put("defenseGroup", defenseGroup)
    val query = OqlBuilder.from(classOf[Advisor], "advisor")
    query.orderBy("advisor.teacher.department.name,advisor.teacher.name")
    val advisors = entityDao.search(query).toBuffer
    val writerAdvisors = defenseGroup.writers.flatMap(_.writer.advisor)
    advisors.subtractAll(writerAdvisors)

    //将答辩组所在学院的教师放在头部
    val myDeparts = advisors.filter(x => x.teacher.department == defenseGroup.department)
    advisors.subtractAll(myDeparts)
    advisors.prependAll(myDeparts)

    put("myDepartAdvisors", myDeparts)
    put("advisors", advisors)
    forward()
  }

  @mapping(method = "delete")
  def remove(): View = {
    val advisor = getAdvisor
    val defenseGroup = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    if groupService.isGroupAdmin(defenseGroup, advisor) then entityDao.remove(defenseGroup)
    redirect("index", "info.remove.success")
  }

  def removeAdvisor(): View = {
    val advisor = getAdvisor
    val group = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    if (groupService.isGroupAdmin(group, advisor)) {
      val removed = entityDao.get(classOf[DefenseMember], getLong("defenseMember.id").get)
      group.members.subtractOne(removed)
      entityDao.saveOrUpdate(group)
    }
    redirect("edit", s"id=${group.id}", "info.remove.success")
  }

  /** 保存答辩组
   *
   * @return
   */
  def save(): View = {
    val advisor = getAdvisor
    val season = thesisPlanService.getPlan().get.season
    val group = getLong("defenseGroup.id") match {
      case None => new DefenseGroup(season, advisor.teacher.department)
      case Some(id) => entityDao.get(classOf[DefenseGroup], id)
    }
    group.office = advisor.teacher.office
    group.place = get("defenseGroup.place")
    group.beginAt = getInstant("defenseGroup.beginAt")
    group.endAt = getInstant("defenseGroup.endAt")
    group.secretary = getLong("defenseGroup.secretary.id").map(x => entityDao.get(classOf[User], x))
    group.idx = getInt("defenseGroup.idx", 1)
    entityDao.saveOrUpdate(group)
    redirect("index", "info.save.success")
  }

  def removeWriter(): View = {
    val advisor = getAdvisor
    val defenseGroup = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    if (groupService.isGroupAdmin(defenseGroup, advisor) && !defenseGroup.published) {
      val removed = entityDao.get(classOf[Writer], getLong("writer.id").get)
      defenseGroup.removeWriter(List(removed))
      entityDao.saveOrUpdate(defenseGroup)
      val msg = s"从第${defenseGroup.idx}组中移除${removed.std.name}"
      businessLogger.info(msg, defenseGroup.id, Map("writer.std.code" -> removed.std.code, "group.id" -> defenseGroup.id.toString))
    }
    redirect("edit", s"id=${defenseGroup.id}", "info.remove.success")
  }

  def addWriterList(): View = {
    val advisor = getAdvisor
    val group = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    put("defenseGroup", group)
    if (groupService.isGroupAdmin(group, advisor)) {
      val advisors = group.members.map(_.teacher)
      val query = OqlBuilder.from(classOf[Writer], "writer")
      query.where("writer.advisor.teacher.office=:office", advisor.teacher.office.get)
      if advisors.nonEmpty then query.where("writer.advisor.teacher not in(:advisors)", advisors)
      query.where("not exists(from " + classOf[DefenseGroup].getName + " dg join dg.writers as wr where wr.writer=writer)")
      val writers = entityDao.search(query)
      put("writers", writers)
    } else {
      put("writers", List.empty[Writer])
    }
    forward()
  }

  def addWriter(): View = {
    val advisor = getAdvisor
    val defenseGroup = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    if (groupService.isGroupAdmin(defenseGroup, advisor)) {
      val advisors = defenseGroup.members.map(_.teacher).toSet
      val added = entityDao.find(classOf[Writer], getAll("writer.id", classOf[Long]))
      added foreach { add =>
        if (!advisors.contains(add.advisor.get.teacher))
          defenseGroup.addWriters(Set(add))
      }
      entityDao.saveOrUpdate(defenseGroup)
    }
    redirect("edit", s"id=${defenseGroup.id}", "info.remove.success")
  }

  def autoAddWriter(): View = {
    val advisor = getAdvisor
    val newCount = getInt("newCount", 0)
    val defenseGroup = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    if (defenseGroup.office.contains(advisor.teacher.office.get) && newCount > 0) {
      val advisors = defenseGroup.members.map(_.teacher)
      val query = OqlBuilder.from(classOf[Writer], "writer")
      query.where("writer.advisor.teacher.office=:office", advisor.teacher.office.get)
      if advisors.nonEmpty then query.where("writer.advisor.teacher not in(:advisors)", advisors)
      query.where("not exists(from " + classOf[DefenseGroup].getName + " dg join dg.writers as wr where wr.writer=writer)")
      val writers = entityDao.search(query)
      var i = 0
      val writerIter = writers.iterator
      while (i < newCount && writerIter.hasNext) {
        val writer = writerIter.next()
        defenseGroup.addWriters(Set(writer))
        i += 1
      }
      redirect("edit", "id=" + defenseGroup.id, s"成功添加了${i}名学生")
    } else redirect("edit", "id=" + defenseGroup.id, "成功添加了0名学生")
  }

  def notices(): View = {
    val advisor = getAdvisor
    val season = thesisPlanService.getSeason()
    val query = OqlBuilder.from(classOf[DefenseGroup], "dg")
    query.where("dg.season=:season", season)
    query.join("dg.members", "member")
    query.where("member.teacher=:me and member.leader=true", advisor.teacher)
    val groups = entityDao.search(query)
    val notices = groups.flatMap(g => g.notices)
    put("notices", notices)
    forward()
  }

  def removeNotice(): View = {
    val advisor = getAdvisor
    getLong("notice.id") foreach { noticeId =>
      val notice = entityDao.get(classOf[DefenseNotice], noticeId)
      if (groupService.isGroupManager(notice.group, advisor.teacher)) entityDao.remove(notice)
    }
    redirect("index", "info.remove.success")
  }

  def saveNotice(): View = {
    val advisor = getAdvisor
    getLong("notice.group.id") foreach { groupId =>
      val group = entityDao.get(classOf[DefenseGroup], groupId)
      if (groupService.isGroupManager(group, advisor.teacher)) {
        val notice = getLong("notice.id")
          .map(id => entityDao.get(classOf[DefenseNotice], id)).getOrElse(new DefenseNotice)
        notice.group = group
        notice.title = get("notice.title", "--")
        notice.contents = get("notice.contents", "--")
        notice.updatedAt = Instant.now
        entityDao.saveOrUpdate(notice)
      }
    }
    redirect("index", "info.save.success")
  }

  def report(): View = {
    val group = entityDao.get(classOf[DefenseGroup], getLong("id").get)
    val reviews =
      if group.writers.isEmpty then Map.empty[Writer, ThesisReview]
      else entityDao.findBy(classOf[ThesisReview], "writer", group.orderedWriters).map(x => (x.writer, x)).toMap

    val hsw = DefenseReport.render(group, reviews)
    val bos = new ByteArrayOutputStream()
    hsw.write(bos)
    Stream(new ByteArrayInputStream(bos.toByteArray), MediaTypes.ApplicationXlsx, "答辩信息表.xls")
  }

  def paperZip(): View = {
    val group = entityDao.get(classOf[DefenseGroup], getLong("id").get)
    val papers =
      if group.writers.isEmpty then List.empty[ThesisPaper]
      else entityDao.findBy(classOf[ThesisPaper], "writer", group.orderedWriters)

    val season = thesisPlanService.getPlan().get.season
    val targetZip = PaperDownloadHelper.zipGroupPapers(season, group, papers)
    val fileName = season.name + "_" + group.department.name + "_第" + group.idx + "组论文.zip"
    Stream(targetZip, MediaTypes.ApplicationZip, fileName)
  }

  def inputScore(): View = {
    val advisor = getAdvisor
    val season = thesisPlanService.getSeason()
    val query = OqlBuilder.from(classOf[DefenseGroup], "dg")
    query.where("dg.season=:season", season)
    query.where("exists(from dg.members as m where m.teacher=:me and m.leader=true)", advisor.teacher)
    getLong("id") foreach { id =>
      query.where("dg.id=:id", id)
    }
    val myGroups = entityDao.search(query)
    val writers = myGroups.flatMap(_.writers.map(_.writer))
    put("myGroups", myGroups)
    val reviews = entityDao.findBy(classOf[ThesisReview], "writer", writers).map(x => (x.writer, x)).toMap
    put("reviews", reviews)
    forward()
  }

  def saveScore(): View = {
    val group = entityDao.get(classOf[DefenseGroup], getLongId("group"))
    val reviews = entityDao.find(classOf[ThesisReview], getLongIds("review"))
    reviews foreach { r =>
      getInt(s"defense_score_${r.id}") match
        case None =>
          r.defenseScore = None
          r.finalScore = None
          r.finalScoreText = None
        case Some(s) =>
          val score = Math.round(r.crossReviewScore.getOrElse(0) * 0.6d + s * 0.4d).intValue()
          val scoreText = ScoreTextHelper.convert(score)
          r.defenseScore = Some(s)
          r.finalScore = Some(score)
          r.finalScoreText = Some(scoreText)
          r.remark = None
    }

    entityDao.saveOrUpdate(reviews)
    val msg = s"录入第${group.idx}组答辩成绩"
    businessLogger.info(msg, group.id, reviews.map(x => x.writer.code -> x.defenseScore.map(_.toString).getOrElse("")).toMap)
    redirect("index", "录入成功")
  }
}
