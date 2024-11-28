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

import org.beangle.commons.bean.Properties
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.EntityDao
import org.beangle.commons.bean.DefaultPropertyExtractor
import org.openurp.degree.thesis.model.{Commitment, Writer}

class CommitmentPropertyExtractor(entityDao: EntityDao) extends DefaultPropertyExtractor {
  override def get(target: Object, property: String): Any = {
    var writer = target.asInstanceOf[Writer]
    if (property.startsWith("commitment.confirmed")) {
      val commitments = entityDao.findBy(classOf[Commitment], "writer" -> writer)
      if commitments.isEmpty then "否"
      else if (commitments.head.confirmed) "是" else "否"
      //Properties.get[Any](commitments.head, Strings.substringAfter(property, "commitment."))
    } else {
      Properties.get[Any](writer, property)
    }
  }
}
