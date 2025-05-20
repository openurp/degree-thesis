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

import org.beangle.commons.lang.Numbers
import org.beangle.webmvc.view.View
import org.openurp.base.model.AuditStatus
import org.openurp.degree.thesis.model.*

import java.time.Instant

/** 指导教师评阅
 */
class ReviewAction extends AdvisorSupport {

  def index(): View = {
    val advisor = getAdvisor
    val writers = getWriters
    val papers = entityDao.findBy(classOf[ThesisPaper], "writer", writers).map(x => (x.writer, x)).toMap
    put("papers", papers)
    val reviews = entityDao.findBy(classOf[ThesisReview], "writer", writers).map(x => (x.writer, x)).toMap
    val checks = entityDao.findBy(classOf[CopyCheck], "writer", writers).groupBy(_.writer)
    //FIXME 需要检查是否启用了中期检查
    //    var midtermChecks = entityDao.findBy(classOf[MidtermCheck], "writer", writers).map(x => (x.writer, x)).toMap
    //    if (midtermChecks.isEmpty) {
    val midtermChecks = writers.map { x =>
      val c = new MidtermCheck()
      c.status = AuditStatus.Passed
      (x, c)
    }.toMap
    //    }
    val blindReviews = entityDao.findBy(classOf[BlindPeerReview], "writer", writers).map(x => (x.writer, x)).toMap
    put("checks", checks)
    put("reviews", reviews)
    put("writers", writers)
    put("midtermChecks", midtermChecks)
    put("blindReviews", blindReviews)
    forward()
  }

  def review(): View = {
    val writer = entityDao.get(classOf[Writer], getLongId("writer"))
    val review = entityDao.findBy(classOf[ThesisReview], "writer", writer).headOption.getOrElse(new ThesisReview)
    put("writer", writer)
    val checks = entityDao.findBy(classOf[CopyCheck], "writer" -> writer)
    put("checks", checks)
    put("review", review)
    put("blindReview", entityDao.findBy(classOf[BlindPeerReview], "writer", writer).headOption)
    if (review.advisorScore.isEmpty) {
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
      forward()
    }
  }

  def saveReview(): View = {
    val writer = entityDao.get(classOf[Writer], getLongId("writer"))
    val review = entityDao.findBy(classOf[ThesisReview], "writer", writer).headOption.getOrElse(new ThesisReview)
    review.writer = writer
    review.subjectScore = getInt("review.subjectScore")
    review.subjectScore = getInt("review.subjectScore")
    review.writeScore = getInt("review.writeScore")
    review.researchScore = getInt("review.researchScore")
    review.innovationScore = getInt("review.innovationScore")
    review.attitudeScore = getInt("review.attitudeScore")
    review.advisorReviewAt = Some(Instant.now)
    val finalScore = review.subjectScore.getOrElse(0) + review.writeScore.getOrElse(0) +
      review.researchScore.getOrElse(0) + review.innovationScore.getOrElse(0) + review.attitudeScore.getOrElse(0)

    val checks = entityDao.findBy(classOf[CopyCheck], "writer" -> writer, "recheck" -> false)
    val blindReview = entityDao.findBy(classOf[BlindPeerReview], "writer", writer).headOption
    if (checks.exists(!_.passed) && finalScore > 70) {
      redirect("review", s"writer.id=${writer.id}", "分数超过了70分")
    } else {
      blindReview match
        case None =>
          review.advisorSelfScore = Some(finalScore)
          review.advisorScore = Some(finalScore)
          review.blindReviewScore = None
          val msg = s"评阅了${writer.std.name}的论文，总分:${finalScore}"
          businessLogger.info(msg, writer.id, Map("writer.id" -> writer.id))
          entityDao.saveOrUpdate(review)
          redirect("index", "保存成功")
        case Some(r) =>
          r.score match
            case None => redirect("review", s"writer.id=${writer.id}", "校外送审成绩尚未公布，暂停打分")
            case Some(s) =>
              val avgScore = Numbers.round(finalScore * 0.4 + s * 0.6f, 0).toInt
              if (checks.exists(!_.passed) && avgScore > 70) {
                redirect("review", s"writer.id=${writer.id}", s"和校外送审汇总后，分数为${avgScore}超过了70分")
              } else {
                review.advisorSelfScore = Some(finalScore)
                review.blindReviewScore = Some(s)
                review.advisorScore = Some(avgScore)
                val msg = s"评阅了${writer.std.name}的论文，总分:${finalScore}, 结合校外送审成绩:${s},最终分:${avgScore}"
                businessLogger.info(msg, writer.id, Map("writer.id" -> writer.id))
                entityDao.saveOrUpdate(review)
                redirect("index", "保存成功")
              }
    }
  }
}
