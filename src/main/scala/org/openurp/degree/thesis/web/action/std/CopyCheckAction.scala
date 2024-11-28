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

package org.openurp.degree.thesis.web.action.std

import org.beangle.data.dao.OqlBuilder
import org.beangle.webmvc.view.{Status, Stream, View}
import org.openurp.degree.thesis.model.CopyCheck

import java.io.File

class CopyCheckAction extends WriterSupport {

  def index(): View = {
    val writer = getWriter
    val query = OqlBuilder.from(classOf[CopyCheck], "c")
    query.where("c.writer= :writer", writer)
    val checks = entityDao.search(query)
    put("writer", writer)
    put("checks", checks)
    forward()
  }

  def view(): View = {
    forward()
  }

  def report(): View = {
    val writer = getWriter
    val query = OqlBuilder.from(classOf[CopyCheck], "c")
    query.where("c.writer= :writer", writer)
    entityDao.search(query).headOption match {
      case Some(check) =>
        check.report match {
          case Some(r) => Stream(new File(r))
          case None => Status.NotFound
        }
      case None => Status.NotFound
    }
  }
}
