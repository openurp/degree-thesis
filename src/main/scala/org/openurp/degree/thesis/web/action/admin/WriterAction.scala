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
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.doc.excel.schema.ExcelSchema
import org.beangle.doc.transfer.importer.ImportSetting
import org.beangle.doc.transfer.importer.listener.ForeignerListener
import org.beangle.webmvc.annotation.response
import org.beangle.webmvc.view.{Stream, View}
import org.beangle.webmvc.support.action.{ExportSupport, ImportSupport, RestfulAction}
import org.openurp.base.model.{Project, User}
import org.openurp.base.std.model.{GraduateSeason, Student}
import org.openurp.degree.thesis.model.{Advisor, SubjectApply, ThesisPaper, Writer}
import org.openurp.degree.thesis.service.ThesisPlanService
import org.openurp.degree.thesis.web.helper.WriterImportListener
import org.openurp.starter.web.support.ProjectSupport

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

class WriterAction extends RestfulAction[Writer], ProjectSupport, ExportSupport[Writer], ImportSupport[Writer] {
  var thesisPlanService: ThesisPlanService = _

  override protected def indexSetting(): Unit = {
    given project: Project = getProject

    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateOn desc")
    put("seasons", entityDao.search(gQuery))
    put("departs", getDeparts)
    super.indexSetting()
  }

  override protected def saveAndRedirect(writer: Writer): View = {
    val project: Project = getProject
    getLong("user.id") foreach { userId =>
      val user = entityDao.find(classOf[User], userId).get
      val stds = entityDao.findBy(classOf[Student], "project" -> project, "code" -> user.code)
      stds foreach { std =>
        writer.std = std
      }
    }

    if (!writer.persisted) {
      val wQuery = OqlBuilder.from(classOf[Writer], "w")
      wQuery.where("w.std=:std and w.season=:season", writer.std, writer.season)
      val exists = entityDao.search(wQuery)
      if (exists.isEmpty) {
        updateAdvisor(writer)
        saveOrUpdate(writer)
        redirect("search", "info.save.success")
      } else {
        redirect("search", "该学生已经在名单中")
      }
    } else {
      updateAdvisor(writer)
      saveOrUpdate(writer)
      redirect("search", "info.save.success")
    }
  }

  private def updateAdvisor(writer: Writer): Unit = {
    get("advisor_code") foreach { code =>
      if (Strings.isNotEmpty(code)) {
        val query = OqlBuilder.from(classOf[Advisor], "advisor")
        query.where("advisor.teacher.staff.code=:code", code)
        query.where("advisor.teacher.staff.school=:school", writer.std.project.school)
        entityDao.search(query).headOption foreach { a =>
          writer.advisor = Some(a)
        }
      }
    }
  }

  override protected def getQueryBuilder: OqlBuilder[Writer] = {
    given project: Project = getProject

    val query = super.getQueryBuilder
    query.where("writer.std.state.department in(:departs)", getDeparts)
    get("absence") match
      case Some("paper") => query.where("not exists(from " + classOf[ThesisPaper].getName + " p where p.writer=writer)")
      case _ =>

    query
  }

  override protected def editSetting(entity: Writer): Unit = {
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateOn desc")
    gQuery.limit(1, 1)
    val seasons = entityDao.search(gQuery)
    put("seasons", seasons)
    put("advisors", entityDao.getAll(classOf[Advisor]))
    super.editSetting(entity)
  }

  def absence(): View = {
    put("writers", entityDao.search(getQueryBuilder))
    forward()
  }

  def uploadPaper(): View = {
    val writer = entityDao.get(classOf[Writer], getLongId("writer"))
    redirect(to(classOf[PaperAction], "uploadForm", s"&writer.id=${writer.id}&_params=thesisPaper.writer.season.id=${writer.season.id}"), null)
  }

  @response
  def downloadTemplate(): Any = {
    given project: Project = getProject

    val schema = new ExcelSchema()
    val sheet = schema.createScheet("数据模板")
    sheet.title("学生信息模板")
    sheet.remark("特别说明：\n1、不可改变本表格的行列结构以及批注，否则将会导入失败！\n2、必须按照规格说明的格式填写。\n3、可以多次导入，重复的信息会被新数据更新覆盖。\n4、保存的excel文件名称可以自定。")
    sheet.add("学号", "writer.std.code").length(10).required().remark("≤15位")
    sheet.add("手机", "writer.mobile").length(11)
    sheet.add("电子邮箱", "writer.email").length(50)

    val os = new ByteArrayOutputStream()
    schema.generate(os)
    Stream(new ByteArrayInputStream(os.toByteArray), MediaTypes.ApplicationXlsx, "学生信息.xlsx")
  }

  protected override def configImport(setting: ImportSetting): Unit = {
    val fl = new ForeignerListener(entityDao)
    val seasonId = getLongId("writer.season")
    val season = entityDao.get(classOf[GraduateSeason], seasonId)
    setting.listeners = List(fl, new WriterImportListener(entityDao, season))
  }

  override protected def removeAndRedirect(writers: Seq[Writer]): View = {
    val applies = entityDao.findBy(classOf[SubjectApply], "writer", writers)
    entityDao.remove(applies)
    super.removeAndRedirect(writers)
  }

}
