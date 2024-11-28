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

import org.apache.poi.hssf.usermodel.{HSSFCell, HSSFWorkbook}
import org.apache.poi.ss.usermodel.BorderStyle
import org.beangle.commons.lang.ClassLoaders
import org.beangle.ems.app.security.RemoteService
import org.openurp.degree.thesis.model.{DefenseGroup, ThesisReview, Writer}

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import scala.collection.mutable

object DefenseReport {

  def render(group: DefenseGroup, reviews: Map[Writer, ThesisReview]): HSSFWorkbook = {
    val hsw = new HSSFWorkbook(ClassLoaders.getResourceAsStream("org/openurp/degree/thesis/template/defense_group.xls").get)
    val sheet = hsw.getSheetAt(0)
    val font = hsw.createFont
    font.setFontName("宋体")
    font.setFontHeightInPoints(10.toShort)
    val style = hsw.createCellStyle
    style.setFont(font)
    style.setBorderLeft(BorderStyle.THIN)
    style.setBorderBottom(BorderStyle.THIN)
    style.setBorderRight(BorderStyle.THIN)
    style.setBorderTop(BorderStyle.THIN)

    val schoolName = RemoteService.getOrg.name

    val title = schoolName + group.department.name + "第" + group.idx + "答辩组答辩表"
    sheet.getRow(0).getCell(0).setCellValue(title)
    val members = new mutable.StringBuilder("答辩组长：")
    group.members foreach { m =>
      if m.leader then members.append(m.teacher.name).append(" ")
    }
    members.append("答辩组成员：")
    group.members foreach { m =>
      if !m.leader then members.append(m.teacher.name).append(" ")
    }
    members.append("答辩时间: ")
    group.beginAt foreach { beginAt =>
      members.append(beginAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM月dd日HH:mm")))
      members.append("-")
      group.endAt foreach { endAt =>
        members.append(endAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")))
      }
    }
    members.append("地点:" + group.place.getOrElse("--"))
    sheet.getRow(2).getCell(0).setCellValue(members.toString)
    var i = 0
    val writers = group.orderedWriters
    while (i < writers.size) {
      val xs = writers(i)
      val row = sheet.createRow(i + 5)
      val cell = new Array[HSSFCell](12)
      for (j <- 0 until 12) {
        cell(j) = row.createCell(j)
        cell(j).setCellStyle(style)
      }
      cell(0).setCellValue(i + 1)
      cell(1).setCellValue(xs.name)
      cell(2).setCellValue(xs.code)
      cell(3).setCellValue(xs.squad.map(_.name).getOrElse("--") + "班")
      cell(4).setCellValue(xs.advisor.map(_.name).getOrElse("--"))
      cell(5).setCellValue(xs.thesisTitle.getOrElse("--"))
      reviews.get(xs) foreach { review =>
        review.crossReviewer foreach { crossReviewer => cell(6).setCellValue(crossReviewer.name) }
        review.advisorScore foreach { advisorScore => cell(7).setCellValue(advisorScore) }
        review.crossReviewScore foreach { score => cell(8).setCellValue(score) }
        review.defenseScore foreach { score => cell(9).setCellValue(score) }
        review.finalScore foreach { score => cell(10).setCellValue(score) }
      }
      i += 1
    }
    hsw
  }
}
