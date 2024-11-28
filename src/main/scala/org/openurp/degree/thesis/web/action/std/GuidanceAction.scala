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

import org.beangle.commons.conversion.string.EnumConverters
import org.beangle.data.model.Entity
import org.beangle.webmvc.view.View
import org.openurp.degree.thesis.model.{Guidance, Stage}

import java.time.Instant
import scala.collection.mutable

class GuidanceAction extends WriterSupport {

  def index(): View = {
    val writer = getWriter
    val stage = EnumConverters.convert(get("stage", "16"), classOf[Stage])
    val guidances = entityDao.findBy(classOf[Guidance], "writer" -> writer, "stage" -> stage)
    put("guidances", guidances)
    put("writer", writer)
    put("stage", stage)
    put("plan", thesisPlanService.getDepartPlan(writer).get)
    forward()
  }

  def edit(): View = {
    val writer = getWriter
    val stage = EnumConverters.convert(get("stage", "0"), classOf[Stage]).asInstanceOf[Stage]
    val guidances = entityDao.findBy(classOf[Guidance], "writer" -> writer, "stage" -> stage)
    put("guidances", guidances)
    put("writer", writer)
    put("stage", stage)
    put("plan", thesisPlanService.getDepartPlan(writer).get)
    forward()
  }

  def save(): View = {
    val writer = getWriter
    val stage = EnumConverters.convert(get("stage", "0"), classOf[Stage]).asInstanceOf[Stage]
    val guidances = entityDao.findBy(classOf[Guidance], "writer" -> writer, "stage" -> stage)
    val gMap = guidances.map(g => (g.idx, g)).toMap
    val saved = new mutable.ArrayBuffer[Entity[_]]
    Array[Short](1, 2) foreach { idx =>
      get(s"contents$idx") foreach { content =>
        val g = gMap.getOrElse(idx, new Guidance)
        g.writer = writer
        g.updatedAt = Instant.now
        g.idx = idx
        g.stage = stage
        g.contents = content
        saved += g
      }
    }
    val deadline = writer.getOrCreateDeadline(stage)
    deadline.submitAt = Some(Instant.now)
    deadline.updatedAt = Instant.now
    saved += deadline
    entityDao.saveOrUpdate(saved)
    redirect("index", "stage=" + stage.id, "info.save.success")
  }
}
