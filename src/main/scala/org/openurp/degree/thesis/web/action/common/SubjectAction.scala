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

package org.openurp.degree.thesis.web.action.common

import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.EntityAction
import org.openurp.degree.thesis.model.{Proposal, Subject, SubjectApply}

class SubjectAction extends ActionSupport with EntityAction[Subject] {
  var entityDao: EntityDao = _

  @mapping(value = "{id}")
  def info(@param("id") id: String): View = {
    put("subject", entityDao.get(classOf[Subject], id.toLong))
    forward()
  }
}
