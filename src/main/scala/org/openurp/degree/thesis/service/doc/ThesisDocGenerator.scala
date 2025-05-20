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

package org.openurp.degree.thesis.service.doc

import org.beangle.commons.collection.Collections
import org.beangle.doc.docx.DocTemplate
import org.openurp.base.model.{AuditStatus, Department}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.code.job.model.ProfessionalTitle
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.web.helper.SignatureHelper

import java.io.{ByteArrayInputStream, InputStream}
import java.net.URL
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId}
import scala.math.BigDecimal.RoundingMode

object ThesisDocGenerator extends AbstractFileGenerator {
  def genCover(writer: Writer): InputStream = {
    val bytes = getTemplate("cover.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(writer)
        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  /**
   * 承诺书
   *
   * @param writer
   * @param commitment
   * @return
   */
  def genCommitment(writer: Writer, commitment: Option[Commitment], signature: Option[Signature]): InputStream = {
    val bytes = getTemplate("commitment.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(writer)
        addDate("date", None, data)
        signature foreach { s =>
          s.writerUrl foreach { l => data.put("esign", SignatureHelper.readBase64(l)) }
        }
        if commitment.nonEmpty && commitment.get.confirmed then
          data.put("confirmed", "√")
          data.put("unconfirmed", " ")
          addDate("date", Some(commitment.get.updatedAt), data)
        else
          data.put("unconfirmed", "√")
          data.put("confirmed", " ")

        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  /**
   * 任务书
   *
   * @param writer
   * @param commitment
   * @return
   */
  def genTask(writer: Writer, plan: DepartPlan, signature: Option[Signature]): InputStream = {
    val bytes = getTemplate("task.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(writer)
        data.put("subject_time", stageTime(plan.getStageTime(Stage.Subject)))
        data.put("subject_review_time", stageTime(plan.getStageTime(Stage.SubjectReview)))
        data.put("subject_chosen_round1_time", stageTime(plan.getStageTime(Stage.SubjectChosenRound1)))
        data.put("subject_chosen_round2_time", stageTime(plan.getStageTime(Stage.SubjectChosenRound2)))
        data.put("commitment_time", stageTime(plan.getStageTime(Stage.Commitment)))
        data.put("proposal_time", stageTime(plan.getStageTime(Stage.Proposal)))
        data.put("guidance1_time", stageTime(plan.getStageTime(Stage.Guidance1)))
        data.put("midterm_check_time", stageTime(plan.getStageTime(Stage.MidtermCheck)))
        data.put("guidance2_time", stageTime(plan.getStageTime(Stage.Guidance2)))
        data.put("thesis_submit_time", stageTime2(plan.getStageTime(Stage.ThesisDraftSubmit)))
        data.put("thesis_review_time", stageTime2(plan.getStageTime(Stage.ThesisReview)))
        data.put("oral_defense_time", stageTime2(plan.getStageTime(Stage.OralDefense)))

        val formatter = DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日")
        data.put("begin_on", formatter.format(plan.getStageTime(Stage.Subject).beginOn))
        data.put("end_on", formatter.format(plan.getStageTime(Stage.OralDefense).endOn))
        signature foreach { s =>
          s.writerUrl foreach { l =>
            val s = SignatureHelper.readBase64(l)
            if (null != s) data.put("esign", s)
          }
        }
        DocTemplate.process(url, data)
    new ByteArrayInputStream(bytes)
  }

  def genProposal(proposal: Proposal, signature: Option[Signature]): InputStream = {
    val bytes = getTemplate("proposal.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(proposal.writer)
        data.put("proposal_meanings", proposal.meanings)
        data.put("proposal_conditions", proposal.conditions)
        data.put("proposal_outline", proposal.outline)
        data.put("proposal_references", proposal.references)
        data.put("proposal_methods", proposal.methods)
        signature foreach { s =>
          s.advisorUrl foreach { l => data.put("esign", SignatureHelper.readBase64(l)) }
        }
        if (proposal.status == AuditStatus.Passed) {
          addDate("date", proposal.confirmAt, data)
          proposal.advisorOpinion match {
            case Some(o) => data.put("advisor_opinion", o)
            case None => data.put("advisor_opinion", "审查通过")
          }
        } else {
          addDate("date", None, data)
          data.put("advisor_opinion", "")
        }
        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  def genGuidance(writer: Writer, plan: DepartPlan, guidances: Seq[Guidance], idx: Int, signature: Option[Signature]): InputStream = {
    val bytes = getTemplate(s"guidance${idx}.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(writer)
        val ordered = guidances.sortBy(_.idx)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val stage = plan.getStageTime(if idx == 1 then Stage.Guidance1 else Stage.Guidance2)
        data.put("stage_begin_on", dateFormatter.format(stage.beginOn))
        data.put("stage_end_on", dateFormatter.format(stage.endOn))
        data.put("content1", ordered.head.contents)
        data.put("content2", ordered.last.contents)
        val formatter2 = DateTimeFormatter.ofPattern("yyyy年 MM月 dd日")
        data.put("date1", formatter2.format(ordered.head.updatedAt.atZone(ZoneId.systemDefault()).toLocalDate))
        data.put("date2", formatter2.format(ordered.last.updatedAt.atZone(ZoneId.systemDefault()).toLocalDate))
        signature foreach { s =>
          s.advisorUrl foreach { l => data.put("esign", SignatureHelper.readBase64(l)) }
        }
        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  def genMidtermCheck(writer: Writer, midtermCheck: Option[MidtermCheck]): InputStream = {
    val bytes = getTemplate(s"midtermCheck.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(writer)
        midtermCheck foreach { check =>
          data.put("proceeding", check.proceeding)
          val details = check.details.sortBy(_.item.code)
          details foreach { detail =>
            data.put(s"${detail.item.name}_passed", if detail.passed then "√" else "")
            data.put(s"${detail.item.name}_unpassed", if detail.passed then "" else "√")
            data.put(s"${detail.item.name}_opinion", detail.auditOpinion.getOrElse(""))
          }

          data.put(if (check.status == AuditStatus.Passed || check.status == AuditStatus.PassedByAdvisor) then "passed" else "unpassed", "√")
          data.put("conclusion", check.conclusion.getOrElse(""))
          addDate("date", Some(check.submitAt), data)
        }
        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  def genCopyCheck(writer: Writer, checks: Seq[CopyCheck], signature: Option[Signature]): InputStream = {
    val bytes = getTemplate(s"copyCheck.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(writer)
        var result1 = ""
        var result2 = ""
        var comment = ""

        val first = checks.filter(!_.recheck).headOption
        val second = checks.filter(_.recheck).headOption

        first match
          case None => result1 = "未按时接受检测，视为未通过"
          case Some(c) =>
            result1 = if c.passed then "通过" else "未通过"
            comment = if c.passed then "无需整改" else ""

        second match
          case None =>
            if first.nonEmpty && first.get.passed then result2 = "无需参加复检"
          case Some(c) =>
            result2 = if c.passed then "通过" else "未通过，不予答辩，延期毕业"

        signature foreach { s =>
          s.writerUrl foreach { l => data.put("esign", SignatureHelper.readBase64(l)) }
        }
        data.put("first_check_result", result1)
        data.put("second_check_result", result2)
        data.put("comments_before_2ndcheck", comment)
        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  def genBlindReview(writer: Writer, review: Option[BlindPeerReview]): InputStream = {
    val bytes = getTemplate(s"blindReview.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(writer)
        review match
          case None => data.put("chosen", "否"); data.put("conclusion", "--")
          case Some(r) => data.put("chosen", "是"); data.put("conclusion", r.score.getOrElse("--").toString)
        data.put("comment", "")
        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  def genAdvisorReview(writer: Writer, review: Option[ThesisReview], signature: Option[Signature]): InputStream = {
    val bytes = getTemplate(s"advisorReview.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(writer)
        review foreach { r =>
          addScore("subject_score", r.subjectScore, data)
          addScore("write_score", r.writeScore, data)
          addScore("research_score", r.researchScore, data)
          addScore("innovation_score", r.innovationScore, data)
          addScore("attitude_score", r.attitudeScore, data)
          addScore("advisor_score", r.advisorScore, data)

          signature foreach { s =>
            s.advisorUrl foreach { l => data.put("esign", SignatureHelper.readBase64(l)) }
          }
          //如果有盲审，请加入如下说明
          //"注: 1、该成绩为修正后的成绩；\r\n2、修正公式：指导教师建议成绩( " + advisor_score + " )*40%+校外送审成绩( "+mscj + " )*60%。";
          data.put("review_remark", "")
          addDate("date", r.advisorReviewAt, data)
        }
        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  def genCrossReview(writer: Writer, review: Option[ThesisReview], signature: Option[Signature]): InputStream = {
    val bytes = getTemplate(s"crossReview.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(writer)
        review foreach { r =>
          data.put("cross_review_opinion", r.crossReviewOpinion.getOrElse(""))
          data.put("cross_reviewer_name", r.crossReviewer.map(_.name).getOrElse(""))
          addScore("cross_review_score", r.crossReviewScore, data)
          r.defensePermitted foreach { p =>
            data.put(if p then "defense_permited" else "defense_unpermited", "√")
          }
          addDate("date", r.crossReviewAt, data)
          signature foreach { s =>
            s.advisorUrl foreach { l => data.put("esign", SignatureHelper.readBase64(l)) }
          }
        }
        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  def genDefense(writer: Writer, review: Option[ThesisReview], defenseGroup: Option[DefenseGroup], defenseInfo: Option[DefenseInfo]): InputStream = {
    val bytes = getTemplate(s"defense.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(writer)
        review foreach { r =>
          r.crossReviewer foreach { t =>
            data.put("cross_reviewer_name", t.name)
            addScore("cross_review_score", r.crossReviewScore, data)
            r.crossReviewScore foreach { s =>
              data.put("cross_review_score1", BigDecimal(s * 0.6D).setScale(1, RoundingMode.HALF_UP).toString())
            }
          }
        }

        defenseGroup foreach { g =>
          val memberNames = g.members.filter(!_.leader).map(_.teacher.name).mkString("\n")
          data.put("group_members", memberNames)
          val leader = g.members.filter(_.leader).map(_.teacher.name).mkString("\n")
          data.put("group_leader", leader)
        }

        defenseInfo foreach { i =>
          data.put("thesis_summary", i.thesisSummaryScore.map(_.toString).getOrElse(""))
          data.put("answer_summary", i.answerSummaryScore.map(_.toString).getOrElse(""))
        }
        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  def genReportLeader(season: GraduateSeason, depart: Department): InputStream = {
    val bytes = getTemplate(s"report_leader.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = Collections.newMap[String, String]
        data.put("season_code", season.code)
        data.put("depart_name", depart.name)
        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  def genReportTitle(season: GraduateSeason, writers: Seq[Writer]): InputStream = {

    def translateTitle(title: Option[ProfessionalTitle]): String = {
      title match
        case None => "其他"
        case Some(t) =>
          if (t.name.contains("副教授")) "副教授"
          else if (t.name.contains("教授")) "教授"
          else if (t.name.contains("讲师")) "讲师"
          else "其他"
    }

    val bytes = getTemplate(s"report_title.docx") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = Collections.newMap[String, String]
        data.put("season_code", season.code)
        val titleWriters = writers.groupBy(x => translateTitle(x.advisor.get.teacher.title))
        val titleAdvisors = writers.map(_.advisor.get).toSet.groupBy(x => translateTitle(x.teacher.title))
        data.put("professorCount", titleAdvisors.getOrElse("教授", List.empty).size.toString)
        data.put("associateCount", titleAdvisors.getOrElse("副教授", List.empty).size.toString)
        data.put("lecturerCount", titleAdvisors.getOrElse("讲师", List.empty).size.toString)
        data.put("otherCount", titleAdvisors.getOrElse("其他", List.empty).size.toString)
        val professorStudentCount = titleWriters.getOrElse("教授", List.empty).size
        val associateStudentCount = titleWriters.getOrElse("副教授", List.empty).size
        val lecturerStudentCount = titleWriters.getOrElse("讲师", List.empty).size
        val otherStudentCount = titleWriters.getOrElse("其他", List.empty).size
        data.put("professorStudentCount", professorStudentCount.toString)
        data.put("associateStudentCount", associateStudentCount.toString)
        data.put("lecturerStudentCount", lecturerStudentCount.toString)
        data.put("otherStudentCount", otherStudentCount.toString)

        data.put("proPercent", (Math.round(professorStudentCount * 10000.0 / writers.size).asInstanceOf[Int] / 100.0).toString + "%")
        data.put("assoPercent", (Math.round(associateStudentCount * 10000.0 / writers.size).asInstanceOf[Int] / 100.0).toString + "%")
        data.put("lecPercent", (Math.round(lecturerStudentCount * 10000.0 / writers.size).asInstanceOf[Int] / 100.0).toString + "%")
        data.put("otherPercent", (Math.round(otherStudentCount * 10000.0 / writers.size).asInstanceOf[Int] / 100.0).toString + "%")
        DocTemplate.process(url, data)

    new ByteArrayInputStream(bytes)
  }

  private def stageTime(time: StageTime): String = {
    time.beginOn.toString + "至" + time.endOn.toString
  }

  private def stageTime2(time: StageTime): String = {
    time.endOn.toString + "前"
  }

}
