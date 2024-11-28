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
import org.openurp.base.edu.model.Major
import org.openurp.base.model.Department
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{Advisor, Subject, Writer}
import org.openurp.degree.thesis.service.SubjectService

class SubjectServiceImpl extends SubjectService {

  var entityDao: EntityDao = _

  override def getMajors(season: GraduateSeason, departs: Iterable[Department]): Seq[Major] = {
    if (departs.isEmpty) return List.empty

    val query = OqlBuilder.from[Major](classOf[Writer].getName, "w")
    query.where("w.std.state.department in(:departments)", departs)
    query.where("w.season=:season", season)
    query.select("distinct w.std.state.major")
    query.orderBy("w.std.state.major.code")
    entityDao.search(query)
  }

  override def getFreeQuota(season: GraduateSeason, advisor: Advisor): Int = {
    val size = entityDao.count(classOf[Subject], "advisor" -> advisor, "season" -> season)
    (advisor.maxWriters - size).toInt
  }

  override def duplicate(season: GraduateSeason, subject: Subject): Boolean = {
    val id = subject.id
    val query = OqlBuilder.from(classOf[Subject], "subject")
      .where("subject.id!=:id and subject.name=:name", id, subject.name)
      .where("subject.season=:season", season)
    entityDao.search(query).nonEmpty
  }

}
