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

import org.beangle.data.dao.EntityDao
import org.beangle.doc.transfer.importer.{ImportListener, ImportResult}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{BlindPeerReview, ThesisPaper, Writer}

import java.time.Instant

class BlindPeerReviewImportListener(season: GraduateSeason, entityDao: EntityDao) extends ImportListener {
  override def onStart(tr: ImportResult): Unit = {}

  override def onFinish(tr: ImportResult): Unit = {}

  override def onItemStart(tr: ImportResult): Unit = {
    transfer.curData.get("writer.std.code") foreach { code =>
      val writers = entityDao.findBy(classOf[Writer], "std.code" -> code, "season" -> season)
      if (writers.nonEmpty) {
        val reviews = entityDao.findBy(classOf[BlindPeerReview], "writer" -> writers.head)
        if (reviews.nonEmpty && reviews.size == 1) {
          transfer.current = reviews.head
        } else {
          entityDao.findBy(classOf[ThesisPaper], "writer", writers.head).headOption match
            case None =>
              tr.addFailure("该学生没有上传论文", code)
            case Some(paper) =>
              val review = new BlindPeerReview
              review.writer = writers.head
              review.remark = Some("人工导入")
              review.updatedAt = Instant.now
              transfer.current = review
        }
      } else {
        tr.addFailure("错误的学号", code)
      }
    }
  }

  override def onItemFinish(tr: ImportResult): Unit = {
    val review = transfer.current.asInstanceOf[BlindPeerReview]
    entityDao.saveOrUpdate(review)
  }
}
