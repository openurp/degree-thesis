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

import jakarta.servlet.http.Part
import org.apache.commons.compress.archivers.zip.ZipFile
import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.collection.Collections
import org.beangle.commons.file.zip.Zipper
import org.beangle.commons.io.{Files, IOs}
import org.beangle.commons.lang.*
import org.beangle.commons.lang.text.TemporalFormatter
import org.beangle.data.dao.OqlBuilder
import org.beangle.doc.excel.schema.ExcelSchema
import org.beangle.doc.transfer.exporter.ExportContext
import org.beangle.doc.transfer.importer.ImportSetting
import org.beangle.doc.transfer.importer.listener.ForeignerListener
import org.beangle.ems.app.EmsApp
import org.beangle.ems.app.web.WebBusinessLogger
import org.beangle.webmvc.annotation.response
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.action.{ExportSupport, ImportSupport, RestfulAction}
import org.beangle.webmvc.view.{Status, Stream, View}
import org.openurp.base.model.{Department, Project}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.code.person.model.Language
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.service.doc.ThesisPdfGenerator
import org.openurp.degree.thesis.service.{DeferService, ThesisCheckService}
import org.openurp.degree.thesis.web.helper.*
import org.openurp.degree.thesis.web.helper.ThesisCheckFileNaming.getShortSchoolYear
import org.openurp.starter.web.support.ProjectSupport

import java.io.*
import java.time.{Instant, LocalDate}
import scala.jdk.javaapi.CollectionConverters.asScala

/** 论文抽检，教育部论文抽检对接
 */
class ThesisCheckAction extends RestfulAction[ThesisCheck], ProjectSupport, ExportSupport[ThesisCheck], ImportSupport[ThesisCheck] {
  var deferService: DeferService = _
  var businessLogger: WebBusinessLogger = _
  var thesisCheckService: ThesisCheckService = _

  override def index(): View = {
    given project: Project = getProject

    put("departs", getDeparts)
    val gQuery = OqlBuilder.from(classOf[GraduateSeason], "gg")
    gQuery.orderBy("gg.graduateOn desc")
    put("seasons", entityDao.search(gQuery))
    forward()
  }

  override protected def getQueryBuilder: OqlBuilder[ThesisCheck] = {
    given project: Project = getProject

    put("project", project)
    val query = super.getQueryBuilder
    getBoolean("sameTitle") foreach { sameTitle =>
      if sameTitle then query.where("thesisCheck.title = thesisCheck.writer.thesisTitle")
      else query.where("thesisCheck.title != thesisCheck.writer.thesisTitle")
    }
    getBoolean("docOk") foreach { docOk =>
      if docOk then query.where("thesisCheck.paperDoc is not null and thesisCheck.proposalDoc is not null and thesisCheck.defenseDoc is not null")
      else query.where("thesisCheck.paperDoc is null or thesisCheck.proposalDoc is null or thesisCheck.defenseDoc is null")
    }

    val seasonId = getLongId("thesisCheck.season")
    val season = entityDao.get(classOf[GraduateSeason], seasonId)
    put("schoolYear", getShortSchoolYear(season))

    query.where("thesisCheck.writer.std.state.department in(:departs)", getDeparts)
    query
  }

  /** 下载选定的论文 */
  def paper(): View = {
    val checks = entityDao.find(classOf[ThesisCheck], getLongIds("thesisCheck"))
    val papers = checks.flatMap(_.paperDoc)
    val season = checks.head.season
    val dir = new File(System.getProperty("java.io.tmpdir") + s"season${season.id}" + Files./ + "batch")
    if (dir.exists()) {
      Files.travel(dir, f => f.delete())
    }
    dir.mkdirs()

    PaperDownloadHelper.download(dir, checks, ThesisCheckFileNaming.paperFileName2)
    val targetZip = new File(System.getProperty("java.io.tmpdir") + s"season${season.id}" + Files./ + "batch.zip")
    Zipper.zip(dir, targetZip)
    val fileName =
      if (papers.size == 1) {
        season.name + "_" + papers.head.writer.code + s"的论文.zip"
      } else {
        season.name + "_" + papers.head.writer.code + s"等${papers.size}篇论文.zip"
      }
    Stream(targetZip, MediaTypes.ApplicationZip, fileName).cleanup(() => targetZip.delete())
  }

  /** 下载单个学生的开题报告 */
  def proposalDoc(): View = {
    val check = entityDao.get(classOf[ThesisCheck], getLongId("thesisCheck"))
    check.proposalDoc match {
      case Some(doc) =>
        val path = EmsApp.getBlobRepository(true).url(doc.filePath)
        val response = ActionContext.current.response
        response.sendRedirect(path.get.toString)
        null
      case None => Status.NotFound
    }
  }

  /** 下载单个学生的答辩评分表报告 */
  def defenseDoc(): View = {
    val check = entityDao.get(classOf[ThesisCheck], getLongId("thesisCheck"))
    check.defenseDoc match {
      case Some(doc) =>
        val path = EmsApp.getBlobRepository(true).url(doc.filePath)
        val response = ActionContext.current.response
        response.sendRedirect(path.get.toString)
        null
      case None => Status.NotFound
    }
  }

  def stat(): View = {
    given project: Project = getProject

    val seasonId = getLong("thesisCheck.season.id").get
    val query1 = OqlBuilder.from[Array[Any]](classOf[ThesisCheck].getName, "check")
    query1.where("check.season.id=:seasonId", seasonId)
    query1.where("check.writer.std.state.department in(:departs)", getDeparts)
    query1.select("check.writer.std.state.department.id,check.writer.std.state.department.code,check.writer.std.state.department.name,count(*)")
    query1.groupBy("check.writer.std.state.department.id,check.writer.std.state.department.code,check.writer.std.state.department.name")
    query1.orderBy("check.writer.std.state.department.code")
    val papers = entityDao.search(query1)

    val query2 = OqlBuilder.from[Array[Any]](classOf[ThesisDoc].getName, "doc")
    query2.where("exists(from " + classOf[ThesisCheck].getName + " c where c.writer=doc.writer and c.season.id=:seasonId)", seasonId)
    query2.where("doc.writer.std.state.department in(:departs)", getDeparts)
    query2.where("doc.stage=:stage", Stage.Proposal)
    query2.select("doc.writer.std.state.department.id,count(*)")
    query2.groupBy("doc.writer.std.state.department.id")
    val proposals = entityDao.search(query2).map(x => (x(0), x(1))).toMap

    val query3 = OqlBuilder.from[Array[Any]](classOf[ThesisDoc].getName, "doc")
    query3.where("exists(from " + classOf[ThesisCheck].getName + " c where c.writer=doc.writer and c.season.id=:seasonId)", seasonId)
    query3.where("doc.writer.std.state.department in(:departs)", getDeparts)
    query3.where("doc.stage=:stage", Stage.OralDefense)
    query3.select("doc.writer.std.state.department.id,count(*)")
    query3.groupBy("doc.writer.std.state.department.id")
    val defenses = entityDao.search(query3).map(x => (x(0), x(1))).toMap

    val zipTimes = Collections.newMap[Int, java.util.Date]
    val zipSizes = Collections.newMap[Int, Long]
    papers foreach { p =>
      val departId = p(0).asInstanceOf[Number].intValue()
      val f = ThesisPdfGenerator.getDepartZipFile(seasonId, departId, "thesis_check")
      if (f.exists()) {
        zipTimes.put(departId, new java.util.Date(f.lastModified()))
        zipSizes.put(departId, f.length())
      }
    }
    put("papers", papers)
    put("proposals", proposals)
    put("defenses", defenses)
    put("zipTimes", zipTimes)
    put("zipSizes", zipSizes)
    put("seasonId", seasonId)
    forward()
  }

  def genZip(): View = {
    val seasonId = getLongId("season")
    val departmentId = getIntId("department")
    //下载论文
    val query1 = OqlBuilder.from(classOf[ThesisCheck], "check")
    query1.where("check.season.id=:seasonIds", seasonId)
    query1.where("check.writer.std.state.department.id=:departId", departmentId)
    val checks = entityDao.search(query1)
    val writer2Check = checks.map(x => (x.writer, x)).toMap
    val checkDir = ThesisPdfGenerator.getDepartFolder(seasonId, departmentId, "thesis_check")
    //下载论文
    PaperDownloadHelper.download(new File(checkDir), checks, ThesisCheckFileNaming.paperFileName)

    //下载材料
    val docs = Collections.newBuffer[ThesisDoc]
    checks foreach { c =>
      docs.addAll(c.proposalDoc)
      docs.addAll(c.defenseDoc)
    }
    val zipFiles = PaperDownloadHelper.downloadDocs(new File(checkDir), docs.groupBy(x => x.writer), Some(f => ThesisCheckFileNaming.attachFileName(writer2Check(f))))
    val targetZip = ThesisPdfGenerator.getDepartZipFile(seasonId, departmentId, "thesis_check")
    Zipper.zip(new File(checkDir), targetZip)

    redirect("stat", "thesisCheck.season.id=" + seasonId, "生成成功")
  }

  def downloadFiles(): View = {
    val checks = entityDao.find(classOf[ThesisCheck], getLongIds("thesisCheck"))
    val writer2Check = checks.map(x => (x.writer, x)).toMap
    val seasonId = checks.head.season.id
    val departmentId = checks.head.writer.department.id
    val checkDir = ThesisPdfGenerator.getDepartFolder(seasonId, departmentId, "thesis_check_tmp")
    //下载论文
    PaperDownloadHelper.download(new File(checkDir), checks, ThesisCheckFileNaming.paperFileName)
    //下载材料
    val docs = Collections.newBuffer[ThesisDoc]
    checks foreach { c =>
      docs.addAll(c.proposalDoc)
      docs.addAll(c.defenseDoc)
    }
    val zipFiles = PaperDownloadHelper.downloadDocs(new File(checkDir), docs.groupBy(x => x.writer), Some(f => ThesisCheckFileNaming.attachFileName(writer2Check(f))))
    val targetZip = ThesisPdfGenerator.getDepartZipFile(seasonId, departmentId, "thesis_check_tmp")
    Zipper.zip(new File(checkDir), targetZip)
    Stream(targetZip, MediaTypes.ApplicationZip, s"${checks.head.writer.code}等${checks.size}人的论文材料.zip").cleanup { () =>
      targetZip.delete()
      Files.travel(new File(checkDir), f => f.delete())
      new File(checkDir).delete()
    }
  }

  /** 下载部门压缩包
   *
   * @return
   */
  def downloadDepartZip(): View = {
    val seasonId = getLongId("season")
    val departmentId = getIntId("department")
    val file = ThesisPdfGenerator.getDepartZipFile(seasonId, departmentId, "thesis_check")
    val depart = entityDao.get(classOf[Department], departmentId)
    val season = entityDao.get(classOf[GraduateSeason], seasonId)
    val fileName = season.name + "_" + depart.school.code + "_" + depart.name + "_论文材料.zip"
    Stream(file, MediaTypes.ApplicationZip, fileName)
  }

  def uploadDocForm(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("thesisCheck.season"))
    put("season", season)
    val docTypes = entityDao.findBy(classOf[ThesisDocType], "code", Seq("thesisPaper", "oralDefense", "proposal"))
    put("docTypes", docTypes)
    forward()
  }

  /** 解压文件，上传到blob服务器
   *
   * @param season
   * @param zipfile
   * @param encoding
   * @return
   */
  private def processZip(season: GraduateSeason, zipfile: File, docType: ThesisDocType, encoding: String): Iterable[String] = {
    val messages = Collections.newBuffer[String]
    val blob = EmsApp.getBlobRepository(true)
    val file: ZipFile = if (null == encoding) new ZipFile(zipfile) else new ZipFile(zipfile, encoding)
    var i = 0
    try {
      val en = file.getEntries()
      asScala(en) foreach { ze =>
        i = i + 1
        if (!ze.isDirectory) {
          val fileName = if (ze.getName.contains("/")) Strings.substringAfterLast(ze.getName, "/") else ze.getName
          if (fileName.indexOf(".") < 1) {
            logger.warn(fileName + " format is error")
          } else {
            val usernameCode = Strings.substringBefore(fileName, ".").trim()
            val usercode = usernameCode.toCharArray
            var j = 0
            while (j < usercode.length) {
              if (!Chars.isAsciiAlpha(usercode(j)) && !Chars.isNumber(usercode(j))) {
                usercode(j) = ' '
              }
              j += 1
            }
            var stdCode = new String(usercode.filter(_ != ' '))
            if (Strings.isBlank(stdCode)) stdCode = usernameCode
            val query = OqlBuilder.from(classOf[ThesisCheck], "c")
            query.where("(lower(c.writer.std.code)=:code or c.writerName=:name) and c.season=:season", stdCode.toLowerCase, stdCode.toLowerCase, season)
            val checks = entityDao.search(query)
            if (checks.size != 1) {
              messages.addOne(s"从${usernameCode}找不到唯一的学生")
            } else {
              val writer = checks.head.writer
              val storeName = s"${writer.std.code}." + Strings.substringAfterLast(fileName, ".")

              val dd = entityDao.findBy(classOf[ThesisDoc], "writer" -> writer, "docType" -> docType).headOption.getOrElse(new ThesisDoc)
              dd.writer = writer
              dd.stage = docType.stage
              dd.docType = docType
              if (null != dd.filePath && dd.filePath.startsWith("/")) {
                blob.remove(dd.filePath)
              }
              val meta = blob.upload("/" + writer.season.id.toString + s"/archive/${docType.code}/",
                file.getInputStream(ze), storeName, writer.std.code + " " + writer.std.name)
              dd.fileExt = meta.mediaType
              dd.filePath = meta.filePath
              dd.updatedAt = Instant.now
              entityDao.saveOrUpdate(dd)
              checks foreach { c =>
                if (docType.stage == Stage.OralDefense) {
                  c.defenseDoc = Some(dd)
                } else if (docType.stage == Stage.Proposal) {
                  c.proposalDoc = Some(dd)
                } else if (docType.stage == Stage.ThesisFinalSubmit) {
                  c.paperDoc = Some(dd)
                }
                entityDao.saveOrUpdate(c)
              }
            }
          }
        }
      }
      file.close()
    } catch {
      case e: IOException => Throwables.propagate(e)
    }
    messages
  }

  /**
   * 批量上传材料(stage表示答辩还是开题报告）
   *
   * @return
   */
  def uploadDoc(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("thesisCheck.season"))
    val docType = entityDao.get(classOf[ThesisDocType], getIntId("docType"))
    val messages = Collections.newBuffer[String]
    val parts = getAll("file", classOf[Part])
    if (parts.nonEmpty && parts.head.getSize > 0) {
      val tmpFile = new File(SystemInfo.tmpDir + "/doc" + System.currentTimeMillis())
      IOs.copy(parts.head.getInputStream, new FileOutputStream(tmpFile))
      messages.addAll(processZip(season, tmpFile, docType, "GBK"))
    }
    if (messages.nonEmpty) {
      put("messages", messages)
      put("season", season)
      forward()
    } else {
      redirect("search", s"&thesisCheck.season.id=${season.id}", "上传成功")
    }
  }

  override protected def editSetting(check: ThesisCheck): Unit = {
    put("languages", codeService.get(classOf[Language]))
    super.editSetting(check)
  }

  override def saveAndRedirect(check: ThesisCheck): View = {
    entityDao.saveOrUpdate(check)
    val paper = entityDao.findBy(classOf[ThesisPaper], "writer", check.writer).headOption
    paper foreach { pd =>
      if (check.title != "--") {
        check.writer.thesisTitle = Some(check.title)
        check.writer.researchField = check.researchField
        pd.title = check.title
      }
      if (check.language.nonEmpty) pd.language = check.language
      if (check.keywords.nonEmpty) pd.keywords = check.keywords
      if (check.researchField.nonEmpty) pd.researchField = check.researchField
      entityDao.saveOrUpdate(pd)
    }

    val thesisPaperType = entityDao.findBy(classOf[ThesisDocType], "code", "thesisPaper").head
    val proposalType = entityDao.findBy(classOf[ThesisDocType], "code", "proposal").head
    val defenseType = entityDao.findBy(classOf[ThesisDocType], "code", "oralDefense").head

    entityDao.findBy(classOf[ThesisDocType], "code", "cover") foreach { coverType =>
      uploadSingleDoc(check, "cover_file", coverType)
    }
    uploadSingleDoc(check, "paper_file", thesisPaperType)
    uploadSingleDoc(check, "proposal_file", proposalType)
    uploadSingleDoc(check, "defense_file", defenseType)

    redirect("search", "info.save.success")
  }

  private def uploadSingleDoc(check: ThesisCheck, partName: String, docType: ThesisDocType): Unit = {
    val parts = getAll(partName, classOf[Part])
    val writer = check.writer
    if (parts.nonEmpty && parts.head.getSize > 0) {
      val part = parts.head
      val blob = EmsApp.getBlobRepository(true)
      val storeName = s"${writer.std.code}." + Strings.substringAfterLast(part.getSubmittedFileName, ".")

      val dd = entityDao.findBy(classOf[ThesisDoc], "writer" -> writer, "docType" -> docType).headOption.getOrElse(new ThesisDoc)
      dd.writer = writer
      dd.stage = docType.stage
      if (null != dd.filePath && dd.filePath.startsWith("/")) blob.remove(dd.filePath)

      val meta = blob.upload("/" + writer.season.id.toString + s"/archive/${docType.code}/",
        part.getInputStream, storeName, writer.std.code + " " + writer.std.name)
      dd.fileExt = meta.mediaType
      dd.filePath = meta.filePath
      dd.docType = docType
      dd.updatedAt = Instant.now

      entityDao.saveOrUpdate(dd)
      if (docType.stage == Stage.OralDefense) {
        check.defenseDoc = Some(dd)
      } else if (docType.stage == Stage.Proposal) {
        check.proposalDoc = Some(dd)
      } else if (docType.stage == Stage.ThesisFinalSubmit) {
        check.paperDoc = Some(dd)
      }
      entityDao.saveOrUpdate(check)
      val msg = s"上传了${writer.code}的论文材料:${docType.name}"
      businessLogger.info(msg, writer.id, Map("file" -> dd.filePath))
    }
  }

  @response
  def downloadTemplate(): Any = {
    val writerOnly = getBoolean("writerOnly", false)
    if (writerOnly) {
      val schema = new ExcelSchema()
      val sheet = schema.createScheet("数据模板")
      sheet.add("学位授予单位代码", "dummy1")
      sheet.add("学位授予单位名称", "dummy2")
      sheet.add("姓名", "thesisCheck.writerName").required()
      sheet.add("培养单位码", "dummy3")
      sheet.add("学士学位专业代码", "thesisCheck.degreeMajorCode").required()
      sheet.add("学士学位专业名称", "thesisCheck.degreeMajorName").required()
      sheet.add("证书专业名称", "thesisCheck.certMajorName").required()
      sheet.add("入学年月", "thesisCheck.enrollOn").required()
      sheet.add("学号", "writer.std.code").required()
      sheet.add("考生号", "thesisCheck.examineeCode").required()
      sheet.add("毕业年月", "thesisCheck.graduateOn").required()
      sheet.add("是否主辅修学位", "thesisCheck.majorMinorDegree")
      sheet.add("是否双学士学位", "thesisCheck.dualDegree")
      sheet.add("是否联合学位", "thesisCheck.jointDegree")
      sheet.add("联合培养单位名称", "thesisCheck.jointOrgCode")
      sheet.add("是否第二学位", "thesisCheck.secondDegree")
      sheet.add("是否辅修学位", "thesisCheck.minorDegree")
      sheet.add("学位类型", "thesisCheck.eduType")

      sheet.add("论文类型", "dummy4")
      sheet.add("导师姓名", "dummy5")
      sheet.add("论文题目", "dummy6")
      sheet.add("论文关键词", "dummy7")
      sheet.add("论文研究方向", "dummy8")
      sheet.add("论文撰写语种", "dummy9")
      sheet.add("论文原文或说明文件名称", "dummy10")
      sheet.add("支撑材料文件名称", "dummy11")
      sheet.add("查重报告文件名称", "dummy12")
      sheet.add("是否本专业第一届毕业生", "dummy13")
      sheet.add("毕业生所在院系代码", "thesisCheck.departNo")
      sheet.add("毕业生所在院系名称", "thesisCheck.departName")
      val os = new ByteArrayOutputStream()
      schema.generate(os)
      Stream(new ByteArrayInputStream(os.toByteArray), MediaTypes.ApplicationXlsx, "抽检名单模板.xlsx")
    } else {
      val languages = codeService.get(classOf[Language]).map(x => x.name)
      val schema = new ExcelSchema()
      val thesisTypes = List("毕业论文", "毕业设计", "涉密论文", "无")
      val sheet = schema.createScheet("数据模板")
      sheet.title("论文信息模板")
      sheet.add("学号", "writer.std.code").length(15).required().remark("≤15位")
      sheet.add("学士学位专业代码", "thesisCheck.degreeMajorCode").required()
      sheet.add("论文类型", "thesisCheck.thesisType").ref(thesisTypes).required()
      sheet.add("导师姓名", "thesisCheck.advisor").required()
      sheet.add("论文题目", "thesisCheck.title").length(200).required()
      sheet.add("论文关键词", "thesisCheck.keywords").required().remark("采用全角分号；进行分隔")
      sheet.add("论文研究方向", "thesisCheck.researchField").required()
      sheet.add("论文撰写语种", "thesisCheck.language.name").ref(languages).required()
      sheet.add("是否本专业第一届毕业生", "thesisCheck.firstSeason").required().remark("是/否")

      val os = new ByteArrayOutputStream()
      schema.generate(os)
      Stream(new ByteArrayInputStream(os.toByteArray), MediaTypes.ApplicationXlsx, "论文信息模板.xlsx")
    }
  }

  def importWriters(): View = {
    forward()
  }

  protected override def configImport(setting: ImportSetting): Unit = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("thesisCheck.season"))
    val fl = new ForeignerListener(entityDao)
    fl.addForeigerKey("name")
    setting.listeners = List(fl, new ThesisCheckImportListener(season, entityDao, thesisCheckService))
  }

  override protected def configExport(context: ExportContext): Unit = {
    context.registerFormatter(classOf[LocalDate], TemporalFormatter("yyyyMMdd"))
    context.attrs.indices foreach { i =>
      if (context.titles(i).contains("年月")) {
        context.registerFormatter(context.attrs(i), TemporalFormatter("yyyyMM"))
      }
    }
    context.extractor = new ThesisCheckPropertyExtractor(entityDao)
    context.exporter = new RemarkSimpleExporter(
      List("填写说明：\n1.A-X列内容为原始学位授予信息，请在本科抽检平台【论文上报】-【学位授予信息详情】页面下载，原始数据不允许修改，否则无法完成上传；若S-X列信息全部为空，表明为2024年6月1日之前授予的学位信息，须按照第二行填写说明补充完整；\n2.S-AD列为待补充字段，请按照第二行填写说明进行填写；\n3.如表中含样例数据，上传前须删除样例数据行。",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        "该列为学位授予信息原始数据，请勿修改；",
        """1.若为2024年6月1日之后授予的学位，该列为学位授予信息原始数据，请勿修改；
          |2.若为2024年6月1日之前授予的学位，该列为必填项：下拉选择论文类型，或参照表中附件2论文类型字典填写。"
          |""".stripMargin,
        """1.若为2024年6月1日之后授予的学位，该列为学位授予信息原始数据，请勿修改；
          |2.若为2024年6月1日之前授予的学位，该列为必填项（若S列论文类型为“涉密论文”或“无”，不填）：按实际情况填写，若有多位指导老师，以中英文分号“；”分隔。"
          |""".stripMargin,
        """1.若为2024年6月1日之后授予的学位，该列为学位授予信息原始数据，请勿修改；
          |2.若为2024年6月1日之前授予的学位，该列为必填项；若S列论文类型为“涉密论文”或“无”，不填。"
          |""".stripMargin,
        """1.若为2024年6月1日之后授予的学位，该列为学位授予信息原始数据，请勿修改；
          |2.若为2024年6月1日之前授予的学位，该列为必填项（若S列论文类型为“涉密论文”或“无”，不填）：限100个汉字以内，以中英文分号“；”分隔。
          |""".stripMargin,
        """1.若为2024年6月1日之后授予的学位，该列为学位授予信息原始数据，请勿修改；
          |2.若为2024年6月1日之前授予的学位，该列为必填项（若S列论文类型为“涉密论文”或“无”，不填）：
          |①须为中文；
          |②每个研究方向限15个汉字以内，限填两个研究方向，以中英文分号“；”分隔；
          |③论文研究方向不能与专业名称相同。""".stripMargin,
        """1.若为2024年6月1日之后授予的学位，该列为学位授予信息原始数据，请勿修改；
          |2.若为2024年6月1日之前授予的学位，该列为必填项（若S列论文类型为“涉密论文”或“无”，不填）：下拉选择语种，或参照表中附件3撰写语种字典填写。
          |""".stripMargin,
        """必填；
          |①本列最多可填写一个文件名，仅支持.PDF后缀文件名（后缀名不区分英文大小写）；
          |②若S列为【毕业论文】，则本列填写毕业论文文件名称。建议命名格式：学年度_省市代码_单位代码_专业代码_考生号_LW.PDF。例如2324学年度北京市（11）北京大学（10001）国际经济与贸易（020401）专业，考生号为20610709150378学生的论文建议命名为：2324_11_10001_020401_20610709150378_LW.PDF
          |③若S列为【毕业设计】，则本列填写毕业设计文件名称或毕业设计说明文件名称。建议命名格式：学年度_省市代码_单位代码_专业代码_考生号_BS.PDF。例如2324学年度北京市（11）中国人民大学（10002）美术学（130401）专业，考生号为20610709150378学生的毕业设计建议命名为：2324_11_10002_130401_20610709150378_BS.PDF
          |④若S列为【涉密论文】，则本列须填写相关定密材料文件名称。建议命名格式：学年度_省市代码_单位代码_专业代码_考生号_SM.PDF。例如2324学年度北京市（11）北京理工大学（10007）测控技术与仪器（080301）专业，考生号为20610709150378学生的定密证明材料建议命名为：2324_11_10007_080301_20610709150378_SM.PDF
          |⑤若S列为【其他】，则本列填写文本类文件名称或非文本类说明文件名称。建议命名格式：学年度_省市代码_单位代码_专业代码_考生号_QT.PDF。例如2324学年度北京市（11）中国传媒大学（10033）表演（130301）专业，考生号为20610709150378学生的文本类文件名称建议命名为：2324_11_10033_130301_20610709150378_QT.PDF
          |⑥若S列为【无】，则本列填写《普通高等学校本科专业类教学质量国家标准》不要求做毕业论文（设计）的规定或培养方案、成绩单等证明材料文件名称。（《标准》中有毕业论文（设计）要求的，须上传本校该专业的培养方案。）
          |建议命名格式：学年度_省市代码_单位代码_专业代码_考生号_W.PDF。例如2324学年度北京市（11）首都医科大学（10025）临床医学（100201K）专业，考生号为20610709150378学生的无毕业论文（设计）证明材料建议命名为：2324_11_10025_100201K_20610709150378_W.PDF。
          |无论文如需上传同一份证明材料，须填写相同的文件名称。""".stripMargin,
        """选填（若为非文本类毕业设计或非文本类“其他”，则该列为必填）；
          |本列最多可填写一个文件名，填写内容为支撑材料压缩包文件名称，仅支持.ZIP后缀文件名（后缀名不区分英文大小写）；压缩包文件大小不超过2GB。
          |建议压缩包文件命名规则：学年度_省市代码_单位代码_专业代码_考生号_CL.ZIP。例如2324学年度北京市（11）北京航空航天大学（10006）软件工程（080902）专业，考生号为20610709150378学生的支撑材料文件建议命名为：2324_11_10006_080902_20610709150378_CL.ZIP""".stripMargin,
        """选填（若所在省级教育行政部门设置了上传查重报告的要求，则该列为必填）；
          |本列最多可填写一个文件名，填写内容为查重报告文件名称，仅支持.PDF后缀文件名（后缀名不区分英文大小写）。
          |建议查重报告文件命名规则：学年度_省市代码_单位代码_专业代码_考生号_CCBG.PDF。例如2324学年度北京市（11）中国矿业大学（北京）（11413）法学（030101K）专业，考生号为20610709150378学生的查重报告文件建议命名为：2324_11_11413_030101K_20610709150378_CCBG.PDF""".stripMargin,
        """必填；
          |填写“是”或“否”。"
          |""".stripMargin,
        """必填；
          |限20字符以内；
          |若校级用户已开通院系用户上报，则该列内容须与校级用户【院系用户管理】页面“院系代码”保持一致。"
          |""".stripMargin,
        """必填；
          |限20字符以内；
          |若校级用户已开通院系用户上报，则该列内容须与校级用户【院系用户管理】页面“院系名称”保持一致。"
          |""".stripMargin
      ))
    super.configExport(context)
  }

  /** 关联文档到论文抽检
   *
   * @return
   */
  def updateDoc(): View = {
    val season = entityDao.get(classOf[GraduateSeason], getLongId("thesisCheck.season"))
    val checks = entityDao.findBy(classOf[ThesisCheck], "season", season)
    checks foreach { c =>
      thesisCheckService.updateDoc(c)
    }
    redirect("search", "操作成功")
  }
}
