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

import org.apache.poi.hssf.usermodel.*
import org.apache.poi.ss.usermodel.BorderStyle
import org.beangle.commons.lang.ClassLoaders
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{DefenseWriter, ThesisReview}

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util
import java.util.List

object WriterReport {

  def renderThesis(season: GraduateSeason, reviews: Seq[ThesisReview]): HSSFWorkbook = {
    val hsw = new HSSFWorkbook(ClassLoaders.getResourceAsStream("org/openurp/degree/thesis/template/report_thesis.xls").get)
    val sheet = hsw.getSheetAt(0)
    val font = hsw.createFont
    font.setFontName("宋体")
    font.setFontHeightInPoints(10.toShort)
    val style = hsw.createCellStyle
    style.setFont(font)
    val border = BorderStyle.valueOf(1.asInstanceOf[Short])
    style.setBorderLeft(border)
    style.setBorderBottom(border)
    style.setBorderRight(border)
    style.setBorderTop(border)
    sheet.shiftRows(26, 28, reviews.size - 22)

    sheet.getRow(0).getCell(0).setCellValue(s"${season.project.school.name}${season.code}届全日制本科生毕业论文信息汇总表\r\n(此表由教学秘书填写)")
    for (i <- 0 until reviews.size) {
      val r = reviews(i)
      val row = sheet.createRow(i + 4)
      val cell = new Array[HSSFCell](8)
      for (j <- 0 until 8) {
        cell(j) = row.createCell(j)
        cell(j).setCellStyle(style)
      }
      cell(0).setCellValue(i + 1)
      cell(1).setCellValue(r.writer.name)
      cell(2).setCellValue(r.writer.code)
      cell(3).setCellValue(r.writer.major.name)
      cell(4).setCellValue(r.writer.squad.map(_.name).getOrElse(""))
      cell(5).setCellValue(r.writer.advisor.map(_.name).getOrElse(""))
      cell(6).setCellValue(r.writer.thesisTitle.getOrElse(""))
      r.finalScoreText foreach { s => cell(7).setCellValue(s) }
    }
    hsw
  }

  def renderDefense(season: GraduateSeason, defenseWriters: Seq[DefenseWriter]): HSSFWorkbook = {
    val hsw = new HSSFWorkbook(ClassLoaders.getResourceAsStream("org/openurp/degree/thesis/template/report_defense.xls").get)
    val sheet: HSSFSheet = hsw.getSheetAt(0)
    val font: HSSFFont = hsw.createFont
    font.setFontName("宋体")
    font.setFontHeightInPoints(10.toShort)
    val style: HSSFCellStyle = hsw.createCellStyle
    val border = BorderStyle.valueOf(1.asInstanceOf[Short])
    style.setBorderLeft(border)
    style.setBorderBottom(border)
    style.setBorderRight(border)
    style.setBorderTop(border)
    style.setFont(font)
    sheet.shiftRows(30, 30, defenseWriters.size - 26)
    val pattern1 = DateTimeFormatter.ofPattern("MM月dd日HH:mm")
    val pattern2 = DateTimeFormatter.ofPattern("HH:mm")
    sheet.getRow(0).getCell(0).setCellValue(s"${season.project.school.name}${season.code}届全日制本科生毕业论文答辩安排信息汇总表")
    for (i <- 0 until defenseWriters.size) {
      val dw = defenseWriters(i)
      val row: HSSFRow = sheet.createRow(i + 4)
      val cell: Array[HSSFCell] = new Array[HSSFCell](11)
      for (j <- 0 until 11) {
        cell(j) = row.createCell(j)
        cell(j).setCellStyle(style)
      }
      cell(0).setCellValue(i + 1)
      cell(1).setCellValue(dw.writer.name)
      cell(2).setCellValue(dw.writer.code)
      cell(3).setCellValue(dw.writer.advisor.map(_.name).getOrElse(""))
      cell(4).setCellValue(dw.writer.thesisTitle.getOrElse(""))
      val group = dw.group
      cell(5).setCellValue(group.idx)
      val leader = group.leaderTeacher
      var mCount = 0
      leader foreach { l =>
        mCount += 1
        cell(5 + mCount).setCellValue(l.name)
      }
      group.memberTeachers foreach { t =>
        mCount += 1
        if (mCount <= 3) {
          cell(5 + mCount).setCellValue(t.name)
        }
      }
      if (mCount < 3) {
        (mCount until 3).foreach { m =>
          mCount += 1
          cell(5 + mCount).setCellValue("")
        }
      }
      group.beginAt foreach { beginAt =>
        var time = pattern1.format(beginAt.atZone(ZoneId.systemDefault()))
        group.endAt.foreach { endAt =>
          time += ("-" + pattern2.format(endAt.atZone(ZoneId.systemDefault())))
        }
        cell(9).setCellValue(time)
      }
      cell(10).setCellValue(group.place.getOrElse(""))
    }
    hsw
  }
}
