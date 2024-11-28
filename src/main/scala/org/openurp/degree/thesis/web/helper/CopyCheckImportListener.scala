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

import org.beangle.commons.conversion.string.{BooleanConverter, TemporalConverter}
import org.beangle.data.dao.EntityDao
import org.beangle.doc.transfer.importer.{ImportListener, ImportResult}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{CopyCheck, ThesisPaper, Writer}

import java.time.{Instant, LocalDate}

class CopyCheckImportListener(season: GraduateSeason, entityDao: EntityDao) extends ImportListener {
  override def onStart(tr: ImportResult): Unit = {}

  override def onFinish(tr: ImportResult): Unit = {}

  override def onItemStart(tr: ImportResult): Unit = {
    transfer.curData.get("writer.std.code") foreach { code =>
      val writers = entityDao.findBy(classOf[Writer], "std.code" -> code, "season" -> season)
      if (writers.nonEmpty) {
        val recheck = BooleanConverter.apply(transfer.curData.get("copyCheck.recheck").getOrElse("N").toString)
        val checks = entityDao.findBy(classOf[CopyCheck], "writer" -> writers.head, "recheck" -> recheck)
        if (checks.nonEmpty) {
          transfer.current = checks.head
        } else {
          val check = new CopyCheck
          check.writer = writers.head
          transfer.current = check
        }
      } else {
        tr.addFailure("错误的学号", code)
      }
    }
  }

  override def onItemFinish(tr: ImportResult): Unit = {
    val check = transfer.current.asInstanceOf[CopyCheck]
    entityDao.saveOrUpdate(check)
    entityDao.findBy(classOf[ThesisPaper], "writer", check.writer) foreach { paper =>
      if check.recheck then
        paper.recheck = Some(check)
        paper.copyCheck match {
          case None => paper.copyCheck = entityDao.findBy(classOf[CopyCheck], "writer" -> check.writer, "recheck" -> false).headOption
          case Some(first) => if first == check then paper.copyCheck = None
        }
      else
        paper.copyCheck = Some(check)
        if paper.recheck.contains(check) then paper.recheck = None

      entityDao.saveOrUpdate(paper)
    }
  }
}
