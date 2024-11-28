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

package org.openurp.degree.thesis.service.impl

import org.beangle.data.dao.EntityDao
import org.openurp.degree.thesis.model.{Deadline, DelayDays, Stage}
import org.openurp.degree.thesis.service.DeferService

import java.time.*

class DeferServiceImpl extends DeferService {
  var entityDao: EntityDao = _

  override def defer(deadline: Deadline, stage: Stage): Unit = {
    deadline.delayCount += 1
    val endAt = deadline.endAt match {
      case None => Instant.now
      case Some(ea) => ea
    }
    val newEndAt = if endAt.isBefore(Instant.now) then Instant.now else endAt

    deadline.endAt = Some(deferDays(newEndAt, DelayDays.dayOf(stage)))
    deadline.updatedAt = Instant.now
    entityDao.saveOrUpdate(deadline)
  }

  private def deferDays(i: Instant, days: Int): Instant = {
    val next = i.atZone(ZoneId.systemDefault()).toLocalDate.plusDays(days)
    //不用使用LocalTime.MAX,有可能存储到数据库系统中显示次日的凌晨了
    LocalDateTime.of(next,LocalTime.of(23, 59, 59)).atZone(ZoneId.systemDefault()).toInstant
  }

}
