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

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.doc.transfer.importer.{ImportListener, ImportResult}
import org.openurp.base.model.School
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{Advisor, ThesisReview, Writer}

class ThesisReviewImportListener(season: GraduateSeason, entityDao: EntityDao) extends ImportListener {
  override def onItemStart(tr: ImportResult): Unit = {
    transfer.curData.get("writer.std.code") foreach { code =>
      findWriter(code.toString, season) match {
        case None => tr.addFailure(s"找不到学生信息", code)
        case Some(w) =>
          val query = OqlBuilder.from(classOf[ThesisReview], "c")
          query.where("c.writer=:writer", w)
          val review = entityDao.search(query).headOption match
            case None =>
              val c = new ThesisReview
              c.writer = w
              c
            case Some(tp) => tp
          transfer.current = review
      }
    }
  }

  private def findWriter(code: String, season: GraduateSeason): Option[Writer] = {
    entityDao.findBy(classOf[Writer], "std.code" -> code, "season" -> season).headOption
  }

  private def findAdvisor(code: String, school: School): Option[Advisor] = {
    entityDao.findBy(classOf[Advisor], "teacher.staff.code" -> code, "teacher.staff.school" -> school).headOption
  }

  override def onItemFinish(tr: ImportResult): Unit = {
    val c = transfer.current.asInstanceOf[ThesisReview]
    if (null != c.writer) {
      c.finalScore foreach { s =>
        c.finalScoreText = Some(ScoreTextHelper.convert(s))
      }
      transfer.curData.get("advisor.teacher.staff.code") foreach { code =>
        findAdvisor(code.toString, c.writer.std.project.school) foreach { a =>
          c.writer.advisor = Some(a)
          entityDao.saveOrUpdate(c)
        }
      }
      entityDao.saveOrUpdate(c)
    }
  }
}
