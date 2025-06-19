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
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}
import org.openurp.base.edu.model.{Major, TeachingOffice}
import org.openurp.base.hr.model.Teacher
import org.openurp.base.model.{Department, Project}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{Advisor, Subject, ThesisReview, Writer}
import org.openurp.degree.thesis.service.ThesisPlanService
import org.openurp.degree.thesis.web.helper.RandomReviewAssigner
import org.openurp.starter.web.support.ProjectSupport

import scala.util.Random

class ReviewAction extends RestfulAction[ThesisReview], ExportSupport[ThesisReview], ProjectSupport {

  var thesisPlanService: ThesisPlanService = _

  override def index(): View = {
    given project: Project = getProject

    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateIn desc")
    put("seasons", entityDao.search(gQuery))
    put("departs", getDeparts)
    forward()
  }

  override protected def getQueryBuilder: OqlBuilder[ThesisReview] = {
    given project: Project = getProject

    put("departs", getDeparts.filter(_.teaching))
    val query = super.getQueryBuilder
    query.where("thesisReview.writer.std.state.department in(:departs)", getDeparts)
    query
  }

  override protected def editSetting(entity: ThesisReview): Unit = {
    val query = OqlBuilder.from(classOf[Advisor], "ad")
    query.where(":depart in elements(ad.departs)", entity.writer.department)
    val advisorId = entity.writer.advisor.map(_.id).getOrElse(0L)
    query.where("ad.id != :myTutorId", advisorId)
    query.select("ad.teacher")
    put("crossReviewers", entityDao.search(query))
  }

  /** 批量指派交叉评阅教师设置
   *
   * @return
   */
  def batchAssignSetting(): View = {
    val reviews = entityDao.find(classOf[ThesisReview], getLongIds("thesisReview"))
    val query = OqlBuilder.from[Teacher](classOf[Advisor].getName, "ad")
    query.where(":depart in elements(ad.departs)", reviews.head.writer.std.department)
    query.select("ad.teacher")
    put("crossReviewers", entityDao.search(query))
    put("thesisReviews", reviews)
    forward()
  }

  /** 批量指派交叉评阅教师
   *
   * @return
   */
  def batchAssign(): View = {
    val reviews = entityDao.find(classOf[ThesisReview], getLongIds("thesisReview"))
    val advisor = getLong("crossReviewer.id").map(id => entityDao.get(classOf[Teacher], id))
    reviews foreach { r =>
      advisor match
        case None => r.crossReviewer = None
        case Some(ad) => if !r.writer.advisor.map(_.teacher).contains(ad) then r.crossReviewer = Some(ad)
    }
    entityDao.saveOrUpdate(reviews)
    redirect("search", "指派成功")
  }

  def batchSetManagerSetting(): View = {
    val reviews = entityDao.find(classOf[ThesisReview], getLongIds("thesisReview"))
    val query = OqlBuilder.from[Teacher](classOf[Advisor].getName, "ad")
    query.where(":depart in elements(ad.departs)", reviews.head.writer.std.department)
    query.select("ad.teacher")
    put("managers", entityDao.search(query))
    put("thesisReviews", reviews)
    forward()
  }

  /** 批量设置交叉评阅负责人
   *
   * @return
   */
  def batchSetManager(): View = {
    val reviews = entityDao.find(classOf[ThesisReview], getLongIds("thesisReview"))
    val reviewer = getLong("manager.id").map(id => entityDao.get(classOf[Teacher], id))
    reviews foreach { r =>
      r.crossReviewManager = reviewer
    }
    entityDao.saveOrUpdate(reviews)
    redirect("search", "设置成功")
  }

  /** 按照教研室主任设置交叉评阅负责人
   *
   * @return
   */
  def batchSetManagerByOfficeDirector(): View = {
    val reviews = entityDao.find(classOf[ThesisReview], getLongIds("thesisReview"))
    reviews foreach { r =>
      if (r.crossReviewManager.isEmpty) {
        r.writer.advisor foreach { a =>
          a.teacher.office foreach { o =>
            r.crossReviewManager = o.director
          }
        }
      }
    }
    entityDao.saveOrUpdate(reviews)
    redirect("search", "设置成功")
  }

  /** 院系内随机分配
   *
   * @return
   */
  def randomAssign(): View = {
    val seasonId = getLongId("thesisReview.writer.season")
    val season = entityDao.get(classOf[GraduateSeason], seasonId)
    val query = OqlBuilder.from(classOf[ThesisReview], "thesisReview")
    query.where("thesisReview.writer.season=:season", season)
    query.where("thesisReview.id in(:ids)", getLongIds("thesisReview"))
    query.where("thesisReview.crossReviewer is null and thesisReview.writer.advisor is not null")
    val reviews = entityDao.search(query)

    val query2 = OqlBuilder.from[Array[Any]](classOf[Writer].getName, "writer")
    query2.where("writer.season=:season", season)
    query2.where("writer.advisor is not null")
    query2.select("distinct writer.std.state.department,writer.advisor.teacher")
    val reviewers = entityDao.search(query2).map(x => x(1).asInstanceOf[Teacher] -> x(0).asInstanceOf[Department]).groupBy(_._2).map(x => (x._1, x._2.map(_._1)))

    val query3 = OqlBuilder.from[Array[Any]](classOf[ThesisReview].getName, "tr")
    query3.where("tr.writer.season=:season", season)
    query3.where("tr.crossReviewer is not null")
    query3.select("tr.crossReviewer.id,count(*)")
    query3.groupBy("tr.crossReviewer.id")
    val reivewCnt = entityDao.search(query3).map(x => x(0).asInstanceOf[Long] -> x(1).asInstanceOf[Number].intValue()).toMap

    if (reviewers.nonEmpty) {
      RandomReviewAssigner.assign2(reviews, reviewers, reivewCnt)
      entityDao.saveOrUpdate(reviews)
    }
    redirect("search", s"随机分配成功${reviews.size}名同学")
  }

}
