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

import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.doc.transfer.exporter.ExportContext
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}
import org.beangle.webmvc.support.helper.QueryHelper
import org.openurp.base.edu.model.TeachingOffice
import org.openurp.base.model.Project
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.web.helper.DefenseWriterPropertyExtractor
import org.openurp.starter.web.support.ProjectSupport

class DefenseWriterAction extends RestfulAction[DefenseWriter], ExportSupport[DefenseWriter], ProjectSupport {
  var businessLogger: WebBusinessLogger = _

  override def indexSetting(): Unit = {
    given project: Project = getProject

    val departs = getDeparts
    put("departs", departs)
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateIn desc")
    put("seasons", entityDao.search(gQuery))
  }

  override protected def getQueryBuilder: OqlBuilder[DefenseWriter] = {
    val query = super.getQueryBuilder

    given project: Project = getProject

    query.where("defenseWriter.group.department in(:departs)", getDeparts)
    getBoolean("copyCheck") foreach { copyCheck =>
      val prefix = if (copyCheck) "" else " not "
      query.where(prefix + s" exists(from ${classOf[CopyCheck].getName} cc where cc.writer=defenseWriter.writer and cc.passed=true)")
    }
    getBoolean("hasPaper") foreach { hasPaper =>
      val prefix = if (hasPaper) "" else " not "
      query.where(prefix + s" exists(from ${classOf[ThesisPaper].getName} p where p.writer=defenseWriter.writer)")
    }
    query
  }

  def emptyGroupWriter(): View = {
    given project: Project = getProject

    val departs = getDeparts

    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where(s"not exists(from ${classOf[DefenseWriter].getName} dw where dw.writer=writer)")
    query.where("writer.std.state.department in(:departs)", departs)
    query.limit(getPageLimit)
    getLong("defenseWriter.writer.season.id") foreach { seasonId =>
      query.where("writer.season.id=:seasonId", seasonId)
    }
    populateConditions(query)
    QueryHelper.sort(query)
    put("writers", entityDao.search(query))
    forward()
  }

  override protected def removeAndRedirect(entities: Seq[DefenseWriter]): View = {
    val query = OqlBuilder.from(classOf[DefenseWriter], "dw")
    query.where("dw.id in(:ids)", entities.map(_.id))
    query.where(s"not exists(from ${classOf[CopyCheck].getName} cc where cc.writer=dw.writer and cc.passed=true)")
    val failed = entityDao.search(query)
    failed foreach { removed =>
      val group = removed.group
      group.removeWriter(List(removed.writer))
      entityDao.saveOrUpdate(group)
      val msg = s"因无反抄袭检测通过的论文,从第${group.idx}组中移除${removed.writer.std.name}"
      businessLogger.info(msg, group.id, Map("writer.std.code" -> removed.writer.std.code, "group.id" -> group.id.toString))
    }

    redirect("search", "info.remove.success")
  }

  override protected def configExport(context: ExportContext): Unit = {
    context.extractor = new DefenseWriterPropertyExtractor(entityDao)
    super.configExport(context)
  }
}
