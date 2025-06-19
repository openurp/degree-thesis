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

package org.openurp.degree.thesis.service.impl

import org.beangle.data.dao.EntityDao
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.ThesisCheckService

class ThesisCheckServiceImpl extends ThesisCheckService {
  var entityDao: EntityDao = _

  override def findWriter(code: String): Option[Writer] = {
    val writers = entityDao.findBy(classOf[Writer], "std.code" -> code)
    writers.sortBy(_.season.graduateIn).reverse.headOption
  }

  override def updateDoc(check: ThesisCheck): Unit = {
    val writer = check.writer
    val docs = entityDao.findBy(classOf[ThesisDoc], "writer", writer)
    check.coverDoc = docs.find(_.docType.code == "cover")
    check.proposalDoc = docs.find(_.docType.code == "proposal")
    check.defenseDoc = docs.find(_.docType.code == "oralDefense")
    check.paperDoc = docs.find(_.docType.code == "thesisPaper")
    entityDao.saveOrUpdate(check)
  }
}
