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

import org.beangle.commons.bean.Initializing
import org.beangle.data.dao.OqlBuilder
import org.beangle.jdbc.query.JdbcExecutor
import org.beangle.ems.app.dao.AppDataSourceFactory
import org.beangle.security.authc.Profile
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.{ExportSupport, RestfulAction}
import org.openurp.base.edu.model.TeachingOffice
import org.openurp.base.hr.model.Teacher
import org.openurp.base.model.{Department, Project}
import org.openurp.degree.thesis.model.Advisor
import org.openurp.degree.thesis.service.DefenseGroupService
import org.openurp.starter.web.helper.EmsCookieHelper
import org.openurp.starter.web.support.ProjectSupport

import java.time.LocalDate

class AdvisorAction extends RestfulAction[Advisor], ExportSupport[Advisor], ProjectSupport, Initializing {
  var defenseGroupService: DefenseGroupService = _

  var baseJdbcExecutor: JdbcExecutor = _

  var defaultmMaxWriters: Int = 8

  override def init(): Unit = {
    val ds = new AppDataSourceFactory()
    ds.name = "base"
    ds.init()
    baseJdbcExecutor = new JdbcExecutor(ds.result)
  }

  def disable(): View = {
    val advisors = entityDao.find(classOf[Advisor], getLongIds("advisor"))
    val yestoday = LocalDate.now().minusDays(1)
    advisors foreach { advisor =>
      advisor.endOn = Some(yestoday)
    }
    entityDao.saveOrUpdate(advisors)
    redirect("search", "禁用成功")
  }

  override protected def indexSetting(): Unit = {
    given project: Project = getProject

    val departs = getDeparts
    put("departs", departs)
    put("offices", entityDao.findBy(classOf[TeachingOffice], "department", departs))
    super.indexSetting()
  }

  override protected def getQueryBuilder: OqlBuilder[Advisor] = {
    val query = super.getQueryBuilder
    getInt("depart.id") foreach { did =>
      query.where("exists(from advisor.departs as d where d.id=:departId)", did)
    }
    getBoolean("active") foreach {
      case false => query.where("advisor.endOn < :now", LocalDate.now)
      case true => query.where("advisor.endOn is null or advisor.endOn >= :now", LocalDate.now)
    }
    query
  }

  override protected def saveAndRedirect(advisor: Advisor): View = {
    val departs = entityDao.find(classOf[Department], getIntIds("depart"))
    advisor.departs.subtractAll(advisor.departs.filterNot(departs.contains))
    advisor.departs ++= departs.filterNot(advisor.departs.contains)
    if (!advisor.persisted) {
      advisor.teacher = entityDao.get(classOf[Teacher], advisor.teacher.id) //reload teacher
    }
    if (!advisor.persisted) {
      advisor.maxWriters = defaultmMaxWriters
    }
    val teacher = advisor.teacher
    getLong("office.id") match
      case None => teacher.office = None
      case Some(officeId) =>
        val office = entityDao.get(classOf[TeachingOffice], officeId)
        teacher.office = Some(office)
        getBoolean("isDirector", false) match
          case true => office.director = Some(teacher)
          case false => if (office.director.contains(teacher)) office.director = None

    if (advisor.persisted) {
      updateTeacher(advisor.teacher)
      super.saveAndRedirect(advisor)
    } else {
      val query = OqlBuilder.from(classOf[Advisor], "ad")
      query.where("ad.teacher=:teacher", advisor.teacher)
      if (entityDao.search(query).isEmpty) {
        updateTeacher(advisor.teacher)
        super.saveAndRedirect(advisor)
      } else {
        redirect("search", "该教师已经存在于名单中")
      }
    }
  }

  private def updateTeacher(teacher: Teacher): Unit = {
    entityDao.saveOrUpdate(teacher)
    teacher.office match {
      case None =>
        baseJdbcExecutor.update(s"update base.teachers set office_id = null where id=${teacher.id}")
      case Some(o) =>
        entityDao.saveOrUpdate(o)
        baseJdbcExecutor.update(s"update base.teachers set office_id=${o.id} where id=${teacher.id}")
        o.director foreach { d =>
          baseJdbcExecutor.update(s"update base.teaching_offices set director_id=${d.id} where id=${o.id}")
        }
    }
  }

  override protected def editSetting(advisor: Advisor): Unit = {
    given project: Project = getProject

    put("offices", entityDao.findBy(classOf[TeachingOffice], "department", getDeparts))
    var isDirector = false
    if (null != advisor.teacher) {
      advisor.teacher.office foreach { o =>
        isDirector = o.director.contains(advisor.teacher)
      }
    }
    put("isDirector", isDirector)
    put("departs", project.departments)
    put("project", project)
    if (!advisor.persisted) {
      val query = OqlBuilder.from(classOf[Teacher], "t")
      query.where("not exists(from " + classOf[Advisor].getName + " a where a.teacher=t)")
      query.orderBy("t.department.id,t.staff.code")
      val teachers = entityDao.search(query)
      put("teachers", teachers)
    }
    new EmsCookieHelper(entityDao).getProfile(request, response) foreach { p =>
      put("isAdmin", p.getProperty("department").contains(Profile.AllValue))
    }
    if (!advisor.persisted) {
      advisor.maxWriters = defaultmMaxWriters
      advisor.beginOn = LocalDate.now
    }
    super.editSetting(advisor)
  }

}
