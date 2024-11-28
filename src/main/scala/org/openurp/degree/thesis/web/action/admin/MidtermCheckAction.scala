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

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.collection.Order
import org.beangle.commons.file.zip.Zipper
import org.beangle.commons.io.{Files, IOs}
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.doc.transfer.exporter.ExportContext
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.view.{Status, Stream, View}
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}
import org.openurp.base.model.{AuditStatus, Project}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.DeferService
import org.openurp.degree.thesis.service.doc.ThesisDocGenerator
import org.openurp.degree.thesis.web.helper.MidtermCheckPropertyExtractor
import org.openurp.starter.web.support.ProjectSupport

import java.io.{File, FileOutputStream}

class MidtermCheckAction extends RestfulAction[Writer], ProjectSupport, ExportSupport[Writer] {
  var deferService: DeferService = _
  var businessLogger: WebBusinessLogger = _

  override def index(): View = {
    given project: Project = getProject

    put("departs", getDeparts)
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateOn desc")
    put("seasons", entityDao.search(gQuery))
    forward()
  }

  override protected def getQueryBuilder: OqlBuilder[Writer] = {
    given project: Project = getProject

    val departs = getDeparts
    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.std.state.department in(:departs) and writer.thesisTitle is not null", departs)
    populateConditions(query)
    query.limit(getPageLimit)
    query.orderBy(get(Order.OrderStr, "writer.std.code"))
    get("status") foreach { s =>
      if (Strings.isNotBlank(s)) {
        s match {
          case "教师审查通过" =>
            query.where("exists(from " + classOf[MidtermCheck].getName + " c where c.writer=writer and c.status=:status)", AuditStatus.PassedByAdvisor)
          case "教师审查未通过" =>
            query.where("exists(from " + classOf[MidtermCheck].getName + " c join c.details as d where c.writer=writer and d.passed=false)")
          case "待教师检查" =>
            query.where("exists(from " + classOf[MidtermCheck].getName + " c where c.writer=writer and size(c.details)=0)")
          case "未提交" =>
            query.where("not exists(from " + classOf[MidtermCheck].getName + " c where c.writer=writer)")
          case "学院审查通过" =>
            query.where("exists(from " + classOf[MidtermCheck].getName + " c where c.writer=writer and c.status=:passed)", AuditStatus.Passed)
          case "学院审查未通过" =>
            query.where("exists(from " + classOf[MidtermCheck].getName + " c where c.writer=writer and c.status=:unpassed)", AuditStatus.Rejected)
          case _ =>
        }
      }
    }
    query
  }

  override def search(): View = {
    val query = getQueryBuilder
    val writers = entityDao.search(query)
    put("writers", writers)

    val midtermChecks = entityDao.findBy(classOf[MidtermCheck], "writer", writers).map(x => (x.writer, x)).toMap
    put("midtermChecks", midtermChecks)
    put("stage", Stage.MidtermCheck)
    forward()
  }

  def reject(): View = {
    val writers = entityDao.find(classOf[Writer], getLongIds("writer"))
    val midtermChecks = entityDao.findBy(classOf[MidtermCheck], "writer", writers)
    midtermChecks.foreach { mc =>
      mc.status = AuditStatus.RejectedByDepart
      val msg = s"退回${mc.writer.std.name}的中期检查"
      businessLogger.info(msg, mc.id, Map("writer.std.code" -> mc.writer.std.code))
    }
    entityDao.saveOrUpdate(midtermChecks)
    redirect("search", "退回成功")
  }

  def defer(): View = {
    val writers = entityDao.find(classOf[Writer], getLongIds("writer"))
    writers.foreach { writer =>
      val deadline = writer.getOrCreateDeadline(Stage.MidtermCheck)
      deferService.defer(deadline, Stage.MidtermCheck)
    }
    redirect("search", "info.save.success")
  }

  @mapping(value = "{id}")
  override def info(@param("id") id: String): View = {
    val midtermCheck = entityDao.get(classOf[MidtermCheck], id.toLong)
    val writer = midtermCheck.writer
    put("midtermCheck", midtermCheck)
    put("writer", writer)
    forward()
  }

  def batchAuditSetting(): View = {
    val writerIds = getLongIds("writer")
    val query = OqlBuilder.from(classOf[MidtermCheck], "c")
    query.where("c.writer.id in(:ids)", writerIds)
    query.where("size(c.details)>0")
    val checks = entityDao.search(query)
    put("checks", checks)
    forward()
  }

  def batchAudit(): View = {
    val checks = entityDao.find(classOf[MidtermCheck], getLongIds("midtermCheck"))
    val passed = getBoolean("passed", true)
    val conclusion = get("conclusion")
    checks foreach { check =>
      if (check.details.nonEmpty) {
        check.status = if passed then AuditStatus.Passed else AuditStatus.Rejected
        check.conclusion = conclusion
      }
    }
    entityDao.saveOrUpdate(checks)
    redirect("search", "审核成功")
  }

  def doc(): View = {
    val writer = entityDao.get(classOf[Writer], getLongId("writer"))
    entityDao.findBy(classOf[MidtermCheck], "writer", writer).headOption match
      case Some(p) =>
        Stream(ThesisDocGenerator.genMidtermCheck(writer, Some(p)), MediaTypes.ApplicationDocx,
          s"${writer.std.code}${writer.std.name}中期检查表.docx")
      case None => Status.NotFound
  }

  def batchDoc(): View = {
    val writers = entityDao.find(classOf[Writer], getLongIds("writer"))
    val season = writers.head.season
    val depart = writers.head.department
    val dir = ThesisDocGenerator.getDepartFolder(season.id, depart.id, "tmp")
    writers foreach { writer =>
      entityDao.findBy(classOf[MidtermCheck], "writer", writer).headOption match
        case Some(p) =>
          val is = ThesisDocGenerator.genMidtermCheck(writer, Some(p))
          val file = new FileOutputStream(new File(dir + Files./ + s"${writer.std.code}中期检查表.docx"))
          IOs.copy(is, file)
          IOs.close(file)
        case None => Status.NotFound
    }
    val targetZip = ThesisDocGenerator.getDepartZipFile(season.id, depart.id, "tmp")
    Zipper.zip(new File(dir), targetZip)
    val fileName = season.name + "_" + depart.name + " 中期检查表.zip"
    Stream(targetZip, MediaTypes.ApplicationZip, fileName)
  }

  override protected def configExport(context: ExportContext): Unit = {
    context.extractor = new MidtermCheckPropertyExtractor(entityDao)
    super.configExport(context)
  }
}
