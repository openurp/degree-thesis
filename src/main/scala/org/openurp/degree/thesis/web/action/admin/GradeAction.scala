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
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.webmvc.annotation.response
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.action.{ExportSupport, ImportSupport, RestfulAction}
import org.beangle.webmvc.view.{Stream, View}
import org.openurp.base.model.{AuditStatus, Project}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.{ThesisGradeSyncService, ThesisPlanService}
import org.openurp.degree.thesis.web.helper.{ScoreTextHelper, ThesisReviewImportListener}
import org.openurp.starter.web.support.ProjectSupport

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import scala.collection.mutable

/** 成绩管理
 */
class GradeAction extends RestfulAction[ThesisReview], ExportSupport[ThesisReview], ImportSupport[ThesisReview], ProjectSupport {

  var thesisPlanService: ThesisPlanService = _
  var businessLogger: WebBusinessLogger = _
  var thesisGradeSyncService: ThesisGradeSyncService = _

  override def index(): View = {
    given project: Project = getProject

    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateOn desc")
    put("seasons", entityDao.search(gQuery))
    put("departs", getDeparts)
    forward()
  }

  override protected def getQueryBuilder: OqlBuilder[ThesisReview] = {
    given project: Project = getProject

    put("departs", getDeparts.filter(_.teaching))
    val query = super.getQueryBuilder
    query.where("thesisReview.writer.std.state.department in(:departs)", getDeparts)
    query
  }

  override protected def saveAndRedirect(review: ThesisReview): View = {
    val writer = entityDao.get(classOf[Writer], review.writer.id)
    val msg = s"修改${writer.code}的论文成绩"
    review.defenseScore match {
      case None =>
        review.finalScore = None
        review.finalScoreText = None
      case Some(s) =>
        review.crossReviewScore foreach { crs =>
          val score = Math.round(crs * 0.6d + s * 0.4d).intValue()
          review.finalScore = Some(score)
          review.finalScoreText = Some(ScoreTextHelper.convert(score))
          review.remark = None
        }
    }

    if (review.subjectScore.nonEmpty) {
      val finalScore = review.subjectScore.getOrElse(0) + review.writeScore.getOrElse(0) +
        review.researchScore.getOrElse(0) + review.innovationScore.getOrElse(0) + review.attitudeScore.getOrElse(0)
      review.advisorScore = Some(finalScore)
    }
    businessLogger.info(msg, review.id, ActionContext.current.params)
    super.saveAndRedirect(review)
  }

  private def calcFinalScore(r: ThesisReview): (Option[Float], Option[String]) = {
    r.defenseScore match {
      case None => (None, None)
      case Some(s) =>
        r.crossReviewScore match {
          case None => (None, None)
          case Some(crs) =>
            val score = Math.round(crs * 0.6d + s * 0.4d).floatValue()
            (Some(score), Some(ScoreTextHelper.convert(score)))
        }
    }
  }

  private def calcAdvisorScore(r: ThesisReview): Option[Int] = {
    if (r.subjectScore.nonEmpty) {
      Some(r.subjectScore.getOrElse(0) + r.writeScore.getOrElse(0) +
        r.researchScore.getOrElse(0) + r.innovationScore.getOrElse(0) + r.attitudeScore.getOrElse(0))
    } else {
      r.advisorScore
    }
  }

  def syncGrades(): View = {
    val reviews = entityDao.find(classOf[ThesisReview], getLongIds("thesisReview"))
    val finalReviews = reviews.filter(_.finalScore.nonEmpty)
    finalReviews foreach { review =>
      thesisGradeSyncService.sync(review)
    }
    redirect("search", "论文成绩同步成功")
  }

  @response
  def downloadTemplate(): Any = {
    val schema = new ExcelSchema()
    val sheet = schema.createScheet("数据模板")
    sheet.title("论文成绩导入模板")
    sheet.remark("特别说明：\n1、不可改变本表格的行列结构以及批注，否则将会导入失败！\n2、必须按照规格说明的格式填写。\n3、可以多次导入，重复的信息会被新数据更新覆盖。\n4、保存的excel文件名称可以自定。")
    sheet.add("学号", "writer.std.code").length(20).remark("≤20位").required()
    sheet.add("指导老师工号", "advisor.teacher.staff.code").length(20).remark("≤20位")
    sheet.add("指导老师建议分数", "thesisReview.advisorScore").decimal()
    sheet.add("交叉评阅分数", "thesisReview.crossReviewScore").decimal()
    sheet.add("答辩分数", "thesisReview.defenseScore").decimal()
    sheet.add("最终总分", "thesisReview.finalScore").decimal()
    val os = new ByteArrayOutputStream()
    schema.generate(os)
    Stream(new ByteArrayInputStream(os.toByteArray), MediaTypes.ApplicationXlsx, "答辩成绩导入模板.xlsx")
  }

  protected override def configImport(setting: ImportSetting): Unit = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("thesisReview.writer.season"))
    setting.listeners = List(new ThesisReviewImportListener(season, entityDao))
  }

  def analysis(): View = {
    given project: Project = getProject

    val seasonId = getLongId("thesisReview.writer.season")
    val query = OqlBuilder.from(classOf[ThesisReview], "r")
    query.where("r.writer.std.state.department in(:departs)", getDeparts)
    query.where("r.writer.season.id=:seasonId", seasonId)
    val reviews = entityDao.search(query)
    val results = Collections.newMap[ThesisReview, mutable.Buffer[String]]
    val remarks = Collections.newSet[ThesisReview]
    reviews.foreach { review =>
      val newAdvisorScore = calcAdvisorScore(review)
      if (newAdvisorScore != review.advisorScore) {
        val msgs = results.getOrElseUpdate(review, new mutable.ArrayBuffer[String])
        msgs += s"建议分数从${review.advisorScore.map(_.toString).getOrElse("")}改为${newAdvisorScore.map(_.toString).getOrElse("")}"
        review.advisorScore = newAdvisorScore
      }
      val fs = calcFinalScore(review)
      if (fs._1 != review.finalScore) {
        val msgs = results.getOrElseUpdate(review, new mutable.ArrayBuffer[String])
        msgs += s"最终分数从${review.finalScore.map(_.toString).getOrElse("")}改为${fs._1.map(_.toString).getOrElse("")}"
        review.finalScore = fs._1
        review.finalScoreText = fs._2
      }
      if (review.defenseScore.isEmpty) {
        val midtermChecks = entityDao.findBy(classOf[MidtermCheck], "writer", review.writer)
        if (midtermChecks.isEmpty || midtermChecks.head.status != AuditStatus.Passed) {
          review.remark = Some("中期检查未通过")
          remarks.addOne(review)
        }
        val copyChecks = entityDao.findBy(classOf[CopyCheck], "writer", review.writer)
        if (copyChecks.nonEmpty && !copyChecks.exists(_.passed)) {
          review.remark match {
            case None => review.remark = Some("反抄袭检测未通过")
            case Some(k) => review.remark = Some(k + " 反抄袭检测未通过")
          }
          remarks.addOne(review)
        }
      }
    }
    entityDao.saveOrUpdate(reviews)
    put("results", results)
    put("remarks", remarks)
    forward()
  }

}
