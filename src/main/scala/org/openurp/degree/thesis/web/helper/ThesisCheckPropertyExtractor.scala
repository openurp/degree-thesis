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
import org.openurp.degree.thesis.model.ThesisCheck
import org.beangle.commons.bean.DefaultPropertyExtractor
class ThesisCheckPropertyExtractor(entityDao: EntityDao) extends DefaultPropertyExtractor {
  override def get(target: Object, property: String): Any = {
    val check = target.asInstanceOf[ThesisCheck]
    property match
      case "paperFileName" => ThesisCheckFileNaming.paperFileName(check)
      case "attachFileName" => ThesisCheckFileNaming.attachFileName(check)
      case _ =>
        val v = org.beangle.commons.lang.Options.unwrap(super.get(target, property))
        v match
          case null => ""
          case b: Boolean => if b then "是" else "否"
          case _ => v
  }
}
