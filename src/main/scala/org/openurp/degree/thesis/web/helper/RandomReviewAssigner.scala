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

package org.openurp.degree.thesis.web.helper

import org.beangle.commons.collection.Collections
import org.openurp.base.edu.model.{Major, TeachingOffice}
import org.openurp.base.hr.model.Teacher
import org.openurp.base.model.Department
import org.openurp.degree.thesis.model.{Advisor, ThesisReview}

import scala.util.Random

object RandomReviewAssigner {

  def assign(reviews: Iterable[ThesisReview], reviewers: Iterable[Teacher]): Unit = {
    var reviews2 = Random.shuffle(reviews).toList
    val reviewers2 = Random.shuffle(reviewers)
    while (reviews2.nonEmpty) {
      val aIter = reviewers2.iterator
      val header = reviews2.head
      while (aIter.hasNext && reviews2.nonEmpty) {
        val reviewer = aIter.next()
        val r = reviews2.head
        if (!r.writer.advisor.map(_.teacher).contains(reviewer)) {
          r.crossReviewer = Some(reviewer)
          reviews2 = reviews2.tail
        }
      }
      if (reviews2.nonEmpty && reviews2.head == header) {
        reviews2 = reviews2.tail
      }
    }
  }

  def assign2(reviews: Iterable[ThesisReview], departReviewers: Map[Department, Seq[Teacher]], counts: Map[Long, Int]): Unit = {
    val reviewCounts = Collections.newMap[Teacher, ReviewCount]
    departReviewers foreach { case (_, ts) =>
      ts foreach { t => reviewCounts.getOrElseUpdate(t, new ReviewCount(t, counts.getOrElse(t.id, 0))) }
    }
    val departReviewCounts = departReviewers.map(x => (x._1, x._2.map(reviewCounts(_))))

    reviews foreach { review =>
      val myAdvisor = review.writer.advisor.get.teacher
      val office: TeachingOffice = myAdvisor.office.orNull
      val reviewCnts = departReviewCounts.getOrElse(review.writer.std.department, List.empty)
      var matched =
        if (null == office) reviewCnts.filter { r => r.teacher != myAdvisor }
        else reviewCnts.filter { r => r.teacher.office.contains(office) && r.teacher != myAdvisor }

      if (matched.nonEmpty) {
        matched = matched.sorted
        val minCount = matched.head.count
        matched = matched.filter(_.count == minCount)
        matched = Random.shuffle(matched)
        val matchedReviewer = matched.head
        review.crossReviewer = Some(matchedReviewer.teacher)
        matchedReviewer.count += 1
      }
    }
  }

  class ReviewCount(val teacher: Teacher, var count: Int) extends Ordered[ReviewCount] {
    override def compare(that: ReviewCount): Int = this.count - that.count
  }
}
