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
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.view.View
import org.openurp.base.edu.model.TeachingOffice
import org.openurp.degree.thesis.model.{CopyCheck, Stage, ThesisReview, Writer}

import java.time.Instant

class CrossReviewAction extends AdvisorSupport {

  def index(): View = {
    val advisor = getAdvisor
    val query = OqlBuilder.from(classOf[ThesisReview], "review")
    query.where("review.crossReviewer=:me", advisor.teacher)
    query.where("review.writer.season=:season", thesisPlanService.getPlan().get.season)
    val reviews = entityDao.search(query)
    put("reviews", reviews)

    if (reviews.isEmpty) {
      put("checks", Map.empty[Writer, Seq[CopyCheck]])
    } else {
      val checks = entityDao.findBy(classOf[CopyCheck], "writer", reviews.map(_.writer)).groupBy(_.writer)
      put("checks", checks)
    }

    val query2 = OqlBuilder.from(classOf[TeachingOffice], "office")
    query2.where("office.director.staff.code=:me", Securities.user)
    val offices = entityDao.search(query2)
    put("offices", offices)

    forward()
  }

  def reviewSetting(): View = {
    val review = entityDao.get(classOf[ThesisReview], getLongId("thesisReview"))
    put("review", review)
    if (review.crossReviewScore.isEmpty) {
      forward()
    } else {
      val plan = thesisPlanService.getPlan().get
      val dp = plan.departPlans.find(x => x.department == review.writer.department)
      dp match
        case None => redirect("index", "不在评分时间")
        case Some(p) =>
          val intime = p.getStageTime(Stage.ThesisReview).timeSuitable(Instant.now) == 0
          if intime then
            forward()
          else
            redirect("index", "不在评分时间")
    }
  }

  def review(): View = {
    val review = entityDao.get(classOf[ThesisReview], getLongId("thesisReview"))
    review.crossReviewOpinion = get("thesisReview.crossReviewOpinion")
    review.crossReviewScore = getFloat("thesisReview.crossReviewScore")
    review.defensePermitted = getBoolean("thesisReview.defensePermitted")
    review.crossReviewAt = Some(Instant.now)
    entityDao.saveOrUpdate(review)
    val writer = review.writer
    val msg = s"交叉评阅了${writer.std.name}的论文，评分:${review.crossReviewScore.getOrElse(0)}"
    businessLogger.info(msg, writer.id, ActionContext.current.params)
    redirect("index", "评阅完成")
  }

}
