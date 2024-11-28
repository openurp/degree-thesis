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

import org.beangle.commons.collection.Collections
import org.beangle.data.dao.OqlBuilder
import org.beangle.security.Securities
import org.beangle.webmvc.view.View
import org.openurp.base.edu.model.{Major, TeachingOffice}
import org.openurp.base.hr.model.Teacher
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{Advisor, Subject, ThesisReview, Writer}
import org.openurp.degree.thesis.service.DefenseGroupService
import org.openurp.degree.thesis.web.helper.RandomReviewAssigner

/**
 * 教研室交叉评阅安排
 */
class CrossReviewAction extends DirectorSupport {

  var groupService: DefenseGroupService = _

  def index(): View = {
    val offices = getOffices()
    put("offices", offices)
    val season = thesisPlanService.getPlan().get.season
    val advisors = groupService.getManageTeacher(getAdvisor, season)
    val reviews = listReviews(season)
    put("advisors", advisors)
    put("reviews", reviews)
    put("teacherMajors", getTeacherMajors(season, advisors))

    val reviewStats = reviews.filter(_.crossReviewer.nonEmpty).groupBy(_.crossReviewer.get)
    val adviseStats = reviews.filter(_.writer.advisor.nonEmpty).groupBy(_.writer.advisor.get.teacher)
    put("adviseStats", adviseStats)
    put("reviewStats", reviewStats)
    forward()
  }

  private def listReviews(season: GraduateSeason): Iterable[ThesisReview] = {
    val query = OqlBuilder.from(classOf[ThesisReview], "review")
    query.where("review.writer.season=:season", season)
    query.where("review.crossReviewManager=:me", getAdvisor.teacher)
    query.orderBy("review.writer.std.state.major.id,review.writer.advisor.id,review.writer.std.code")
    entityDao.search(query)
  }

  /** 批量指派交叉评阅教师设置
   *
   * @return
   */
  def batchAssignSetting(): View = {
    val reviews = entityDao.find(classOf[ThesisReview], getLongIds("thesisReview"))
    val advisor = getAdvisor
    put("crossReviewers", groupService.getManageTeacher(advisor, reviews.head.writer.season))
    put("thesisReviews", reviews)
    forward()
  }

  /** 批量指派交叉评阅教师
   *
   * @return
   */
  def batchAssign(): View = {
    val reviews = entityDao.find(classOf[ThesisReview], getLongIds("thesisReview"))
    val reviewer = getLong("crossReviewer.id").map(id => entityDao.get(classOf[Teacher], id))
    reviews foreach { r =>
      reviewer match
        case None => r.crossReviewer = None
        case Some(t) => if !r.writer.advisor.map(_.teacher).contains(t) then r.crossReviewer = reviewer
    }
    entityDao.saveOrUpdate(reviews)
    redirect("index", "指派成功")
  }

  /** 教研室内随机分配
   *
   * @return
   */
  def randomAssign(): View = {
    val season = thesisPlanService.getPlan().get.season
    val reviews = listReviews(season).filter(_.crossReviewer.isEmpty)

    val advisors = groupService.getManageTeacher(getAdvisor, season)
    RandomReviewAssigner.assign(reviews, advisors)
    entityDao.saveOrUpdate(reviews)
    redirect("index", "随机分配成功")
  }

  private def getTeacherMajors(season: GraduateSeason, teachers: Iterable[Teacher]): Map[Teacher, Set[Major]] = {
    val sQuery = OqlBuilder.from[Array[Object]](classOf[Writer].getName, "w")
    sQuery.where("w.season=:season", season)
    sQuery.where("w.advisor is not null and w.std.state.major is not null")
    sQuery.select("distinct w.advisor.teacher,w.std.state.major")
    sQuery.where("w.advisor.teacher in(:teachers)", teachers)
    val rs = Collections.newMap[Teacher, Set[Major]]
    rs ++= entityDao.search(sQuery).map(x => (x(0).asInstanceOf[Teacher], x(1).asInstanceOf[Major])).groupBy(_._1).map(x => (x._1, x._2.map(_._2).toSet))
    teachers.foreach { t =>
      if (!rs.contains(t)) rs.put(t, Set.empty)
    }
    rs.toMap
  }
}
