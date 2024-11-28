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

import org.beangle.commons.collection.Collections
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.openurp.base.model.Department
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{DepartPlan, Stage, ThesisPlan, Writer}
import org.openurp.degree.thesis.service.ThesisPlanService

import java.time.{Instant, LocalDate}

class ThesisPlanServiceImpl extends ThesisPlanService {

  var entityDao: EntityDao = _

  override def getPlan(): Option[ThesisPlan] = {
    val query = OqlBuilder.from(classOf[ThesisPlan], "tp")
    query.where(":today between tp.beginOn and tp.endOn", LocalDate.now)
    query.cacheable()
    val intimePlans = entityDao.search(query)
    if (intimePlans.isEmpty) {
      val query = OqlBuilder.from(classOf[ThesisPlan], "tp")
      query.orderBy("tp.beginOn desc")
      query.cacheable()
      entityDao.search(query).headOption
    } else {
      intimePlans.headOption
    }
  }

  override def getSeason(): GraduateSeason = {
    getPlan().get.season
  }

  override def isOpen(plan: ThesisPlan, depart: Department, stage: Stage): Boolean = {
    plan.departPlans.find(x => x.department == depart) match {
      case None => false
      case Some(dp) =>
        dp.times.find(x => x.stage == stage) match {
          case None => false
          case Some(st) => st.timeSuitable(Instant.now) == 0
        }
    }
  }

  override def getPlanDeparts(ds: Seq[Department]): Seq[Department] = {
    getPlan() match {
      case Some(p) =>
        val allDeparts = p.departPlans.map(_.department)
        Collections.intersection(allDeparts, ds)
      case None => List.empty
    }
  }

  override def getDepartPlan(writer: Writer): Option[DepartPlan] = {
    val depart = writer.department
    val season = writer.season
    entityDao.findBy(classOf[DepartPlan], "department" -> depart, "thesisPlan.season" -> season).headOption
  }
}
