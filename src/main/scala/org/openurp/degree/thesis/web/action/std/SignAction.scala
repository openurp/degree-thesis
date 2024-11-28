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

import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.webmvc.annotation.mapping
import org.beangle.webmvc.view.View
import org.openurp.degree.thesis.model.Signature
import org.openurp.degree.thesis.web.helper.SignatureHelper

import java.io.ByteArrayInputStream

class SignAction extends WriterSupport {

  def index(): View = {
    val writer = getWriter
    entityDao.findBy(classOf[Signature], "writer", writer).headOption foreach { s =>
      s.writerUrl foreach { l => put("signature", SignatureHelper.readBase64(l)) }
    }
    put("EmsBase", Ems.base)
    forward()
  }

  @mapping(value = "new", view = "form")
  def editNew(): View = {
    val writer = getWriter
    entityDao.findBy(classOf[Signature], "writer", writer).headOption foreach { s =>
      s.writerUrl foreach { l => put("signature", SignatureHelper.readBase64(l)) }
    }
    forward()
  }

  def save(): View = {
    get("signature") foreach { code =>
      val writer = getWriter
      val blob = EmsApp.getBlobRepository(true)
      val signature = entityDao.findBy(classOf[Signature], "writer", writer).headOption.getOrElse(new Signature(writer))
      signature.writerUrl foreach { url =>
        blob.remove(url)
      }

      val sign = blob.upload("/" + writer.season.id.toString + s"/signature/",
        new ByteArrayInputStream(code.getBytes), writer.code + ".png.txt", writer.code + " " + writer.name)

      signature.writerUrl = Some(sign.filePath)
      entityDao.saveOrUpdate(signature)
      businessLogger.info("上传签名", signature.id, Map.empty)
    }
    redirect("index", "签名成功")
  }

  def remove(): View = {
    val writer = getWriter
    entityDao.findBy(classOf[Signature], "writer", writer).headOption foreach { s =>
      s.writerUrl foreach { url =>
        EmsApp.getBlobRepository(true).remove(url)
      }
      s.writerUrl = None
      if (s.advisorUrl.isEmpty) {
        entityDao.remove(s)
      } else {
        entityDao.saveOrUpdate(s)
      }
      businessLogger.info(s"删除签名", s.id, Map.empty)
    }
    redirect("index", "删除成功")
  }
}
