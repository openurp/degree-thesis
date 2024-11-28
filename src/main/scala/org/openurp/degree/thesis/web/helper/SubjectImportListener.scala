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

import org.beangle.commons.lang.Strings
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.doc.transfer.importer.{ImportListener, ImportResult}
import org.openurp.base.edu.model.Major
import org.openurp.base.model.AuditStatus
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{Advisor, Subject}

class SubjectImportListener(season: GraduateSeason, entityDao: EntityDao) extends ImportListener {
  override def onItemStart(tr: ImportResult): Unit = {
    tr.transfer.curData.get("subject.name") foreach { name =>
      entityDao.findBy(classOf[Subject], "name" -> name, "season" -> season) foreach { s =>
        tr.transfer.current = s
      }
    }
  }

  override def onItemFinish(tr: ImportResult): Unit = {
    val subject = tr.transfer.current.asInstanceOf[Subject]
    subject.season = season

    val advisorCodeOrName = tr.transfer.curData.getOrElse("advisorCodeOrName", "")
    val query = OqlBuilder.from(classOf[Advisor], "advisor")
    query.where("advisor.teacher.staff.code =:code or advisor.teacher.name=:name", advisorCodeOrName, advisorCodeOrName)
    val advisors = entityDao.search(query)

    var majorCodeOrName = tr.transfer.curData.getOrElse("major.code", "").asInstanceOf[String]
    if (majorCodeOrName.contains(" ")) {
      majorCodeOrName = Strings.substringBefore(majorCodeOrName, " ")
    }
    val mq = OqlBuilder.from(classOf[Major], "major")
    mq.where("major.project=:project", season.project)
    mq.where("major.code =:code or major.name=:name", majorCodeOrName, majorCodeOrName)
    val majors = entityDao.search(mq)

    if (advisors.nonEmpty && majors.nonEmpty) {
      subject.advisor = advisors.head
      subject.majors.addOne(majors.head)
      subject.status = AuditStatus.Passed
      entityDao.saveOrUpdate(subject)
    } else {
      if advisors.isEmpty then
        tr.addFailure("找不到指导老师", advisorCodeOrName)
      else
        tr.addFailure("找不到专业", majorCodeOrName)
    }
  }
}
