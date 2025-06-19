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
import org.beangle.commons.lang.{Enums, Strings}
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
import org.openurp.degree.thesis.service.doc.{ThesisDocGenerator, ThesisPdfGenerator}
import org.openurp.degree.thesis.web.helper.ProposalPropertyExtractor
import org.openurp.starter.web.support.ProjectSupport

import java.io.{File, FileOutputStream}

class ProposalAction extends RestfulAction[Writer], ProjectSupport, ExportSupport[Writer] {

  var deferService: DeferService = _
  var businessLogger: WebBusinessLogger = _

  override def index(): View = {
    given project: Project = getProject

    put("departs", getDeparts)
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateIn desc")
    put("seasons", entityDao.search(gQuery))
    forward()
  }

  override protected def getQueryBuilder: OqlBuilder[Writer] = {
    given project: Project = getProject

    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.std.state.department in(:departs) and writer.advisor is not null", getDeparts)
    populateConditions(query)
    query.limit(getPageLimit)
    query.orderBy(get(Order.OrderStr, "writer.std.code"))
    get("status") foreach { s =>
      if (Strings.isNotBlank(s)) {
        val status = Enums.of(classOf[AuditStatus], s.toInt).get
        if (status == AuditStatus.Draft)
          query.where(" not exists(from " + classOf[Proposal].getName +
            " c where c.writer=writer and c.status in(:statusList))", List(AuditStatus.Rejected, AuditStatus.Submited, AuditStatus.Passed))
        else
          query.where("exists(from " + classOf[Proposal].getName + " c where c.writer=writer and c.status=:status)", status)
      }
    }
    query
  }

  override def search(): View = {
    val writers = entityDao.search(getQueryBuilder)
    put("writers", writers)

    val proposals = entityDao.findBy(classOf[Proposal], "writer", writers).map(x => (x.writer, x)).toMap
    put("proposals", proposals)
    put("stage", Stage.Proposal)
    forward()
  }

  def reject(): View = {
    val writers = entityDao.find(classOf[Writer], getLongIds("writer"))
    val proposals = entityDao.findBy(classOf[Proposal], "writer", writers)
    proposals.foreach { p =>
      p.status = AuditStatus.RejectedByDepart
      val msg = s"退回${p.writer.std.name}的开题报告"
      businessLogger.info(msg, p.id, Map("writer.std.code" -> p.writer.std.code))
    }
    entityDao.saveOrUpdate(proposals)
    redirect("search", "退回成功")
  }

  def defer(): View = {
    val writers = entityDao.find(classOf[Writer], getLongIds("writer"))
    writers.foreach { writer =>
      val deadline = writer.getOrCreateDeadline(Stage.Proposal)
      deferService.defer(deadline, Stage.Proposal)
    }
    redirect("search", "info.save.success")
  }

  @mapping(value = "{id}")
  override def info(@param("id") id: String): View = {
    val proposal = entityDao.get(classOf[Proposal], id.toLong)
    val writer = proposal.writer
    put("proposal", proposal)
    put("writer", writer)
    forward()
  }

  def doc(): View = {
    val writer = entityDao.get(classOf[Writer], getLongId("writer"))
    entityDao.findBy(classOf[Proposal], "writer", writer).headOption match
      case Some(p) =>
        Stream(ThesisDocGenerator.genProposal(p, None), MediaTypes.ApplicationDocx,
          s"${writer.std.code}${writer.std.name}开题报告.docx")
      case None => Status.NotFound
  }

  /** 批量下载word开题报告
   *
   * @return
   */
  def batchDoc(): View = {
    val writers = entityDao.find(classOf[Writer], getLongIds("writer"))
    val season = writers.head.season
    val depart = writers.head.department
    val dir = ThesisDocGenerator.getDepartFolder(season.id, depart.id, "tmp")
    writers foreach { writer =>
      entityDao.findBy(classOf[Proposal], "writer", writer).headOption match
        case Some(p) =>
          val is = ThesisDocGenerator.genProposal(p, None)
          val file = new FileOutputStream(new File(dir + Files./ + s"${writer.std.code}开题报告.docx"))
          IOs.copy(is, file)
          IOs.close(file)
        case None => Status.NotFound
    }
    val targetZip = ThesisDocGenerator.getDepartZipFile(season.id, depart.id, "tmp")
    Zipper.zip(new File(dir), targetZip)
    val fileName = season.name + "_" + depart.name + " 开题报告Docx.zip"
    Stream(targetZip, MediaTypes.ApplicationZip, fileName)
  }

  /** 批量下载pdf开题报告
   *
   * @return
   */
  def batchPdf(): View = {
    val writers = entityDao.find(classOf[Writer], getLongIds("writer"))
    val season = writers.head.season
    val depart = writers.head.department
    val dir = ThesisPdfGenerator.getDepartFolder(season.id, depart.id, "tmp")
    writers foreach { writer =>
      entityDao.findBy(classOf[Proposal], "writer", writer).headOption match
        case Some(p) =>
          val is = ThesisPdfGenerator.genProposal(p)
          val file = new FileOutputStream(new File(dir + Files./ + s"${writer.std.code}开题报告.pdf"))
          IOs.copy(is, file)
          IOs.close(file)
        case None => Status.NotFound
    }
    val targetZip = ThesisPdfGenerator.getDepartZipFile(season.id, depart.id, "tmp")
    Zipper.zip(new File(dir), targetZip)
    val fileName = season.name + "_" + depart.name + " 开题报告PDF.zip"
    Stream(targetZip, MediaTypes.ApplicationZip, fileName)
  }

  override protected def configExport(context: ExportContext): Unit = {
    context.extractor = new ProposalPropertyExtractor(entityDao)
    super.configExport(context)
  }
}
