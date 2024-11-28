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
import org.beangle.commons.bean.orderings.PropertyOrdering
import org.beangle.commons.lang.{ClassLoaders, Strings}
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.{Stream, View}
import org.openurp.base.model.{Department, Project}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.code.job.model.ProfessionalTitle
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.doc.{ThesisDocGenerator, WriterReport}
import org.openurp.degree.thesis.web.helper.AdvisorSubjectStat
import org.openurp.starter.web.support.ProjectSupport

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

/**
 * 报表
 */
class ReportAction extends ActionSupport with ProjectSupport {

  var entityDao: EntityDao = _

  def index(): View = {
    given project: Project = getProject

    val query = OqlBuilder.from(classOf[GraduateSeason], "season")
    query.where("season.project = :project", project)
    query.orderBy("season.code desc")
    val seasons = entityDao.search(query)
    val season = getLong("season.id") match
      case None => seasons.head
      case Some(d) => seasons.find(_.id == d).headOption
    put("season", season)
    put("seasons", seasons)

    val departs = getDeparts
    val depart = getInt("depart.id") match
      case None => departs.head
      case Some(d) => departs.find(_.id == d).headOption

    put("depart", depart)
    put("departs", departs)
    forward()
  }

  /** 论文信息汇总表
   *
   * @return
   */
  def thesis(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("season"))
    val depart = entityDao.get(classOf[Department], getIntId("depart"))
    var thesisReviews = entityDao.findBy(classOf[ThesisReview], "writer.season" -> season, "writer.std.state.department" -> depart)
    thesisReviews = thesisReviews.filter(x => x.writer.advisor.nonEmpty && x.writer.thesisTitle.nonEmpty)
    thesisReviews = thesisReviews.sorted(PropertyOrdering.by("writer.major.name,writer.squad.name,writer.advisor.name"))
    put("thesisReviews", thesisReviews)
    put("season", season)
    put("depart", depart)
    if (getBoolean("download", false)) {
      val hsw = WriterReport.renderThesis(season, thesisReviews)
      val bos = new ByteArrayOutputStream()
      hsw.write(bos)
      Stream(new ByteArrayInputStream(bos.toByteArray), MediaTypes.ApplicationXlsx, depart.name + " 毕业论文信息汇总表.xls")
    } else {
      forward()
    }
  }

  /** 答辩汇总表
   *
   * @return
   */
  def defense(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("season"))
    val depart = entityDao.get(classOf[Department], getIntId("depart"))
    var writers = entityDao.findBy(classOf[DefenseWriter], "writer.season" -> season, "writer.std.state.department" -> depart)
    writers = writers.sorted(PropertyOrdering.by("writer.advisor.name,writer.std.code"))
    put("writers", writers)
    put("season", season)
    put("depart", depart)
    if (getBoolean("download", false)) {
      val hsw = WriterReport.renderDefense(season, writers)
      val bos = new ByteArrayOutputStream()
      hsw.write(bos)
      Stream(new ByteArrayInputStream(bos.toByteArray), MediaTypes.ApplicationXlsx, depart.name + " 答辩安排信息汇总表.xls")
    } else {
      forward()
    }
  }

  /** 材料展示和下载
   *
   * @return
   */
  def material(): View = {
    get("template") match
      case None =>
        val depart = entityDao.get(classOf[Department], getIntId("depart"))
        val season = entityDao.get(classOf[GraduateSeason], getLongId("season"))
        put("depart", depart)
        put("season", season)
        forward()
      case Some(t) =>
        val ext = Strings.substringAfter(t, ".")
        Stream(ClassLoaders.getResourceAsStream(s"org/openurp/degree/thesis/template/${t}").get,
          MediaTypes.get(ext).getOrElse(MediaTypes.ApplicationXlsx), get("name", "template." + ext))
  }

  /** 领导小组名单
   *
   * @return
   */
  def leaderDoc(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("season"))
    val depart = entityDao.get(classOf[Department], getIntId("depart"))
    Stream(ThesisDocGenerator.genReportLeader(season, depart), MediaTypes.ApplicationDocx,
      s"${depart.name} 附件1（领导小组名单）.docx")
  }

  /** 指导老师统计表
   *
   * @return
   */
  def titleDoc(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("season"))
    val depart = entityDao.get(classOf[Department], getIntId("depart"))
    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.season=:season", season)
    query.where("writer.std.state.department=:depart", depart)
    query.where("writer.advisor is not null")

    val writers = entityDao.search(query)
    Stream(ThesisDocGenerator.genReportTitle(season, writers), MediaTypes.ApplicationDocx,
      s"${depart.name} 附件2（指导老师统计表）.docx")
  }

  /** 职称统计表
   *
   * @return
   */
  def title(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("season"))
    val depart = entityDao.get(classOf[Department], getIntId("depart"))
    put("season", season)
    put("depart", depart)

    val query = OqlBuilder.from(classOf[Writer], "writer")
    query.where("writer.season=:season", season)
    query.where("writer.std.state.department=:depart", depart)
    query.where("writer.advisor is not null")

    val writers = entityDao.search(query)
    val titleWriters = writers.groupBy(x => translateTitle(x.advisor.get.teacher.title))
    val titleAdvisors = writers.map(_.advisor.get).toSet.groupBy(x => translateTitle(x.teacher.title))
    put("titleWriters", titleWriters)
    put("titleAdvisors", titleAdvisors)
    put("writers", writers)
    forward()
  }

  private def translateTitle(title: Option[ProfessionalTitle]): String = {
    title match
      case None => "其他"
      case Some(t) =>
        if (t.name.contains("副教授")) "副教授"
        else if (t.name.contains("教授")) "教授"
        else if (t.name.contains("讲师")) "讲师"
        else "其他"
  }

}
