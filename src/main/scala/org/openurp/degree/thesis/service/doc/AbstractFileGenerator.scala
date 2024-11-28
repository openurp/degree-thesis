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
import org.beangle.commons.io.Files
import org.beangle.commons.lang.{Chars, ClassLoaders}
import org.beangle.commons.net.http.HttpUtils
import org.beangle.ems.app.{Ems, EmsApp}
import org.openurp.degree.thesis.model.Writer

import java.io.File
import java.net.URL
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import scala.collection.mutable

object AbstractFileGenerator {

  private def byteLength(str: String): Int = {
    val chars = str.toCharArray
    var l = 0
    chars.indices foreach { i =>
      val c = chars(i)
      if (Chars.isNumber(c) || Chars.isAsciiAlpha(c) || Character.isWhitespace(c)) {
        l += 1
      } else {
        l += 2
      }
    }
    l
  }

  /** 将论文题目拆分成两段，第一段不超过firstLineMaxLength个占位符
   * 中文每个字符两个占位符，字母和数字占一个占位符
   *
   * @param title
   * @return
   */
  def splitTitle(title: String, maxlength: Int): (String, String) = {
    val bytelen = byteLength(title)
    if bytelen <= maxlength then (title, "")
    else if (title.matches("[a-zA-Z '‘’—（）()\"“”-]*")) {
      var seqIdx =  if bytelen > 2 * maxlength then bytelen / 2 else maxlength
      if (title.charAt(seqIdx - 1) == ' ') {
        (title.substring(0, seqIdx).trim(), title.substring(seqIdx).trim())
      } else {
        seqIdx -= 1
        (title.substring(0, seqIdx).trim() + "-", title.substring(seqIdx).trim())
      }
    } else {
      val seqIdx = if bytelen > 2 * maxlength then bytelen / 2 else maxlength
      val chars = title.toCharArray
      var l = 0
      var firstEndIdx = title.length
      var i = 0
      while (i < title.length) {
        val c = chars(i)
        if (Chars.isNumber(c) || Chars.isAsciiAlpha(c) || Character.isWhitespace(c)) {
          l += 1
        } else {
          l += 2
        }
        if (l >= seqIdx) {
          firstEndIdx = if l == seqIdx then i + 1 else i
          i = title.length
        }
        i += 1
      }
      //第二行，要么空，要么至少有两个字符
      if firstEndIdx == title.length - 1 then firstEndIdx -= 1
      (title.substring(0, firstEndIdx).trim(), if (firstEndIdx >= title.length) then "" else title.substring(firstEndIdx).trim())
    }
  }
}

abstract class AbstractFileGenerator {

  /** 论文题目首行最大字符数 */
  val LineMaxLength = 24

  protected def getTemplate(name: String): Option[URL] = {
    ClassLoaders.getResources(s"org/openurp/degree/thesis/template/${name}").headOption match
      case None =>
        val url = new URL(s"${Ems.api}/platform/config/files/${EmsApp.name}/org/openurp/degree/thesis/template/$name")
        val status = HttpUtils.access(url)
        if status.isOk then Some(url) else None
      case a@Some(url) => a
  }

  protected def extract(writer: Writer): mutable.Map[String, String] = {
    val data = Collections.newMap[String, String]
    var tm1 = ""
    var tm2 = ""
    writer.thesisTitle foreach { title =>
      val tms = AbstractFileGenerator.splitTitle(title, LineMaxLength)
      tm1 = tms._1
      tm2 = tms._2
      data.put("thesis_title", title) //xtmc
    }
    data.put("thesis_title1", tm1)
    data.put("thesis_title2", tm2)
    data.put("department_name", writer.std.state.get.department.name)
    data.put("major_name", writer.std.state.get.major.name)
    data.put("squad_name", writer.squad.map(_.name).getOrElse("--") + "班")
    data.put("std_code", writer.code)
    data.put("std_name", writer.name)

    data.put("advisor_name", writer.advisor.map(_.name).getOrElse("--"))
    data
  }

  protected def addDate(name: String, oi: Option[Instant], data: mutable.Map[String, String]): Unit = {
    data.put(name, "    年    月   日")
    oi foreach { i =>
      val formatter = DateTimeFormatter.ofPattern("YYYY年 MM月 dd日")
      data.put(name, formatter.format(i.atZone(ZoneId.systemDefault())))
    }
  }

  protected def addScore(name: String, value: Option[Int], data: mutable.Map[String, String]): Unit = {
    value foreach { v => data.put(name, v.toString) }
  }

  def getDepartZipFile(seasonId: Long, departId: Int, docType: String): File = {
    new File(System.getProperty("java.io.tmpdir") + s"season${seasonId}" + Files./ + s"${docType}${departId}.zip")
  }

  def getDepartFolder(seasonId: Long, departId: Int, docType: String): String = {
    val file = new File(System.getProperty("java.io.tmpdir") + s"season${seasonId}" + Files./ + s"${docType}${departId}" + Files./)
    if (file.exists()) {
      Files.travel(file, f => f.delete())
    }
    file.mkdirs()
    file.getAbsolutePath
  }

}
