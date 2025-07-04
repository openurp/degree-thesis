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
import org.beangle.data.dao.EntityDao
import org.openurp.degree.thesis.model.{Proposal, Writer}

class ProposalPropertyExtractor(entityDao: EntityDao) extends DefaultPropertyExtractor {
  var proposal: Proposal = _

  override def get(target: Object, property: String): Any = {
    val writer = target.asInstanceOf[Writer]
    if (null == proposal || proposal.writer != writer) {
      val proposals = entityDao.findBy(classOf[Proposal], "writer" -> writer)
      if (proposals.nonEmpty) proposal = proposals.head else proposal = null
    }

    if (property.startsWith("proposal.")) {
      val p = property.substring("proposal.".length)
      p match
        case "status" => if null == proposal then "未提交审查" else proposal.status.toString
        case _ => if (null == proposal) then "" else Properties.get[Any](proposal, p)
    } else {
      Properties.get[Any](writer, property)
    }
  }
}
