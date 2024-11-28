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
import org.openurp.degree.thesis.model.*

import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DefenseWriterPropertyExtractor(entityDao: EntityDao) extends DefaultPropertyExtractor {
  val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  override def get(target: Object, property: String): Any = {
    val dw = target.asInstanceOf[DefenseWriter]
    if (property.startsWith("group.leader")) {
      dw.group.leaderTeacher.map(_.name).getOrElse("")
    } else if (property.startsWith("group.members")) {
      dw.group.memberTeachers.map(_.name).mkString(" ")
    } else if (property.startsWith("group.time")) {
      if (dw.group.beginAt.nonEmpty) {
        dateTimeFormatter.format(dw.group.beginAt.get.atZone(ZoneId.systemDefault())) + "~" + timeFormatter.format(dw.group.endAt.get.atZone(ZoneId.systemDefault()))
      } else {
        ""
      }
    } else {
      Properties.get[Any](dw, property)
    }
  }
}
