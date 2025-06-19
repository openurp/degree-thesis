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

package org.openurp.degree.thesis.web.action.std

import jakarta.servlet.http.Part
import org.beangle.commons.activation.{MediaType, MediaTypes}
import org.beangle.commons.lang.Strings
import org.beangle.data.dao.OqlBuilder
import org.beangle.ems.app.EmsApp
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.view.{Status, Stream, View}
import org.openurp.base.model.AuditStatus
import org.openurp.code.person.model.Language
import org.openurp.code.service.CodeService
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.doc.ThesisDocGenerator

import java.time.Instant

class DocAction extends WriterSupport {

  var codeService: CodeService = _

  def index(): View = {
    val writer = getWriter
    put("writer", writer)

    val commitment = entityDao.findBy(classOf[Commitment], "writer", writer)
    put("commitment_confirmed", commitment.headOption.exists(_.confirmed))

    val proposal = entityDao.findBy(classOf[Proposal], "writer", writer)
    put("proposal_status", proposal.headOption.map(_.status).getOrElse(AuditStatus.Draft))

    val guidance1s = entityDao.findBy(classOf[Guidance], "writer" -> writer, "stage" -> Stage.Guidance1)
    val guidance2s = entityDao.findBy(classOf[Guidance], "writer" -> writer, "stage" -> Stage.Guidance2)
    put("guidance1_count", guidance1s.size)
    put("guidance2_count", guidance2s.size)

    val review = entityDao.findBy(classOf[ThesisReview], "writer", writer).headOption
    put("advisor_reviewed", (review.nonEmpty && review.get.advisorScore.nonEmpty))
    put("cross_reviewed", (review.nonEmpty && review.get.crossReviewScore.nonEmpty))

    val bpr = entityDao.findBy(classOf[BlindPeerReview], "writer", writer).headOption
    put("blind_reviewed", (bpr.nonEmpty && bpr.get.score.nonEmpty))

    val docs = entityDao.findBy(classOf[ThesisDoc], "writer", writer)
    val docsMap = docs.map(x => (x.docType.code, x)).toMap
    put("docsMap", docsMap)
    put("signature", entityDao.findBy(classOf[Signature], "writer", writer).headOption)

    val archive = entityDao.findBy(classOf[ThesisArchive], "writer", writer).headOption
    put("archive", archive)
    forward()
  }

  def cover(): View = {
    val writer = getWriter
    Stream(ThesisDocGenerator.genCover(writer), docx, s"${writer.std.code}论文封面.docx")
  }

  def proposal(): View = {
    val writer = getWriter
    val pdf = getBoolean("pdf", false)
    if (pdf) {
      val docs = entityDao.findBy(classOf[ThesisDoc], "writer" -> writer, "stage" -> Stage.Proposal)
      downloadDoc(docs.headOption)
    } else {
      val signature = entityDao.findBy(classOf[Signature], "writer", writer).headOption
      entityDao.findBy(classOf[Proposal], "writer", writer).headOption match
        case Some(p) => Stream(ThesisDocGenerator.genProposal(p, signature), docx, s"${writer.std.code}开题报告.docx")
        case None => Status.NotFound
    }
  }

  def download(): View = {
    val writer = getWriter
    val doc = entityDao.get(classOf[ThesisDoc], getLongId("doc"))
    if (doc.writer == writer) {
      downloadDoc(Some(doc))
    } else {
      Status.NotFound
    }
  }

  private def downloadDoc(doc: Option[ThesisDoc]): View = {
    doc match {
      case Some(d) =>
        val path = EmsApp.getBlobRepository(true).url(d.filePath)
        val response = ActionContext.current.response
        response.sendRedirect(path.get.toString)
        null
      case None => Status.NotFound
    }
  }

  def commitment(): View = {
    val writer = getWriter
    val commitment = entityDao.findBy(classOf[Commitment], "writer", writer).headOption
    val signature = entityDao.findBy(classOf[Signature], "writer", writer).headOption
    Stream(ThesisDocGenerator.genCommitment(writer, commitment, signature), docx, s"${writer.std.code}承诺书.docx")
  }

  def task(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    val signature = entityDao.findBy(classOf[Signature], "writer", writer).headOption
    Stream(ThesisDocGenerator.genTask(writer, plan, signature), docx, s"${writer.std.code}任务书.docx")
  }

  def guidance1(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    val guidances = entityDao.findBy(classOf[Guidance], "writer" -> writer, "stage" -> Stage.Guidance1)
    if (guidances.size < 2) {
      redirect(to(classOf[GuidanceAction], "index", "stage=" + Stage.Guidance1.id), "尚未填写完成")
    } else {
      val signature = entityDao.findBy(classOf[Signature], "writer", writer).headOption
      Stream(ThesisDocGenerator.genGuidance(writer, plan, guidances, 1, signature), docx, s"${writer.std.code}教师指导记录I.docx")
    }
  }

  def guidance2(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    val guidances = entityDao.findBy(classOf[Guidance], "writer" -> writer, "stage" -> Stage.Guidance2)
    if (guidances.size < 2) {
      redirect(to(classOf[GuidanceAction], "index", "stage=" + Stage.Guidance2.id), "尚未填写完成")
    } else {
      val signature = entityDao.findBy(classOf[Signature], "writer", writer).headOption
      Stream(ThesisDocGenerator.genGuidance(writer, plan, guidances, 2, signature), docx, s"${writer.std.code}教师指导记录II.docx")
    }
  }

  def midtermCheck(): View = {
    val writer = getWriter
    val midtermCheck = entityDao.findBy(classOf[MidtermCheck], "writer", writer).headOption
    Stream(ThesisDocGenerator.genMidtermCheck(writer, midtermCheck), docx, s"${writer.std.code}中期检查表.docx")
  }

  def copyCheck(): View = {
    val writer = getWriter
    val checks = entityDao.findBy(classOf[CopyCheck], "writer", writer)
    val signature = entityDao.findBy(classOf[Signature], "writer", writer).headOption
    Stream(ThesisDocGenerator.genCopyCheck(writer, checks, signature), docx, s"${writer.std.code}反抄袭检测情况记录表.docx")
  }

  def blindReview(): View = {
    val writer = getWriter
    val review = entityDao.findBy(classOf[BlindPeerReview], "writer", writer).headOption
    if (review.isEmpty) {
      redirect("index", "没有校外送审信息")
    } else {
      Stream(ThesisDocGenerator.genBlindReview(writer, review), docx, s"${writer.std.code}校外送审情况记录表.docx")
    }
  }

  def advisorReview(): View = {
    val writer = getWriter
    val review = entityDao.findBy(classOf[ThesisReview], "writer", writer).headOption
    if (review.isEmpty || review.get.advisorScore.isEmpty) {
      redirect("index", "指导教师尚未评分")
    } else {
      val signature = entityDao.findBy(classOf[Signature], "writer", writer).headOption
      Stream(ThesisDocGenerator.genAdvisorReview(writer, review, signature), docx, s"${writer.std.code}指导教师评分表.docx")
    }
  }

  def crossReview(): View = {
    val writer = getWriter
    val review = entityDao.findBy(classOf[ThesisReview], "writer", writer).headOption
    if (review.isEmpty || review.get.crossReviewScore.isEmpty) {
      redirect("index", "交叉评阅尚未评分")
    } else {
      val season = writer.season
      var signature: Option[Signature] = None
      review.get.crossReviewer foreach { reviewer =>
        val q = OqlBuilder.from(classOf[Signature], "s")
        q.where("s.writer.season=:season and s.writer.advisor.teacher=:teacher", season, reviewer)
        q.where("s.advisorUrl is not null")
        signature = entityDao.search(q).headOption
      }
      Stream(ThesisDocGenerator.genCrossReview(writer, review, signature), docx, s"${writer.std.code}评阅表.docx")
    }
  }

  def defense(): View = {
    val writer = getWriter
    val pdf = getBoolean("pdf", false)
    if (pdf) {
      val docs = entityDao.findBy(classOf[ThesisDoc], "writer" -> writer, "stage" -> Stage.OralDefense)
      downloadDoc(docs.headOption)
    } else {
      val query = OqlBuilder.from(classOf[DefenseWriter], "dw")
      query.where("dw.writer = :writer", writer)
      query.where("dw.group.published=true")
      val defenseGroup = entityDao.search(query).map(_.group).headOption
      val review = entityDao.findBy(classOf[ThesisReview], "writer", writer).headOption
      val defenseInfo = entityDao.findBy(classOf[DefenseInfo], "writer", writer).headOption
      Stream(ThesisDocGenerator.genDefense(writer, review, defenseGroup, defenseInfo), docx, s"${writer.std.code}答辩记录及评分表.docx")
    }
  }

  def uploadForm(): View = {
    val writer = getWriter
    val paper = entityDao.findBy(classOf[ThesisPaper], "writer", writer).headOption
    if (paper.isEmpty) {
      return redirect("index", "还没有你的论文，暂停上传")
    }
    val dt = findDocTypes(writer)
    put("docTypes", dt)
    put("writer", writer)
    val docs = entityDao.findBy(classOf[ThesisDoc], "writer", writer).map(x => (x.docType, x)).toMap
    put("docs", docs)
    put("paper", paper)
    put("languages", codeService.get(classOf[Language]))

    val archive = entityDao.findBy(classOf[ThesisArchive], "writer", writer).headOption.getOrElse(new ThesisArchive)
    put("archive", archive)
    forward()
  }

  def upload(): View = {
    val writer = getWriter
    val archive = entityDao.findBy(classOf[ThesisArchive], "writer", writer).headOption.getOrElse(new ThesisArchive)
    if (archive.archived.getOrElse(false)) {
      return redirect("index", "你已经上传过归档材料，无需重复上传")
    }
    if (archive.confirmed.getOrElse(false)) {
      return redirect("index", "导师已经确认，无需重复上传")
    }

    val field = get("paper.researchField", "")
    if (writer.std.major.name == field) {
      return redirect("uploadForm", "论文研究方向与学生专业不能一致")
    }

    entityDao.findBy(classOf[ThesisPaper], "writer", writer).headOption foreach { paper =>
      paper.researchField = get("paper.researchField")
      get("paper.keywords") foreach { k =>
        var v = Strings.replace(k, ",", "；")
        v = Strings.replace(v, "，", "；")
        paper.keywords = Some(v)
      }
      paper.thesisType = get("paper.thesisType")
      paper.language = Some(entityDao.get(classOf[Language], getInt("paper.language.id", 0)))
      entityDao.saveOrUpdate(paper)
      val dt = findDocTypes(writer)
      dt foreach { docType =>
        uploadDoc(writer, docType)
      }
    }

    archive.writer = writer
    archive.uploadAt = Some(Instant.now)
    archive.confirmed = None
    archive.confirmedBy = None
    archive.archived = None
    //archive.feedback = None
    entityDao.saveOrUpdate(archive)
    businessLogger.info(s"上传了归档材料", writer.id, None)
    redirect("index", "info.save.success")
  }

  private def findDocTypes(writer: Writer): List[ThesisDocType] = {
    val blindPeerReviews = entityDao.findBy(classOf[BlindPeerReview], "writer", writer)
    val allTypes = entityDao.getAll(classOf[ThesisDocType]).filter(_.active).toBuffer.sortBy(x => x.stage.id.toString + "." + x.code)
    if (blindPeerReviews.isEmpty) {
      allTypes.subtractAll(allTypes.find(_.code == "blindReview")).toList
    } else {
      allTypes.toList
    }
  }

  private def uploadDoc(writer: Writer, docType: ThesisDocType): Unit = {
    val stage = docType.stage
    val parts = getAll(docType.code, classOf[Part])
    if (parts.nonEmpty && parts.head.getSize > 0) {
      val part = parts.head
      val blob = EmsApp.getBlobRepository(true)
      val storeName = s"${writer.std.code} ${docType.name}." + Strings.substringAfterLast(part.getSubmittedFileName, ".")

      val dd = entityDao.findBy(classOf[ThesisDoc], "writer" -> writer, "docType" -> docType).headOption.getOrElse(new ThesisDoc)
      dd.writer = writer
      dd.stage = stage
      dd.docType = docType
      if (null != dd.filePath && dd.filePath.startsWith("/")) blob.remove(dd.filePath)
      val meta = blob.upload("/" + writer.season.id.toString + s"/archive/${docType.code}/",
        part.getInputStream, storeName, writer.std.code + " " + writer.std.name)
      dd.fileExt = meta.mediaType
      dd.filePath = meta.filePath
      dd.updatedAt = Instant.now
      entityDao.saveOrUpdate(dd)
    }
  }

  private def docx: MediaType = MediaTypes.ApplicationDocx
}
