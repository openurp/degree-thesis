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

package org.openurp.degree.thesis.web.action.admin

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.doc.transfer.exporter.ExportContext
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.{EntityAction, ExportSupport}
import org.beangle.webmvc.support.helper.QueryHelper
import org.openurp.base.hr.model.Teacher
import org.openurp.base.model.Project
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{Advisor, Writer}
import org.openurp.degree.thesis.web.helper.AdvisorSubjectStat
import org.openurp.starter.web.support.ProjectSupport

class TitleStatAction extends ActionSupport, EntityAction[AdvisorSubjectStat], ProjectSupport, ExportSupport[AdvisorSubjectStat] {

  var entityDao: EntityDao = _

  def index(): View = {
    given project: Project = getProject

    val seasons = entityDao.getAll(classOf[GraduateSeason])
    put("seasons", seasons.sortBy(_.graduateOn))
    val query = OqlBuilder.from(classOf[Teacher], "t")
    query.where("t.staff.school=:school", project.school)
    query.select("distinct t.staff.title")
    query.orderBy("t.staff.title.code")

    put("departs", getDeparts)
    put("titles", entityDao.search(query))
    forward()
  }

  def stat(): View = {
    val statByAdvisor = getBoolean("statByAdvisor", true)
    put("stats", getQueryData(statByAdvisor))
    if (statByAdvisor) forward("byAdvisor") else forward("bySeason")
  }

  private def getQueryData(statByAdvisor: Boolean): collection.Seq[AdvisorSubjectStat] = {
    val fromSeason = entityDao.get(classOf[GraduateSeason], getLongId("fromSeason"))
    val toSeason = entityDao.get(classOf[GraduateSeason], getLongId("toSeason"))

    put("fromSeason", fromSeason)
    put("toSeason", toSeason)
    val query = OqlBuilder.from[Array[Object]](classOf[Writer].getName, "writer")
    query.where("writer.season.graduateOn between :startYear and :endYear", fromSeason.graduateOn, toSeason.graduateOn)
    query.where("writer.advisor is not null")
    QueryHelper.populate(query)
    if (statByAdvisor) {
      query.groupBy("writer.advisor.id")
      query.select("writer.advisor.id,count(*)")
    } else {
      query.groupBy("writer.season.id,writer.advisor.id")
      query.select("writer.season.id,writer.advisor.id,count(*)")
    }
    val results = entityDao.search(query)
    val stats = results.map { line =>
      if (statByAdvisor) {
        val advisor = entityDao.get(classOf[Advisor], line(0).asInstanceOf[Long])
        AdvisorSubjectStat(advisor, line(1).asInstanceOf[Number].intValue)
      } else {
        val season = entityDao.get(classOf[GraduateSeason], line(0).asInstanceOf[Number].intValue())
        val advisor = entityDao.get(classOf[Advisor], line(1).asInstanceOf[Long])
        AdvisorSubjectStat(season, advisor, line(2).asInstanceOf[Number].intValue)
      }
    }
    if (statByAdvisor) {
      stats.toBuffer.sortBy(x => x.advisor.teacher.department.code + x.advisor.teacher.code)
    } else {
      stats.toBuffer.sortBy(x => x.advisor.teacher.department.code + x.advisor.teacher.code + x.season.graduateOn.toString)
    }
  }

  protected override def configExport(context: ExportContext): Unit = {
    context.put("items", getQueryData(getBoolean("statByAdvisor", true)))
  }

  def writers(): View = {
    val advisor = entityDao.get(classOf[Advisor], getLong("advisorId").get)
    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.thesisTitle is not null and writer.advisor = :advisor", advisor)
    val writers = entityDao.search(query)
    val seasonWriters = writers.groupBy(_.season)
    put("seasonWriters", seasonWriters)
    put("advisor", advisor)
    forward()
  }
}
