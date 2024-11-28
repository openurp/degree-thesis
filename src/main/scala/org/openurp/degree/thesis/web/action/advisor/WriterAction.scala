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

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.webmvc.support.{ActionSupport, ParamSupport}
import org.beangle.webmvc.view.View
import org.openurp.degree.thesis.model.{ThesisPaper, ThesisReview, Writer}
import org.openurp.degree.thesis.service.ThesisPlanService

class WriterAction extends AdvisorSupport {

  def index(): View = {
    val advisor = getAdvisor
    val writers = getWriters
    put("writers", writers)
    thesisPlanService.getPlan() foreach { plan =>
      val departPlans = plan.departPlans.filter(x => advisor.departs.contains(x.department))
      put("departPlans", departPlans)
    }
    val papers = entityDao.findBy(classOf[ThesisPaper], "writer", writers).map(x => (x.writer, x)).toMap
    put("papers", papers)
    put("advisor", advisor)
    put("historyWriters", getHistoryWriters)
    forward()
  }

  def saveProfile(): View = {
    val advisor = getAdvisor
    advisor.mobile = get("mobile")
    advisor.email = get("email")
    advisor.description = get("description")
    entityDao.saveOrUpdate(advisor)
    redirect("index", "保存成功")
  }
}
