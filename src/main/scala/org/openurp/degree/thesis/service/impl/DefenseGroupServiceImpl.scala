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

import org.beangle.commons.collection.Collections
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.openurp.base.hr.model.Teacher
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{Advisor, DefenseGroup, ThesisReview}
import org.openurp.degree.thesis.service.DefenseGroupService

class DefenseGroupServiceImpl extends DefenseGroupService {

  var entityDao: EntityDao = _

  override def getGroups(me: Teacher, season: GraduateSeason): Seq[DefenseGroup] = {
    val query = OqlBuilder.from(classOf[DefenseGroup], "dg")
    query.where("dg.season=:season", season)
    query.where("exists(from dg.members as m where m.teacher=:me) or dg.secretary.code=:secretary", me, me.code)
    entityDao.search(query)
  }

  /** 是否答辩组管理员(目前按照教研室管理）
   * FIXME 没有实现教研室主任的查询
   *
   * @param group
   * @param advisor
   * @return
   */
  override def isGroupAdmin(group: DefenseGroup, advisor: Advisor): Boolean = {
    advisor.teacher.office match
      case None => false
      case Some(o) => group.office.contains(o)
  }

  /** 是否能写组内公告
   *
   * @param group
   * @param me
   * @return
   */
  override def isGroupManager(group: DefenseGroup, me: Teacher): Boolean = {
    if (group.members.exists(x => x.teacher == me && x.leader)) {
      true
    } else if (group.secretary.contains(me)) {
      true
    } else false
  }

  override def isOfficeHead(advisor: Advisor): Boolean = {
    advisor.teacher.office match
      case None => false
      case Some(o) =>
        o.director match
          case None => false
          case Some(d) => d.code == advisor.teacher.code
  }

  override def getManageTeacher(advisor: Advisor, season: GraduateSeason): Iterable[Teacher] = {
    val query = OqlBuilder.from[Teacher](classOf[ThesisReview].getName, "review")
    query.where("review.writer.season=:season", season)
    query.where("review.crossReviewManager=:me", advisor.teacher)
    query.where("review.writer.advisor is not null")
    query.select("distinct review.writer.advisor.teacher")
    val t1 = entityDao.search(query)

    val query2 = OqlBuilder.from[Teacher](classOf[Advisor].getName, "advisor")
    query2.where("advisor.teacher.office.director=:me", advisor.teacher)
    query2.where("advisor.endOn is null")
    query2.select("advisor.teacher")
    val t2 = entityDao.search(query2)

    val rs = Collections.newSet[Teacher]
    rs ++= t1
    rs ++= t2
    rs.toSeq
  }
}
