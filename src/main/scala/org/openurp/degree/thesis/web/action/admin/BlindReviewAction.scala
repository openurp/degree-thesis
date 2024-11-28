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
import org.beangle.commons.file.zip.Zipper
import org.beangle.commons.lang.Numbers
import org.beangle.data.dao.OqlBuilder
import org.beangle.doc.excel.schema.ExcelSchema
import org.beangle.doc.transfer.importer.ImportSetting
import org.beangle.webmvc.annotation.response
import org.beangle.webmvc.view.{Stream, View}
import org.beangle.webmvc.support.action.{ExportSupport, ImportSupport, RestfulAction}
import org.openurp.base.model.{Department, Project}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{BlindPeerReview, ThesisPaper}
import org.openurp.degree.thesis.service.doc.ThesisDocGenerator
import org.openurp.degree.thesis.web.helper.{BlindPeerReviewImportListener, PaperDownloadHelper}
import org.openurp.starter.web.support.ProjectSupport

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, Writer}
import java.time.Instant
import scala.util.Random

class BlindReviewAction extends RestfulAction[BlindPeerReview], ProjectSupport, ExportSupport[BlindPeerReview], ImportSupport[BlindPeerReview] {

  override def index(): View = {
    given project: Project = getProject

    put("departs", getDeparts)
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateOn desc")
    put("seasons", entityDao.search(gQuery))
    forward()
  }

  def drawlot(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("review.writer.season"))
    val exists = entityDao.findBy(classOf[BlindPeerReview], "writer.season", season).groupBy(_.writer.department.id)

    val ratio = getFloat("ratio").getOrElse(0.1f)
    val query = OqlBuilder.from[Array[Any]](classOf[ThesisPaper].getName, "paper")
    query.where("paper.writer.season=:season", season)
    query.select("paper.writer.std.state.department.id,count(*)")
    query.groupBy("paper.writer.std.state.department.id")
    val totals = entityDao.search(query).map(x => x(0).asInstanceOf[Number].intValue -> x(1).asInstanceOf[Number].intValue).toMap

    totals foreach { case (departId, cnt) =>
      val required = Numbers.round(Numbers.multiply(ratio, cnt), 0).toInt
      val existed = exists.getOrElse(departId, Seq.empty).size
      if (required > existed) {
        val q = OqlBuilder.from(classOf[ThesisPaper], "paper")
        q.where("paper.writer.season=:season", season)
        q.where("paper.writer.std.state.department.id=:departId", departId)
        q.where("not exists(from " + classOf[BlindPeerReview].getName + " bpr where bpr.writer=paper.writer)")
        val papers = entityDao.search(q)
        val news = Random.shuffle(papers).take(required - existed).map { paper =>
          val r = new BlindPeerReview
          r.writer = paper.writer
          r.remark = Some("系统随机抽取")
          r.updatedAt = Instant.now
          r
        }
        entityDao.saveOrUpdate(news)
      }
    }
    redirect("search", "随机抽检成功！")
  }

  @response
  def downloadTemplate(): Any = {
    val schema = new ExcelSchema()
    val sheet = schema.createScheet("数据模板")
    sheet.title("论文结果检测模板")
    sheet.remark("特别说明：\n1、不可改变本表格的行列结构以及批注，否则将会导入失败！\n2、必须按照规格说明的格式填写。\n3、可以多次导入，重复的信息会被新数据更新覆盖。\n4、保存的excel文件名称可以自定。")
    sheet.add("学号", "writer.std.code").length(20).remark("≤20位").required()
    sheet.add("分数", "review.score").integer()
    val os = new ByteArrayOutputStream()
    schema.generate(os)
    Stream(new ByteArrayInputStream(os.toByteArray), MediaTypes.ApplicationXlsx, "盲审成绩导入模板.xlsx")
  }

  /** 统计各个学院的论文数量
   *
   * @return
   */
  def stat(): View = {
    given project: Project = getProject

    val seasonId = getLong("review.writer.season.id").get

    val query2 = OqlBuilder.from[Array[Any]](classOf[BlindPeerReview].getName, "review")
    query2.where("review.writer.season.id=:seasonId", seasonId)
    query2.where("review.writer.std.state.department in(:departs)", getDeparts)
    query2.select("review.writer.std.state.department.id,review.writer.std.state.department.name,count(*)")
    query2.groupBy("review.writer.std.state.department.id,review.writer.std.state.department.name")
    query2.orderBy("review.writer.std.state.department.id")
    val papers = entityDao.search(query2)

    val zipTimes = Collections.newMap[Int, java.util.Date]
    papers foreach { p =>
      val departId = p(0).asInstanceOf[Number].intValue()
      val f = ThesisDocGenerator.getDepartZipFile(seasonId, departId, "blind-review")
      if (f.exists()) {
        zipTimes.put(departId, new java.util.Date(f.lastModified()))
      }
    }
    put("papers", papers)
    put("zipTimes", zipTimes)
    put("seasonId", seasonId)
    forward()
  }

  def genZip(): View = {
    val seasonId = getLongId("season")
    val departmentId = getIntId("department")
    val query2 = OqlBuilder.from[ThesisPaper](classOf[ThesisPaper].getName + s" paper,${classOf[BlindPeerReview].getName}  r")
    query2.where("paper.writer.season.id=:seasonId", seasonId)
    query2.where("r.writer.season.id=:seasonId", seasonId)
    query2.where("r.writer=paper.writer")
    query2.where("paper.writer.std.state.department.id=:departId", departmentId)
    query2.select("paper")
    val papers = entityDao.search(query2)
    val dir = ThesisDocGenerator.getDepartFolder(seasonId, departmentId, "blind-review")
    PaperDownloadHelper.download(new File(dir), papers)
    val targetZip = ThesisDocGenerator.getDepartZipFile(seasonId, departmentId, "blind-review")
    Zipper.zip(new File(dir), targetZip)
    redirect("stat", "review.writer.season.id=" + seasonId, "生成成功")
  }

  def downloadZip(): View = {
    val seasonId = getLongId("season")
    val departmentId = getIntId("department")
    val file = ThesisDocGenerator.getDepartZipFile(seasonId, departmentId,  "blind-review")
    val depart = entityDao.get(classOf[Department], departmentId)
    val season = entityDao.get(classOf[GraduateSeason], seasonId)
    val fileName = season.name + "_" + depart.name + " 校外送审论文.zip"
    Stream(file, MediaTypes.ApplicationZip, fileName)
  }

  protected override def configImport(setting: ImportSetting): Unit = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("review.writer.season"))
    setting.listeners = List(new BlindPeerReviewImportListener(season, entityDao))
  }

  override protected def simpleEntityName: String = "review"
}
