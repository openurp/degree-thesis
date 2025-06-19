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
import org.beangle.commons.conversion.string.BooleanConverter
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.doc.transfer.exporter.ExportContext
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.{EntityAction, ExportSupport}
import org.openurp.base.model.Project
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.{DeferService, ThesisPlanService}
import org.openurp.degree.thesis.web.helper.CommitmentPropertyExtractor
import org.openurp.starter.web.support.ProjectSupport

class CommitmentAction extends ActionSupport, EntityAction[Writer], ExportSupport[Writer], ProjectSupport {
  var entityDao: EntityDao = _
  var deferService: DeferService = _
  var thesisPlanService: ThesisPlanService = _

  def index(): View = {
    given project: Project = getProject

    put("departs", getDeparts)
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateIn desc")
    put("seasons", entityDao.search(gQuery))
    forward()
  }

  override def getQueryBuilder: OqlBuilder[Writer] = {
    given project: Project = getProject

    val departs = getDeparts
    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.std.state.department in(:departs)", departs)
    populateConditions(query)
    get("confirmed") foreach { confirmed =>
      if (Strings.isNotBlank(confirmed)) {
        if (BooleanConverter(confirmed)) {
          query.where("exists(from " + classOf[Commitment].getName + " c where c.writer=writer and c.confirmed=true)")
        } else {
          query.where("not exists(from " + classOf[Commitment].getName + " c where c.writer=writer and c.confirmed=true)")
        }
      }
    }
    query.orderBy(get(Order.OrderStr, "writer.std.code"))
    query.limit(getPageLimit)
    query
  }

  def search(): View = {
    val query = getQueryBuilder
    val writers = entityDao.search(query)
    put("writers", writers)

    val commitments = entityDao.findBy(classOf[Commitment], "writer", writers).map(x => (x.writer, x)).toMap
    put("commitments", commitments)
    put("stage", Stage.Commitment)
    forward()
  }

  def defer(): View = {
    val writers = entityDao.find(classOf[Writer], getLongIds("writer"))
    writers.foreach { writer =>
      val deadline = writer.getOrCreateDeadline(Stage.Commitment)
      deferService.defer(deadline, Stage.Commitment)
    }
    redirect("search", "延期成功")
  }

  @mapping(value = "{id}")
  def info(@param("id") id: String): View = {
    val commitment = entityDao.get(classOf[Commitment], id.toLong)
    val writer = commitment.writer
    put("commitment", commitment)

    put("writer", writer)
    put("deadline", writer.getOrCreateDeadline(Stage.Commitment))
    put("plan", thesisPlanService.getDepartPlan(writer))
    forward()
  }

  override protected def configExport(context: ExportContext): Unit = {
    context.extractor = new CommitmentPropertyExtractor(entityDao)
    super.configExport(context)
  }
}
