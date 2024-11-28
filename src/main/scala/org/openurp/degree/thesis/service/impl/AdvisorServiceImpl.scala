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

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.openurp.base.model.Department
import org.openurp.degree.thesis.model.Advisor
import org.openurp.degree.thesis.service.AdvisorService

import java.time.LocalDate

class AdvisorServiceImpl extends AdvisorService {

  var entityDao: EntityDao = _

  override def getAdvisors(departs: Iterable[Department]): Seq[Advisor] = {
    val aQuery = OqlBuilder.from(classOf[Advisor], "advisor")
    aQuery.where("exists(from advisor.departs as d where d in(:departs))", departs)
      .orderBy("advisor.teacher.department,advisor.teacher.name")
    aQuery.where("advisor.endOn is null or :now <= advisor.endOn", LocalDate.now)
    entityDao.search(aQuery)
  }
}
