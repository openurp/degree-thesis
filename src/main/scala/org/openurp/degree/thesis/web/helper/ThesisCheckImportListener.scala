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

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.doc.transfer.importer.{ImportListener, ImportResult}
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{ThesisCheck, ThesisPaper, Writer}
import org.openurp.degree.thesis.service.ThesisCheckService

import java.time.Instant

class ThesisCheckImportListener(season: GraduateSeason, entityDao: EntityDao, thesisCheckService: ThesisCheckService) extends ImportListener {
  override def onItemStart(tr: ImportResult): Unit = {
    transfer.curData.get("writer.std.code") foreach { code =>
      thesisCheckService.findWriter(code.toString) match {
        case None => tr.addFailure(s"找不到学生的毕业论文信息", code)
        case Some(w) =>
          val query = OqlBuilder.from(classOf[ThesisCheck], "c")
          query.where("c.writer=:writer and c.season=:season", w, season)
          query.where("c.degreeMajorCode=:degreeMajorCode", transfer.curData.get("thesisCheck.degreeMajorCode").get.toString)
          val check = entityDao.search(query).headOption match
            case None =>
              val c = new ThesisCheck
              c.writer = w
              c.season = season
              c.title = "--"
              c.updatedAt = Instant.now
              c
            case Some(tp) => tp
          transfer.current = check
      }
    }
  }

  override def onItemFinish(tr: ImportResult): Unit = {
    val c = transfer.current.asInstanceOf[ThesisCheck]
    if (c.departNo.isEmpty) {
      tr.addFailure(s"名单不存在,需要事先导入", transfer.curData.getOrElse("writer.std.code", "--"))
    } else {
      if (c.writer.persisted) {
        entityDao.saveOrUpdate(c)
        thesisCheckService.updateDoc(c)
        val paper = entityDao.findBy(classOf[ThesisPaper], "writer", c.writer).headOption

        //first fetch paper info
        if (c.title == "--") {
          paper foreach { pd =>
            c.title = pd.title
            c.language = pd.language
            c.keywords = pd.keywords
            c.researchField = pd.researchField
            c.advisor = Some(c.writer.advisor.map(_.name).getOrElse("--"))
            entityDao.saveOrUpdate(c)
          }
        } else {
          //update paper info when necessary
          paper foreach { pd =>
            if (c.title != "--") {
              c.writer.thesisTitle = Some(c.title)
              c.writer.researchField = c.researchField
              pd.title = c.title
            }
            if (c.language.nonEmpty) pd.language = c.language
            if (c.keywords.nonEmpty) pd.keywords = c.keywords
            if (c.researchField.nonEmpty) pd.researchField = c.researchField
            entityDao.saveOrUpdate(pd)
          }
        }
      }
    }
  }
}
