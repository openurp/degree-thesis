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

import org.beangle.data.dao.OqlBuilder
import org.beangle.webmvc.view.View
import org.openurp.degree.thesis.model.{Commitment, DepartPlan, Stage}

import java.time.Instant

class CommitmentAction extends WriterSupport {

  def index(): View = {
    val writer = getWriter

    val cQuery = OqlBuilder.from(classOf[Commitment], "c")
    cQuery.where("c.writer=:me", writer)
    put("commitment", entityDao.search(cQuery).headOption)

    put("writer", writer)
    put("deadline", writer.getOrCreateDeadline(Stage.Commitment))
    val plan = thesisPlanService.getDepartPlan(writer).get
    put("plan", plan)
    forward()
  }

  def confirm(): View = {
    val writer = getWriter
    val deadline = writer.getOrCreateDeadline(Stage.Commitment)
    if (deadline.endAt.nonEmpty && deadline.endAt.get.isBefore(Instant.now)) {
      return redirect("index", "任务书确认工作已经结束，若仍需确认请联系学院教学秘书做延期处理")
    }
    val cQuery = OqlBuilder.from(classOf[Commitment], "c")
    cQuery.where("c.writer=:me", writer)
    val comments = entityDao.search(cQuery).headOption
    val c = comments match {
      case None =>
        val c = new Commitment
        c.writer = writer
        c
      case Some(c) => c
    }
    if (!c.confirmed) {
      c.confirmed = true
      c.updatedAt = Instant.now
      val deadline = writer.getOrCreateDeadline(Stage.Commitment)
      deadline.submitAt = Some(Instant.now)
      deadline.updatedAt = Instant.now
      entityDao.saveOrUpdate(c, deadline)
    }
    redirect("index", "info.save.success")
  }
}
