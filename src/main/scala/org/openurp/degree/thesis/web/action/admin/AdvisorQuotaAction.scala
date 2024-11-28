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

package org.openurp.degree.thesis.web.action.admin

import org.beangle.data.dao.OqlBuilder
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}
import org.openurp.base.edu.model.TeachingOffice
import org.openurp.base.model.Project
import org.openurp.degree.thesis.model.Advisor
import org.openurp.starter.web.support.ProjectSupport

import java.time.LocalDate

class AdvisorQuotaAction extends RestfulAction[Advisor], ExportSupport[Advisor], ProjectSupport {

  override protected def indexSetting(): Unit = {
    given project: Project = getProject

    val departs = getDeparts
    put("departs", departs)
    put("offices", entityDao.findBy(classOf[TeachingOffice], "department", departs))
    super.indexSetting()
  }

  def batchUpdateSetting(): View = {
    val advisors = entityDao.find(classOf[Advisor], getLongIds("advisor"))
    put("advisors", advisors)
    forward()
  }

  def batchUpdate(): View = {
    val advisors = entityDao.find(classOf[Advisor], getLongIds("advisor"))
    val quota = getInt("maxWriters", 8)
    advisors foreach { advisor => advisor.maxWriters = quota }
    entityDao.saveOrUpdate(advisors)
    redirect("search", "更新完成")
  }

  override protected def getQueryBuilder: OqlBuilder[Advisor] = {
    val query = super.getQueryBuilder
    getInt("depart.id") foreach { did =>
      query.where("exists(from advisor.departs as d where d.id=:departId)", did)
    }
    getBoolean("active") foreach {
      case false => query.where("advisor.endOn < :now", LocalDate.now)
      case true => query.where("advisor.endOn is null or advisor.endOn >:now", LocalDate.now)
    }
    query
  }

}
