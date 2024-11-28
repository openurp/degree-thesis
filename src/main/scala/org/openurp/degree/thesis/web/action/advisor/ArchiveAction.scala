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

package org.openurp.degree.thesis.web.action.advisor

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.io.Files
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.EmsApp
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.EntitySupport
import org.beangle.webmvc.view.{Stream, View}
import org.openurp.base.model.User
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.doc.ThesisPdfGenerator
import org.openurp.degree.thesis.web.helper.PaperDownloadHelper

import java.io.File
import java.time.Instant

class ArchiveAction extends AdvisorSupport, EntitySupport[ThesisArchive] {
  def index(): View = {
    val query = OqlBuilder.from(classOf[ThesisPlan], "tp")
    query.orderBy("tp.beginOn desc")
    query.cacheable()
    val plans = entityDao.search(query)

    val planId = getLong("plan.id")
    val plan =
      planId match
        case None => plans.head
        case Some(pid) => plans.find(_.id == pid).head

    put("plan", plan)
    put("plans",plans)
    val advisor = getAdvisor
    val writers = getWriters(plan)
    val archivesMap = entityDao.findBy(classOf[ThesisArchive], "writer", writers).filter(_.uploadAt.nonEmpty).map(x => (x.writer, x)).toMap
    put("archives", archivesMap)
    put("writers", writers)

    val docs = entityDao.findBy(classOf[ThesisDoc], "writer", writers)
    put("docs", docs.groupBy(_.writer))
    put("withoutArchives", writers.toBuffer.subtractAll(archivesMap.keySet))
    forward()
  }

  def auditSetting(): View = {
    val writer = entityDao.get(classOf[Writer], getLongId("writer"))
    val docs = entityDao.findBy(classOf[ThesisDoc], "writer", writer)
    val paper = entityDao.findBy(classOf[ThesisPaper], "writer", writer).headOption
    put("writer", writer)
    put("paper", paper)
    put("docs", docs)
    put("archive", entityDao.findBy(classOf[ThesisArchive], "writer", writer).headOption)
    forward()
  }

  def preview(): View = {
    val doc = entityDao.get(classOf[ThesisDoc], getLongId("doc"))
    put("doc_url", EmsApp.getBlobRepository(true).url(doc.filePath))
    forward()
  }

  def audit(): View = {
    val archive = entityDao.get(classOf[ThesisArchive], getLongId("archive"))

    val confirmed = getBoolean("archive.confirmed", false)
    if (archive.archived.getOrElse(false) && !confirmed) {
      return redirect("index", "你已经上传过归档材料，无需审核")
    }

    archive.confirmed = getBoolean("archive.confirmed")
    archive.feedback = get("archive.feedback")
    archive.confirmAt = None
    if (archive.confirmed.getOrElse(false)) {
      val teacher = getAdvisor.teacher
      val teacherUser = entityDao.findBy(classOf[User], "school" -> teacher.staff.school, "code" -> teacher.code).headOption
      archive.confirmedBy = teacherUser
      archive.confirmAt = Some(Instant.now)
    }
    entityDao.saveOrUpdate(archive)
    redirect("auditSetting", s"&writer.id=${archive.writer.id}", "审核完成")
  }

  def downloadDoc(): View = {
    val doc = entityDao.get(classOf[ThesisDoc], getLongId("doc"))
    val path = EmsApp.getBlobRepository(true).url(doc.filePath)
    val response = ActionContext.current.response
    response.sendRedirect(path.get.toString)
    null
  }

  def downloadArchive(): View = {
    val archive = entityDao.get(classOf[ThesisArchive], getLongId("archive"))
    val docs = entityDao.findBy(classOf[ThesisDoc], "writer", archive.writer).groupBy(_.writer)
    val writer = archive.writer
    val stdDir = ThesisPdfGenerator.getDepartFolder(writer.season.id, writer.department.id, writer.std.code)
    val zipFiles = PaperDownloadHelper.downloadDocs(new File(stdDir), docs, None)
    Stream(zipFiles.head, MediaTypes.ApplicationZip, s"${writer.code}的论文材料.zip").cleanup { () =>
      zipFiles.head.delete()
      Files.travel(new File(stdDir), f => f.delete())
      new File(stdDir).delete()
    }
  }
}
