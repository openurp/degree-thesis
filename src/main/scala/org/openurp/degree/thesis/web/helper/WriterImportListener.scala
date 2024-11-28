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
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.Writer

class WriterImportListener(entityDao: EntityDao, season: GraduateSeason) extends ImportListener {

  override def onItemStart(tr: ImportResult): Unit = {
    transfer.curData.get("writer.std.code") foreach { code =>
      val query = OqlBuilder.from(classOf[Writer], "w")
      query.where("w.season=:season and w.std.code=:code", season, code)
      val writers = entityDao.search(query)
      if (writers.nonEmpty) {
        transfer.current = writers.head
      }
    }
  }

  override def onItemFinish(tr: ImportResult): Unit = {
    val writer = transfer.current.asInstanceOf[Writer]
    if (null != writer.std && writer.std.persisted) {
      writer.season = season
      entityDao.saveOrUpdate(writer)
    }
  }
}
