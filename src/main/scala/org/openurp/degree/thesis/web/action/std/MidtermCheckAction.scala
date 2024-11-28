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
import org.openurp.base.model.AuditStatus
import org.openurp.degree.thesis.model.*
import org.openurp.degree.thesis.web.helper.StageChecker

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, ZoneId}

class MidtermCheckAction extends WriterSupport {
  def index(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    val midtermCheck = entityDao.findBy(classOf[MidtermCheck], "writer", writer).headOption
    val deadline = writer.getOrCreateDeadline(Stage.MidtermCheck)

    val stageTime = plan.getStageTime(Stage.MidtermCheck)
    var message = ""
    deadline.endAt match {
      case None => message = "请于 " + stageTime.endOn + "前完成撰写并提交"
      case Some(endAt) =>
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        message = "请于 " + endAt.atZone(ZoneId.systemDefault()).format(formatter) + "前完成提交"
    }
    put("message", message)
    if (deadline.delayCount > 0) {
      put("delay", "你的中期检查报告已延期 " + deadline.delayCount + "次 ,多次延期将影响到论文最终成绩");
    }
    put("writer", writer)
    put("midtermCheck", midtermCheck)
    forward()
  }

  def edit(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    new StageChecker(entityDao, plan).check(writer, Stage.MidtermCheck, true) match {
      case Some(msg) => return redirect("index", msg)
      case None =>
    }
    val midtermCheck = entityDao.findBy(classOf[MidtermCheck], "writer", writer).headOption.getOrElse(new MidtermCheck)
    put("writer", writer)
    put("midtermCheck", midtermCheck)
    forward()
  }

  def info(): View = {
    val writer = getWriter
    put("writer", writer)
    val cQuery = OqlBuilder.from(classOf[MidtermCheck], "c")
    cQuery.where("c.writer=:me", writer)
    put("midtermCheck", entityDao.search(cQuery).headOption)

    forward()
  }

  def save(): View = {
    val writer = getWriter
    val plan = thesisPlanService.getDepartPlan(writer).get
    new StageChecker(entityDao, plan).check(writer, Stage.MidtermCheck, true) match {
      case Some(msg) => return redirect("index", msg)
      case None =>
    }
    val k = entityDao.findBy(classOf[MidtermCheck], "writer", writer).headOption.getOrElse(new MidtermCheck)
    k.proceeding = get("proceeding", "")
    k.submitAt = Instant.now
    k.writer = writer
    k.status = AuditStatus.Submited
    entityDao.saveOrUpdate(k)
    redirect("index", "info.save.success")
  }

}
