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

import org.beangle.commons.collection.Order
import org.beangle.commons.conversion.string.EnumConverters
import org.beangle.data.dao.OqlBuilder
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.RestfulAction
import org.openurp.base.model.Project
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.ThesisPlanService
import org.openurp.starter.web.support.ProjectSupport

class GuidanceAction extends RestfulAction[Guidance] with ProjectSupport {
  var thesisPlanService: ThesisPlanService = _

  override def index(): View = {
    given project: Project = getProject

    put("departs", getDeparts)
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateOn desc")
    put("seasons", entityDao.search(gQuery))
    forward()
  }

  override def search(): View = {
    given project: Project = getProject

    val departs = getDeparts
    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.std.state.department in(:departs) and writer.thesisTitle is not null", departs)
    populateConditions(query)
    query.limit(getPageLimit)
    query.orderBy(get(Order.OrderStr, "writer.std.code"))
    //每个提交有两次
    getInt("guidance_times") foreach { time =>
      query.where("(select count(*) from " + classOf[Guidance].getName + s" g where g.writer=writer)=${time}")
    }
    val writers = entityDao.search(query)

    val gQuery = OqlBuilder.from[Array[Any]](classOf[Guidance].getName, "g")
      .where("g.writer.std.state.department in(:departs)", departs)
    gQuery.select("g.writer.id,g.stage").groupBy("g.writer.id,g.stage")
    val stats = entityDao.search(gQuery).groupBy(x => x(0)).map { case (k, v) => (k, v.map(x => x(1))) }

    put("stats", stats)
    put("writers", writers)
    forward()
  }

  @mapping(value = "{writerId}/{stageId}")
  def view(@param("writerId") writerId: String, @param("stageId") stageId: String): View = {
    val writer = entityDao.get(classOf[Writer], writerId.toLong)
    val stage = EnumConverters.convert(stageId, classOf[Stage])
    val guidances = entityDao.findBy(classOf[Guidance], "writer" -> writer, "stage" -> stage)
    put("guidances", guidances)
    put("writer", writer)
    put("stage", stage)
    put("plan", thesisPlanService.getDepartPlan(writer))
    forward()
  }
}
