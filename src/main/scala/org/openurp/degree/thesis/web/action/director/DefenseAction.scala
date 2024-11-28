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

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.view.{Stream, View}
import org.beangle.webmvc.support.helper.QueryHelper
import org.openurp.base.edu.model.Major
import org.openurp.base.hr.model.Teacher
import org.openurp.base.model.User
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.DefenseGroupService
import org.openurp.degree.thesis.service.doc.DefenseReport
import org.openurp.degree.thesis.web.helper.{PaperDownloadHelper, RandomGroupHelper}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.time.Instant
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class DefenseAction extends DirectorSupport {
  var groupService: DefenseGroupService = _

  def index(): View = {
    val advisor = getAdvisor
    val season = thesisPlanService.getSeason()
    val myGroups = groupService.getGroups(advisor.teacher, season)
    put("myGroups", myGroups)
    val manageGroups = myGroups.filter(g => groupService.isGroupManager(g, advisor.teacher))
    put("manageGroups", manageGroups)

    advisor.teacher.office.foreach { office =>
      val query2 = OqlBuilder.from(classOf[DefenseGroup], "dg")
      query2.where("dg.season=:season", season)
      query2.where("dg.office=:office", office)
      query2.orderBy("dg.idx")
      put("adminGroups", entityDao.search(query2))
      put("office", office)
    }
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
    val season = thesisPlanService.getPlan().get.season
    advisor.teacher.office.foreach { office =>
      val query = OqlBuilder.from(classOf[Advisor], "advisor")
      query.where("advisor.teacher.office=:office", office)
      val advisors = entityDao.search(query).map(x => x.teacher)

      val query2 = OqlBuilder.from(classOf[DefenseMember], "dm")
      query2.where("dm.group.office=:office", office)
      query2.where("dm.group.season=:season", season)
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
      query2.where("writer.season=:season", season)
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
   * @return
   */
  @mapping(value = "{id}/edit")
  def edit(@param("id") id: String): View = {
    val defenseGroup = entityDao.get(classOf[DefenseGroup], id.toLong)
    put("defenseGroup", defenseGroup)
    put("writers", defenseGroup.orderedWriters)

    val teachers = searchMemberCandidates(defenseGroup)
    //将答辩组所在学院的教师放在头部
    val myDeparts = teachers.filter(x => x.department == defenseGroup.department)
    teachers.subtractAll(myDeparts)
    teachers.prependAll(myDeparts)
    val secretaries = entityDao.findBy(classOf[User], "school" -> defenseGroup.season.project.school, "code" -> myDeparts.map(_.code))
    put("secretaries", secretaries)
    put("advisors", teachers)
    forward()
  }

  private def searchMemberCandidates(group: DefenseGroup): mutable.Buffer[Teacher] = {
    val query = OqlBuilder.from[Teacher](classOf[Advisor].getName, "advisor")
    //query.where("advisor.endOn is null or advisor.endOn>:today", LocalDate.now)
    query.orderBy("advisor.teacher.department.name,advisor.teacher.name")
    query.select("advisor.teacher")
    val teachers = entityDao.search(query).toBuffer
    val writerAdvisors = group.writers.filter(_.writer.advisor.nonEmpty).map(_.writer.advisor.get.teacher).toSet
    teachers.subtractAll(writerAdvisors)

    teachers
  }

  @mapping(value = "new", view = "new,form")
  def editNew(): View = {
    val advisor = getAdvisor
    val season = thesisPlanService.getPlan().get.season
    val defenseGroup = new DefenseGroup(season, advisor.teacher.department)
    put("defenseGroup", defenseGroup)
    val teachers = searchMemberCandidates(defenseGroup)
    //将答辩组所在学院的教师放在头部
    val myDeparts = teachers.filter(x => x.department == defenseGroup.department)
    teachers.subtractAll(myDeparts)
    teachers.prependAll(myDeparts)
    val secretaries = entityDao.findBy(classOf[User], "school" -> defenseGroup.season.project.school, "code" -> myDeparts.map(_.code))
    put("secretaries", secretaries)
    put("advisors", teachers)
    forward()
  }

  @mapping(method = "delete")
  def remove(): View = {
    val advisor = getAdvisor
    val defenseGroup = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    if groupService.isGroupAdmin(defenseGroup, advisor) then
      if defenseGroup.published then
        redirect("index", "不能删除已经发布的答辩组")
      else
        entityDao.remove(defenseGroup)
        redirect("index", "info.remove.success")
    else
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

  private def genGroupIdx(group: DefenseGroup): Int = {
    if (group.idx == 0) {
      val query = OqlBuilder.from[Int](classOf[DefenseGroup].getName, "g")
      query.where("g.department=:depart", group.department)
      query.where("g.season=:season", group.season)
      query.select("g.idx")
      query.orderBy("g.idx desc")
      val indexList = entityDao.search(query)
      if (indexList.isEmpty) 1 else indexList.head + 1
    } else {
      group.idx
    }
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
    group.idx = genGroupIdx(group)
    group.members foreach { m =>
      getBoolean(s"member_${m.teacher.id}.leader") foreach { leader =>
        m.leader = leader
      }
    }
    (1 to 5) foreach { i =>
      getLong(s"newmember_$i.teacher.id") foreach { teacherId =>
        val teacher = entityDao.get(classOf[Teacher], teacherId)
        if group.members.forall(x => x.teacher != teacher) then
          val nm = new DefenseMember
          nm.teacher = teacher
          nm.leader = getBoolean(s"newmember_$i.leader", false)
          nm.group = group
          group.members += nm
        end if
      }
    }
    entityDao.saveOrUpdate(group)
    redirect("index", "info.save.success")
  }

  def removeWriter(): View = {
    val advisor = getAdvisor
    val defenseGroup = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    if (groupService.isGroupAdmin(defenseGroup, advisor)) {
      val removed = entityDao.get(classOf[Writer], getLong("writer.id").get)
      defenseGroup.removeWriter(Set(removed))
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
      val query = getCandidatesQuery(advisor, group)
      QueryHelper.populate(query, null)
      QueryHelper.sort(query)
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
        if (!advisors.contains(add.advisor.get.teacher)) {
          defenseGroup.addWriters(List(add))
        }
      }
      entityDao.saveOrUpdate(defenseGroup)
    }
    redirect("edit", s"id=${defenseGroup.id}", "info.save.success")
  }

  private def getCandidatesQuery(advisor: Advisor, group: DefenseGroup): OqlBuilder[Writer] = {
    val season = thesisPlanService.getSeason()
    val advisors = group.members.map(_.teacher)
    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.season=:season", season)
    query.where("writer.advisor is not null")
    query.where("exists(from " + classOf[ThesisReview].getName + " tr where tr.writer=writer and tr.crossReviewManager=:manager)", advisor.teacher)
    if advisors.nonEmpty then query.where("writer.advisor.teacher not in(:advisors)", advisors)
    query.where("not exists(from " + classOf[DefenseWriter].getName + " dw  where dw.writer=writer)")
    query
  }

  def autoAddWriter(): View = {
    val advisor = getAdvisor
    val newCount = getInt("newCount", 0)
    val defenseGroup = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    if (defenseGroup.office.contains(advisor.teacher.office.get) && newCount > 0) {
      val query = getCandidatesQuery(getAdvisor, defenseGroup)
      val writers = entityDao.search(query)
      var i = 0
      val writerIter = writers.iterator
      while (i < newCount && writerIter.hasNext) {
        val writer = writerIter.next()
        defenseGroup.addWriters(List(writer))
        i += 1
      }
      entityDao.saveOrUpdate(defenseGroup)
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

  def randomGroupSetting(): View = {
    val advisor = getAdvisor
    val season = thesisPlanService.getPlan().get.season
    val group = new DefenseGroup(season, advisor.teacher.department)

    val query = getCandidatesQuery(advisor, group)
    val writers = entityDao.search(query)
    val majorWriters = writers.groupBy(_.std.state.get.major)
    put("majorWriters", majorWriters)
    put("writers", writers)

    val teachers = groupService.getManageTeacher(advisor, thesisPlanService.getPlan().get.season)
    put("teachers", teachers)
    forward()
  }

  def randomGroupSchedule(): View = {
    val advisor = getAdvisor
    val season = thesisPlanService.getPlan().get.season
    val group = new DefenseGroup(season, advisor.teacher.department)
    val query = getCandidatesQuery(advisor, group)
    val writers = entityDao.search(query)
    val majorWriters = writers.groupBy(_.std.state.get.major)
    put("majorWriters", majorWriters)
    put("writers", writers)

    val teachers = groupService.getManageTeacher(advisor, thesisPlanService.getPlan().get.season)
    put("teachers", teachers)

    val selectTeachers = entityDao.find(classOf[Teacher], getLongIds("teacher"))
    val selectMajors = entityDao.find(classOf[Major], getLongIds("major"))
    val selectWriters = writers.filter(x => selectMajors.contains(x.std.state.get.major))
    put("selectTeachers", selectTeachers.toSet)
    put("selectMajors", selectMajors.toSet)

    var count = getInt("groupCount", 1)
    if (count <= 1) count = 1
    if (count > teachers.size) count = teachers.size

    val groups = RandomGroupHelper.generate(selectWriters, selectTeachers, count)
    put("groupCount", count)
    put("groups", groups)
    forward()
  }

  def saveRandomGroup(): View = {
    val count = getInt("groupCount", 1)
    val advisor = getAdvisor
    val season = thesisPlanService.getPlan().get.season

    (0 until count) foreach { i =>
      val group = new DefenseGroup(season, advisor.teacher.department)
      group.office = advisor.teacher.office
      val teachers = entityDao.find(classOf[Teacher], Strings.splitToLong(get(s"group_${i}_teacherIds", "")))
      val writers = entityDao.find(classOf[Writer], Strings.splitToLong(get(s"group_${i}_writerIds", "")))
      group.members ++= teachers.map { x => val m = new DefenseMember; m.group = group; m.teacher = x; m }
      group.addWriters(writers)
      group.idx = genGroupIdx(group)
      entityDao.saveOrUpdate(group)
    }
    redirect("index", "分组成功")
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

}
