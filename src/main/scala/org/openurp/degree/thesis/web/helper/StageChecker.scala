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

import org.beangle.commons.bean.Properties
import org.beangle.data.dao.EntityDao
import org.beangle.data.model.Entity
import org.openurp.base.model.AuditStatus
import org.openurp.degree.thesis.model.Stage.*
import org.openurp.degree.thesis.model.{Commitment, MidtermCheck, Proposal, *}

import java.time.{Instant, LocalDate}

class StageChecker(entityDao: EntityDao, plan: DepartPlan) {

  def check(writer: Writer, stage: Stage, checkTime: Boolean): Option[String] = {
    val deadline = writer.getOrCreateDeadline(stage)
    val stageName = stage.name
    val clazz: Class[? <: Entity[Long]] =
      stage match
        case Stage.Subject => null
        case Stage.SubjectReview => null
        case Stage.SubjectChosen => null
        case Stage.SubjectChosenRound1 => null
        case Stage.SubjectChosenRound2 => null
        case Stage.Commitment => classOf[Commitment]
        case Stage.Proposal => classOf[Proposal]
        case Stage.Guidance => null
        case Stage.Guidance1 => null
        case Stage.MidtermCheck => classOf[MidtermCheck]
        case Stage.Guidance2 => null
        case Stage.ThesisDraftSubmit => null
        case Stage.ThesisFinalSubmit => null
        case Stage.ThesisReview => null
        case Stage.OralDefense => null
        case Stage.ThesisArchiveSubmit => null
        case _ => null

    if (null == clazz) return None

    val result = entityDao.findBy(clazz, "writer", writer).headOption
    if (checkTime) {
      if (result.isEmpty) {
        deadline.endAt match
          case None =>
            val stageTime = plan.getStageTime(stage)
            if stageTime.endOn.isBefore(LocalDate.now) then return Some(s"${stageName}工作时间已过，请联系教学秘书")
          case Some(endAt) =>
            if endAt.isBefore(Instant.now) then return Some(s"${stageName}填写已经结束，若仍需确认请联系学院教学秘书做延期处理")
      }
    }

    result match {
      case Some(p) =>
        val status: AuditStatus = Properties.get(p, "status")
        if status == AuditStatus.Passed then
          Some(s"${stageName}审查已通过，如需修改，请联系指导教师驳回")
        //        else if status == AuditStatus.Submited then
        //          Some(s"${stageName}已经提交，正在审查，不能修改")
        else None
      case None => None
    }
  }
}
