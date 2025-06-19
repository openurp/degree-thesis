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

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.lang.{Numbers, Strings}
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.view.{Stream, View}
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}
import org.beangle.webmvc.support.helper.QueryHelper
import org.openurp.base.edu.model.{Major, TeachingOffice}
import org.openurp.base.hr.model.Teacher
import org.openurp.base.model.{Department, Project, User}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.doc.DefenseReport
import org.openurp.degree.thesis.service.{DefenseGroupService, ThesisPlanService}
import org.openurp.degree.thesis.web.helper.{PaperDownloadHelper, RandomGroupHelper, ScoreTextHelper}
import org.openurp.starter.web.support.ProjectSupport

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import scala.collection.mutable

class DefenseAction extends RestfulAction[DefenseGroup], ExportSupport[DefenseGroup], ProjectSupport {
  var groupService: DefenseGroupService = _
  var thesisPlanService: ThesisPlanService = _
  var businessLogger: WebBusinessLogger = _

  override def indexSetting(): Unit = {
    given project: Project = getProject

    val departs = getDeparts
    put("departs", departs)
    val oQuery = OqlBuilder.from(classOf[TeachingOffice], "o")
    oQuery.where("o.department in(:departs)", departs)
    oQuery.where("o.endOn is null")
    val offices = entityDao.search(oQuery)
    put("offices", offices)

    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateIn desc")
    put("seasons", entityDao.search(gQuery))
  }

  override protected def getQueryBuilder: OqlBuilder[DefenseGroup] = {
    val query = super.getQueryBuilder

    given project: Project = getProject

    query.where("defenseGroup.department in(:departs)", getDeparts)
    get("teacherName") foreach { teacherName =>
      if (Strings.isNotBlank(teacherName)) {
        query.where("exists(from defenseGroup.members m where m.teacher.name like :teacherName)", s"%${teacherName}%")
      }
    }
    get("writerName") foreach { writerName =>
      if (Strings.isNotBlank(writerName)) {
        query.where("exists(from defenseGroup.writers w where w.writer.std.name like :writerName or w.writer.std.code like :writerName)", s"%${writerName}%")
      }
    }
    query
  }

  /** 编辑答辩组
   *
   * @param id
   * @return
   */
  @mapping(value = "{id}/edit")
  override def edit(@param("id") id: String): View = {
    val defenseGroup = entityDao.get(classOf[DefenseGroup], id.toLong)
    put("defenseGroup", defenseGroup)
    put("writers", defenseGroup.orderedWriters)

    given project: Project = getProject

    val teachers = searchMemberCandidates(defenseGroup)
    //将答辩组所在学院的教师放在头部
    val myDeparts = teachers.filter(x => x.department == defenseGroup.department)
    teachers.subtractAll(myDeparts)
    teachers.prependAll(myDeparts)
    val secretaries = entityDao.findBy(classOf[User], "school" -> project.school, "code" -> myDeparts.map(_.code))
    put("secretaries", secretaries)
    put("advisors", teachers)
    val departs = getDeparts
    put("departs", departs)
    put("offices", entityDao.findBy(classOf[TeachingOffice], "project" -> project, "department" -> departs))
    forward()
  }

  private def searchMemberCandidates(group: DefenseGroup): mutable.Buffer[Teacher] = {
    val query = OqlBuilder.from[Teacher](classOf[Advisor].getName, "advisor")
    //query.where("advisor.endOn is null or advisor.endOn>:today", LocalDate.now)
    query.orderBy("advisor.teacher.department.name,advisor.teacher.name")
    query.select("advisor.teacher")
    val teachers = entityDao.search(query).toBuffer
    val writerAdvisors = group.orderedWriters.map(_.advisor.get.teacher).toSet
    teachers.subtractAll(writerAdvisors)

    teachers
  }

  @mapping(value = "new", view = "new,form")
  override def editNew(): View = {
    given project: Project = getProject

    val departs = getDeparts
    val defenseGroup = new DefenseGroup(thesisPlanService.getPlan().get.season, departs.head)
    put("defenseGroup", defenseGroup)
    put("departs", departs)
    put("offices", entityDao.findBy(classOf[TeachingOffice], "department" -> departs))
    val teachers = searchMemberCandidates(defenseGroup)
    //将答辩组所在学院的教师放在头部
    val myDeparts = teachers.filter(x => x.department == defenseGroup.department)
    teachers.subtractAll(myDeparts)
    teachers.prependAll(myDeparts)
    val secretaries = entityDao.findBy(classOf[User], "school" -> project.school, "code" -> myDeparts.map(_.code))
    put("secretaries", secretaries)
    put("advisors", teachers)
    forward()
  }

  @mapping(value = "{id}")
  override def info(@param("id") id: String): View = {
    val group = entityDao.get(classOf[DefenseGroup], id.toLong)
    val reviews =
      if group.writers.isEmpty then Map.empty[Writer, ThesisReview]
      else entityDao.findBy(classOf[ThesisReview], "writer", group.orderedWriters).map(x => (x.writer, x)).toMap

    put("reviews", reviews)
    put("defenseGroup", group)
    forward()
  }

  @mapping(method = "delete")
  override def remove(): View = {
    val defenseGroups = entityDao.find(classOf[DefenseGroup], getLongIds("defenseGroup"))

    given project: Project = getProject

    var removed = 0
    val departs = getDeparts
    defenseGroups foreach { defenseGroup =>
      if departs.contains(defenseGroup.department) && !defenseGroup.published then
        entityDao.remove(defenseGroup)
        removed += 1
    }
    redirect("index", if removed > 0 then s"成功删除${removed}组" else "已发布的组不能删除，请取消发布后删除")
  }

  def removeAdvisor(): View = {
    given project: Project = getProject

    val departs = getDeparts
    val group = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    if (departs.contains(group.department)) {
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
  override def save(): View = {
    given project: Project = getProject

    val departs = getDeparts
    val group = getLong("defenseGroup.id") match {
      case None => new DefenseGroup(thesisPlanService.getPlan().get.season, null)
      case Some(id) => entityDao.get(classOf[DefenseGroup], id)
    }

    group.department = entityDao.get(classOf[Department], getIntId("defenseGroup.department"))
    group.office = entityDao.find(classOf[TeachingOffice], getLongIds("defenseGroup.office")).headOption
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
      getLong(s"newmember_${i}.teacher.id") foreach { teacherId =>
        val teacher = entityDao.get(classOf[Teacher], teacherId)
        if group.members.forall(x => x.teacher != teacher) then
          val nm = new DefenseMember
          nm.teacher = teacher
          nm.leader = getBoolean(s"newmember_${i}.leader", false)
          nm.group = group
          group.members += nm
        end if
      }
    }
    entityDao.saveOrUpdate(group)
    redirect("index", "info.save.success")
  }

  def removeWriter(): View = {
    val defenseGroup = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    val removed = entityDao.get(classOf[Writer], getLong("writer.id").get)
    defenseGroup.removeWriter(Set(removed))
    entityDao.saveOrUpdate(defenseGroup)
    val msg = s"从第${defenseGroup.idx}组中移除${removed.std.name}"
    businessLogger.info(msg, defenseGroup.id, Map("writer.std.code" -> removed.std.code, "group.id" -> defenseGroup.id.toString))
    redirect("edit", s"id=${defenseGroup.id}", "info.remove.success")
  }

  def addWriterList(): View = {
    given project: Project = getProject

    val season = thesisPlanService.getPlan().get.season
    val group = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    put("defenseGroup", group)
    val advisors = group.members.map(_.teacher)
    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.season=:season", season)
    query.where("writer.std.state.department = :depart", group.department)
    if advisors.nonEmpty then query.where("writer.advisor.teacher not in(:advisors)", advisors)
    query.where("not exists(from " + classOf[DefenseGroup].getName + " dg join dg.writers as wr where wr.writer=writer)")
    populateConditions(query)
    QueryHelper.sort(query)
    val writers = entityDao.search(query)
    put("writers", writers)
    forward()
  }

  def addWriter(): View = {
    val defenseGroup = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    val advisors = defenseGroup.members.map(_.teacher).toSet
    val added = entityDao.find(classOf[Writer], getAll("writer.id", classOf[Long]))
    added foreach { add =>
      if (!advisors.contains(add.advisor.get.teacher)) defenseGroup.addWriters(Set(add))
    }
    entityDao.saveOrUpdate(defenseGroup)
    redirect("edit", s"id=${defenseGroup.id}", "info.save.success")
  }

  def autoAddWriter(): View = {
    val season = thesisPlanService.getPlan().get.season
    val newCount = getInt("newCount", 0)
    val defenseGroup = entityDao.get(classOf[DefenseGroup], getLong("defenseGroup.id").get)
    if (newCount > 0) {
      val advisors = defenseGroup.members.map(_.teacher)
      val query = OqlBuilder.from(classOf[Writer], "writer")
      query.where("writer.season=:season", season)
      query.where("writer.std.state.department =:depart", defenseGroup.department)
      defenseGroup.office.foreach { office =>
        query.where("writer.advisor.teacher.office=:office", office)
      }
      if advisors.nonEmpty then query.where("writer.advisor.teacher not in(:advisors)", advisors)
      query.where("not exists(from " + classOf[DefenseGroup].getName + " dg join dg.writers as wr where wr.writer=writer)")
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

  /** 自动建组
   *
   * @return
   */
  def randomManager(): View = {
    given project: Project = getProject

    val season = thesisPlanService.getPlan().get.season
    val departs = getDeparts
    val query = OqlBuilder.from[Array[Any]](classOf[ThesisReview].getName, "tr")
    query.where("tr.writer.std.state.department in(:departs)", departs)
    query.where("tr.writer.season=:season", season)
    query.where("tr.crossReviewManager is not null")
    query.where("not exists(from " + classOf[DefenseGroup].getName + " dg join dg.writers as wr where wr.writer=tr.writer)")
    query.select("tr.crossReviewManager.id,count(*)")
    query.groupBy("tr.crossReviewManager.id")
    //    query.orderBy("tr.crossReviewManager.department.code,tr.crossReviewManager.name")
    val rs = entityDao.search(query)
    rs.map { a =>
      a(0) = entityDao.get(classOf[Teacher], a(0).asInstanceOf[Long])
      a
    }
    put("reviewManagers", rs.sortBy(x => x(0).asInstanceOf[Teacher].department.code))
    forward()
  }

  private def getCandinatesQuery(advisor: Advisor, group: DefenseGroup): OqlBuilder[Writer] = {
    val season = thesisPlanService.getPlan().get.season
    val advisors = group.members.map(_.teacher)
    val query = OqlBuilder.from[Writer](classOf[ThesisReview].getName, "tr")
    query.where("tr.writer.season=:season", season)
    query.where("tr.writer.advisor is not null")
    query.where("tr.crossReviewManager = :manager", advisor.teacher)
    if advisors.nonEmpty then query.where("tr.writer.advisor.teacher not in(:advisors)", advisors)
    query.where("not exists(from " + classOf[DefenseGroup].getName + " dg join dg.writers as wr where wr.writer=tr.writer)")
    query.select("tr.writer")
    query
  }

  def randomGroupSetting(): View = {
    val advisor = entityDao.findBy(classOf[Advisor], "teacher.id", getLongId("manager")).head
    val season = thesisPlanService.getPlan().get.season
    val group = new DefenseGroup(season, advisor.teacher.department)
    val query = getCandinatesQuery(advisor, group)
    val writers = entityDao.search(query)

    val majorWriters = writers.groupBy(_.std.state.get.major)
    put("majorWriters", majorWriters)
    put("writers", writers)

    var teachers = writers.map(_.advisor.get.teacher).toSet
    val offices = writers.flatMap(_.advisor.get.teacher.office).toSet
    if (offices.nonEmpty) {
      val query2 = OqlBuilder.from[Teacher](classOf[Advisor].getName, "advisor")
      query2.where("advisor.teacher.office in(:offices)", offices)
      query2.where("advisor.endOn is null")
      query2.select("advisor.teacher")
      teachers ++= entityDao.search(query2)
    }
    put("teachers", teachers)
    put("manager", advisor.teacher)
    forward()
  }

  def randomGroupSchedule(): View = {
    val advisor = entityDao.findBy(classOf[Advisor], "teacher.id", getLongId("manager")).head
    val season = thesisPlanService.getPlan().get.season
    val group = new DefenseGroup(season, advisor.teacher.department)
    val query = getCandinatesQuery(advisor, group)
    val writers = entityDao.search(query)

    val majorWriters = writers.groupBy(_.std.state.get.major)
    put("majorWriters", majorWriters)
    put("writers", writers)
    val teachers = groupService.getManageTeacher(advisor, thesisPlanService.getPlan().get.season)
    put("teachers", teachers)

    val selectTeachers = entityDao.find(classOf[Teacher], getLongIds("teacher"))
    val selectMajors = entityDao.find(classOf[Major], getLongIds("major"))
    put("selectTeachers", selectTeachers.toSet)
    put("selectMajors", selectMajors.toSet)
    var count = getInt("groupCount", 1)
    if (count <= 1) count = 1
    if (count > teachers.size) count = teachers.size
    val groups = RandomGroupHelper.generate(writers, selectTeachers, count)
    put("groupCount", count)
    put("groups", groups)
    put("manager", advisor.teacher)
    forward()
  }

  def saveRandomGroup(): View = {
    val count = getInt("groupCount", 1)
    val advisor = entityDao.findBy(classOf[Advisor], "teacher.id", getLongId("manager")).head
    val season = thesisPlanService.getPlan().get.season
    (0 until count) foreach { i =>
      val group = new DefenseGroup(season, advisor.teacher.department)
      val teachers = entityDao.find(classOf[Teacher], Strings.splitToLong(get(s"group_${i}_teacherIds", "")))
      val writers = entityDao.find(classOf[Writer], Strings.splitToLong(get(s"group_${i}_writerIds", "")))
      group.office = advisor.teacher.office

      group.members ++= teachers.map { x => val m = new DefenseMember; m.group = group; m.teacher = x; m }
      group.addWriters(writers)
      group.idx = genGroupIdx(group)
      entityDao.saveOrUpdate(group)
    }
    redirect("search", "分组成功")
  }

  def publish(): View = {
    val defenseGroups = entityDao.find(classOf[DefenseGroup], getLongIds("defenseGroup"))

    given project: Project = getProject

    val published = getBoolean("published", true)
    val departs = getDeparts
    defenseGroups foreach { defenseGroup =>
      if departs.contains(defenseGroup.department) then defenseGroup.published = published
    }
    entityDao.saveOrUpdate(defenseGroups)
    redirect("search", "操作成功")
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

  def reportZip(): View = {
    val group = entityDao.get(classOf[DefenseGroup], getLong("id").get)
    val reviews =
      if group.writers.isEmpty then List.empty[ThesisReview]
      else entityDao.findBy(classOf[ThesisReview], "writer", group.orderedWriters)

    val season = thesisPlanService.getPlan().get.season
    val targetZip = PaperDownloadHelper.zipGroupReport(season, group, reviews)
    val fileName = season.name + "_" + group.department.name + "_第" + group.idx + "组答辩评分表.zip"
    Stream(targetZip, MediaTypes.ApplicationZip, fileName).cleanup { () => targetZip.delete() }
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
    val group = entityDao.get(classOf[DefenseGroup], getLongId("defenseGroup"))
    val writers = group.writers.map(_.writer)
    put("group", group)
    val reviews =
      if writers.isEmpty then Map.empty
      else entityDao.findBy(classOf[ThesisReview], "writer", writers).map(x => (x.writer, x)).toMap
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
          r.defenseScore = Some(s)
          val score = Numbers.round(r.crossReviewScore.getOrElse(0f) * 0.6d + s * 0.4d, 0).intValue()
          val scoreText = ScoreTextHelper.convert(score)
          r.remark = None
          r.finalScore = Some(score)
          r.finalScoreText = Some(scoreText)
    }

    entityDao.saveOrUpdate(reviews)
    val msg = s"录入第${group.idx}组答辩成绩"
    businessLogger.info(msg, group.id, reviews.map(x => x.writer.code -> x.defenseScore.map(_.toString).getOrElse("")).toMap)
    redirect("search", "录入成功")
  }
}
