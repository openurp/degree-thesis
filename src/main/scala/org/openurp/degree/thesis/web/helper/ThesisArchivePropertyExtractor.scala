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

import org.beangle.commons.bean.{DefaultPropertyExtractor, Properties}
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.EntityDao
import org.openurp.degree.thesis.model.{ThesisArchive, ThesisPaper, ThesisReview}

class ThesisArchivePropertyExtractor(entityDao: EntityDao) extends DefaultPropertyExtractor {
  override def get(target: Object, property: String): Any = {
    if (property.startsWith("paper.")) {
      val p = Strings.substringAfter(property, "paper.")
      val paper = entityDao.findBy(classOf[ThesisPaper], "writer", target.asInstanceOf[ThesisArchive].writer).head
      Properties.get[Any](paper, p)
    } else if (property.startsWith("review.")) {
      val p = Strings.substringAfter(property, "review.")
      val review = entityDao.findBy(classOf[ThesisReview], "writer", target.asInstanceOf[ThesisArchive].writer).head
      Properties.get[Any](review, p)
    } else {
      Properties.get[Any](target, property)
    }
  }
}
