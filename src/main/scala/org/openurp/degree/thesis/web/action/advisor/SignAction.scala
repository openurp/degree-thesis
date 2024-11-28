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

package org.openurp.degree.thesis.web.action.advisor

import org.beangle.ems.app.{Ems, EmsApp}
import org.beangle.webmvc.annotation.mapping
import org.beangle.webmvc.view.View
import org.openurp.degree.thesis.model.Signature
import org.openurp.degree.thesis.web.helper.SignatureHelper

import java.io.ByteArrayInputStream

/** 教师签名
 */
class SignAction extends AdvisorSupport {

  def index(): View = {
    val writers = getWriters
    put("writers", writers)
    val signature = entityDao.findBy(classOf[Signature], "writer", writers).find(_.advisorUrl.nonEmpty)
    signature foreach { s =>
      s.advisorUrl foreach { url => put("signature", SignatureHelper.readBase64(url)) }
    }
    put("EmsBase", Ems.base)
    forward()
  }

  def upload(): View = {
    forward()
  }

  @mapping(value = "new", view = "form")
  def editNew(): View = {
    val advisor = getAdvisor
    val writers = getWriters
    put("writers", writers)
    val signature = entityDao.findBy(classOf[Signature], "writer", writers).find(_.advisorUrl.nonEmpty)
    signature foreach { s =>
      s.advisorUrl foreach { url => put("signature", SignatureHelper.readBase64(url)) }
    }
    forward()
  }

  def save(): View = {
    get("signature") foreach { code =>
      val advisor = getAdvisor
      val blob = EmsApp.getBlobRepository(true)
      val writers = getWriters
      val first = entityDao.findBy(classOf[Signature], "writer", writers).find(_.advisorUrl.nonEmpty).headOption
      first foreach { s =>
        s.advisorUrl foreach { url => blob.remove(url) }
      }

      val season = thesisPlanService.getSeason()
      val sign = blob.upload("/" + season.id.toString + s"/signature/",
        new ByteArrayInputStream(code.getBytes), advisor.code + ".png.txt", advisor.code + " " + advisor.name)

      for (writer <- writers) {
        val signature = entityDao.findBy(classOf[Signature], "writer", writer).headOption.getOrElse(new Signature(writer))
        signature.advisorUrl = Some(sign.filePath)
        entityDao.saveOrUpdate(signature)
      }
    }
    redirect("index", "签名成功")
  }

  def remove(): View = {
    val advisor = getAdvisor
    val writers = getWriters
    val blob = EmsApp.getBlobRepository(true)
    val signatures = entityDao.findBy(classOf[Signature], "writer", writers)
    val first = signatures.find(_.advisorUrl.nonEmpty).headOption
    first foreach { s =>
      s.advisorUrl foreach { url =>
        blob.remove(url)
      }
    }

    signatures.foreach { s => s.advisorUrl = None }
    entityDao.saveOrUpdate(signatures)
    businessLogger.info(s"删除签名", advisor.id, Map.empty)
    redirect("index", "删除成功")
  }
}
