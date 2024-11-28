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
import org.beangle.commons.collection.Collections
import org.beangle.data.dao.OqlBuilder
import org.beangle.doc.excel.schema.ExcelSchema
import org.beangle.doc.transfer.importer.ImportSetting
import org.beangle.doc.transfer.importer.listener.ForeignerListener
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.webmvc.annotation.response
import org.beangle.webmvc.view.{Stream, View}
import org.beangle.webmvc.support.action.{ExportSupport, ImportSupport, RestfulAction}
import org.openurp.base.edu.model.{Major, TeachingOffice}
import org.openurp.base.model.{AuditStatus, Department, Project}
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.{AdvisorService, SubjectService, ThesisPlanService}
import org.openurp.degree.thesis.web.helper.SubjectImportListener
import org.openurp.starter.web.support.ProjectSupport

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.time.LocalDate
import scala.collection.mutable

/** 论文题目管理
 *
 */
class SubjectAction extends RestfulAction[Subject], DepartSupport, ProjectSupport, ImportSupport[Subject], ExportSupport[Subject] {

  var thesisPlanService: ThesisPlanService = _
  var subjectService: SubjectService = _
  var advisorService: AdvisorService = _
  var businessLogger: WebBusinessLogger = _

  override protected def indexSetting(): Unit = {
    given project: Project = getProject

    val plan = thesisPlanService.getPlan().get
    val departs = getDeparts
    put("departs", departs)
    put("offices", entityDao.findBy(classOf[TeachingOffice], "department", departs))
    put("majors", subjectService.getMajors(plan.season, departs))

    super.indexSetting()
  }

  override def getQueryBuilder: OqlBuilder[Subject] = {
    given project: Project = getProject

    val query = super.getQueryBuilder
    val season = thesisPlanService.getPlan().get.season
    query.where("subject.season = :season", season)
    query.where("subject.depart in(:departs)", getDeparts)

    getLong("major.id") foreach { majorId =>
      query.where(":major in elements(subject.majors)", entityDao.get(classOf[Major], majorId))
    }
    getBoolean("departMatched") foreach { matched =>
      query.where("subject.depart " + (if matched then " " else " not ") + " in elements(subject.advisor.departs)")
    }
    query
  }

  override def editSetting(subject: Subject): Unit = {
    given project: Project = getProject

    var departs: collection.Seq[Department] = getDeparts
    getLong("subject.advisor.id") foreach { advisorId =>
      subject.advisor = entityDao.get(classOf[Advisor], advisorId)
      departs = subject.advisor.departs
    }
    if (subject.persisted) {
      departs = subject.advisor.departs
    }
    put("departs", departs)
    if (subject.advisor == null || !subject.advisor.persisted) {
      put("advisors", advisorService.getAdvisors(departs))
    }

    val plan = thesisPlanService.getPlan().get
    put("majors", subjectService.getMajors(plan.season, departs))
  }

  override protected def saveAndRedirect(subject: Subject): View = {
    val advisor = entityDao.get(classOf[Advisor], subject.advisor.id)
    if (null == subject.season || !subject.season.persisted) {
      subject.season = thesisPlanService.getPlan().get.season
    }
    var errorMsg: String = null
    if (subjectService.duplicate(subject.season, subject)) {
      errorMsg = "题目重复"
    }
    if (!subject.persisted && subjectService.getFreeQuota(subject.season, advisor) <= 0) {
      errorMsg = "此老师添加的参考题目数已经达到限定，不能添加此课题!"
    }
    if (null != errorMsg) {
      editSetting(subject)
      put("subject", subject)
      addError(errorMsg)
      return forward("form")
    }

    val newer = !subject.persisted
    val majors = entityDao.find(classOf[Major], getAll("majorId", classOf[Long]))
    subject.majors.clear()
    subject.majors ++= majors
    subject.status = AuditStatus.Submited
    saveOrUpdate(subject)
    val message = (if newer then "添加" else "修改") + s"论文选题：${subject.name}"
    businessLogger.info(message, subject.id, Map.empty)
    redirect("search", "info.save.success")
  }

  def teachers(): View = {
    given project: Project = getProject

    val departs = getInt("subject.advisor.teacher.department.id") match {
      case Some(id) => List(entityDao.get(classOf[Department], id))
      case None => getDeparts
    }
    val season = thesisPlanService.getPlan().get.season
    val emptyOnly = getBoolean("emptyOnly", false)
    var subjectStats = Map.empty[Long, Int]
    var advisors: Seq[Advisor] = null
    if emptyOnly then
      val aQuery = OqlBuilder.from(classOf[Advisor], "advisor")
      aQuery.where("exists(from advisor.departs as d where d in(:departs))", departs)
      aQuery.where("advisor.endOn is null or advisor.endOn>=:now", LocalDate.now)
        .orderBy("advisor.teacher.department,advisor.teacher.name")
      aQuery.where("not exists(from " + classOf[Subject].getName + " s where s.advisor=advisor and s.season=:season and s.depart in(:departs))", season, departs)
      advisors = entityDao.search(aQuery)
    else
      val query = OqlBuilder.from[Array[Any]](classOf[Subject].getName, "s")
      query.where("s.depart in(:depart) and s.season=:season", departs, season)
      query.groupBy("s.advisor.id").select("s.advisor.id,count(*)")
      subjectStats = entityDao.search(query).map { x => (x(0).asInstanceOf[Number].longValue, x(1).asInstanceOf[Number].intValue) }.toMap
      advisors = advisorService.getAdvisors(departs)
    end if

    val historyStats = new mutable.HashMap[String, Any]
    val historyQuery = OqlBuilder.from[Array[Any]](classOf[Subject].getName, "h")
    historyQuery.where("h.advisor in(:js)", advisors)
    historyQuery.where("h.season.graduateOn<=:graduateOn", season.graduateOn)
    historyQuery.select("h.advisor.id,count(distinct h.name)")
    historyQuery.groupBy("h.advisor.id")
    val historyData = entityDao.search(historyQuery)
    historyData foreach { data =>
      historyStats.put(data(0).toString, data(1))
    }
    put("advisors", advisors)
    put("emptyOnly", emptyOnly)
    put("subjectStats", subjectStats)
    put("historyStats", historyStats)
    forward()
  }

  def history(): View = {
    val plan = thesisPlanService.getPlan().get
    val advisor = entityDao.get(classOf[Advisor], getLongId("subject.advisor"))
    val query = OqlBuilder.from(classOf[Subject], "subject").where("subject.advisor=:advisor", advisor)
    query.where("subject.season.graduateOn<:graduateOn", plan.season.graduateOn)
    query.where("not exists(from " + classOf[Subject].getName +
      " s2 where s2.name=subject.name and s2.advisor=subject.advisor and s2.season.graduateOn > subject.season.graduateOn)")
    query.orderBy("subject.season.graduateOn desc")
    val lstmList = entityDao.search(query)

    put("lstmList", lstmList)
    put("majors", subjectService.getMajors(plan.season, advisor.departs))
    put("advisor", advisor)
    forward()
  }

  def addFromHistory(): View = {
    val plan = thesisPlanService.getPlan().get
    val advisor = entityDao.get(classOf[Advisor], getLongId("subject.advisor"))
    val majors = entityDao.find(classOf[Major], getAll("majorId", classOf[Long]))
    val season = plan.season
    val errors = Collections.newBuffer[String]
    getAll("lstmId") foreach { id =>
      val lstm = entityDao.get(classOf[Subject], id.toString.toLong)
      val ksList = entityDao.findBy(classOf[Subject], "season" -> season, "name" -> lstm.name)
      if (ksList.isEmpty) {
        val kt = new Subject
        kt.advisor = advisor
        kt.depart = lstm.depart
        kt.name = lstm.name
        kt.researchField = lstm.researchField
        kt.contents = lstm.contents
        kt.season = season
        kt.status = AuditStatus.Submited
        kt.majors ++= majors
        if (null == kt.season || !kt.season.persisted) {
          kt.season = season
        }
        val errorSize = errors.size
        if (subjectService.duplicate(season, kt)) {
          errors.addOne("题目重复")
        } else {
          if (subjectService.getFreeQuota(season, advisor) <= 0) {
            errors.addOne("此老师添加的参考题目数已经达到限定，不能添加此课题!")
          }
        }
        if (errors.size == errorSize) {
          entityDao.saveOrUpdate(kt)
          val message = s"从历史选题中增加：${kt.name}"
          businessLogger.info(message, kt.id, Map.empty)
        }
      }
    }
    redirect("search", if errors.isEmpty then "info.save.success" else errors.mkString(","))
  }

  def batchUpdateSetting(): View = {
    given project: Project = getProject

    val plan = thesisPlanService.getPlan().get
    val majors = subjectService.getMajors(plan.season, getDeparts)
    put("majors", majors)
    put("subjects", entityDao.find(classOf[Subject], getLongIds("subject")))
    forward()
  }

  def batchUpdate(): View = {
    val subjects = entityDao.find(classOf[Subject], getLongIds("subject"))
    val majors = entityDao.find(classOf[Major], getLongIds("major"))
    subjects foreach { subject =>
      subject.majors.clear()
      subject.majors.addAll(majors)
    }
    entityDao.saveOrUpdate(subjects)
    redirect("search", "info.save.success")
  }

  @response
  def downloadTemplate(): Any = {
    val departs = entityDao.search(OqlBuilder.from(classOf[Department], "bt").orderBy("bt.name")).map(x => x.code + " " + x.name)
    val majors = entityDao.getAll(classOf[Major]).filter(_.endOn.isEmpty).sortBy(_.code).map(x => x.code + " " + x.name)
    val schema = new ExcelSchema()
    val sheet = schema.createScheet("数据模板")
    sheet.title("题目信息模板")
    sheet.remark("特别说明：\n1、不可改变本表格的行列结构以及批注，否则将会导入失败！\n2、必须按照规格说明的格式填写。\n3、可以多次导入，重复的信息会被新数据更新覆盖。\n4、保存的excel文件名称可以自定。")
    sheet.add("指导教师工号或者姓名", "advisorCodeOrName").length(10).required().remark("≤10位")
    sheet.add("题目", "subject.name").length(100).required()
    sheet.add("面向专业", "major.code").ref(majors).required()
    sheet.add("研究方向", "subject.researchField").required()
    sheet.add("指导院系", "subject.depart.code").ref(departs).required()

    val os = new ByteArrayOutputStream()
    schema.generate(os)
    Stream(new ByteArrayInputStream(os.toByteArray), MediaTypes.ApplicationXlsx, "题目模板.xlsx")
  }

  protected override def configImport(setting: ImportSetting): Unit = {
    val fl = new ForeignerListener(entityDao)
    fl.addForeigerKey("name")
    val season = thesisPlanService.getPlan().get.season
    setting.listeners = List(fl, new SubjectImportListener(season, entityDao))
  }
}
