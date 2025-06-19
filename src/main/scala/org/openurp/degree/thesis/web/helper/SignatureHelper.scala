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

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.codec.binary.Base64
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Strings
import org.beangle.ems.app.EmsApp

import java.io.{File, FileInputStream, InputStream}

object SignatureHelper {

  def readBase64(url: String): String = {
    val blob = EmsApp.getBlobRepository(true)
    try {
      IOs.readString(blob.url(url).get.openStream())
    } catch
      case e: Exception => null
  }

  def readBase64toBytes(url: String): Array[Byte] = {
    val blob = EmsApp.getBlobRepository(true)
    try {
      var contents  = IOs.readString(blob.url(url).get.openStream())
        contents = Strings.substringAfter(contents,";base64,")
        Base64.decode(contents)
    } catch
      case e: Exception => null
  }

  def toBase64(file: File): String = {
    toBase64(new FileInputStream(file), file.getName)
  }

  def toBase64(is: InputStream, fileName: String): String = {
    val contentType = MediaTypes.get(Strings.substringAfterLast(fileName, ".")).map(_.toString).getOrElse("image/png")
    val data = Base64.encode(IOs.readBytes(is))
    s"data:${contentType};base64,${data}"
  }
}
