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

import org.beangle.webmvc.view.{Status, View}
import org.beangle.webmvc.support.action.EntityAction
import org.openurp.degree.thesis.model.*

class CommitmentAction extends AdvisorSupport, EntityAction[Commitment] {

  def index(): View = {
    forward()
  }

  def search(): View = {
    val writers = getWriters
    put("writers", writers)

    val commitments = entityDao.findBy(classOf[Commitment], "writer", writers).map(x => (x.writer, x)).toMap
    put("commitments", commitments)
    put("stage", Stage.Commitment)
    forward()
  }

  def info(): View = {
    val c = entityDao.findBy(classOf[Commitment], "writer.id", getLongId("writer")).headOption
    c match {
      case Some(commitment) =>
        val writer = commitment.writer
        put("commitment", commitment)
        put("writer", writer)
        put("deadline", writer.getOrCreateDeadline(Stage.Commitment))
        put("plan", thesisPlanService.getDepartPlan(writer).get)
        forward()
      case None => Status.NotFound
    }
  }

}
