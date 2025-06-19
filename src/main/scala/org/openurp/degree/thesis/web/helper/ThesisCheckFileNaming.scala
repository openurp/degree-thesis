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

package org.openurp.degree.thesis.web.helper

import org.beangle.commons.lang.Strings
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{ThesisCheck, Writer}

object ThesisCheckFileNaming {

  def paperFileName(check: ThesisCheck): String = {
    s"${fileName(check)}_LW.pdf"
  }

  def paperFileName2(check: ThesisCheck): String = {
    s"${fileName(check)}_LW." + Strings.substringAfterLast(check.paperDoc.get.filePath, ".")
  }

  def attachFileName(check: ThesisCheck): String = {
    s"${fileName(check)}_CL.zip"
  }

  private def majorCode(writer: Writer): String = {
    var major = writer.std.state.get.major.getDisciplineCode(writer.std.graduateOn)
    if (major.isEmpty) major = "x"
    major
  }

  private def fileName(check: ThesisCheck): String = {
    val schoolYear = getShortSchoolYear(check.season)
    val city = "31" //上海
    val school = check.writer.std.project.school.code
    val major = check.degreeMajorCode
    val code = check.writer.std.code
    s"${schoolYear}_${city}_${school}_${major}_${code}"
  }

  def getShortSchoolYear(season: GraduateSeason): String = {
    val year = season.graduateIn.getYear.toString
    val endYear = year.substring(year.length - 2)
    val beforeYear = (endYear.toInt - 1).toString
    s"${beforeYear}${endYear}" //check.season.graduateOn
  }
}
