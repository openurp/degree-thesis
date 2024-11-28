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

import org.beangle.data.dao.EntityDao
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.action.EntityAction
import org.openurp.degree.thesis.model.{Advisor, DefenseNotice}

class DefenseNoticeAction extends ActionSupport with EntityAction[DefenseNotice] {
  var entityDao: EntityDao = _

  @mapping(value = "{id}")
  def info(@param("id") id: String): View = {
    put("advisor", entityDao.get(classOf[Advisor], id.toLong))
    forward()
  }

  //  @Transactional
  //  @RequestMapping(value = { "/dbztz/view" }, method = {
  //    org.springframework.web.bind.annotation.RequestMethod.GET })
  //  @Access({ enums.YHLX.学生, enums.YHLX.教师 })
  //  public ModelAndView view(HttpSession session, Long id) {
  //    ModelAndView mv = new ModelAndView();
  //
  //    DBZTZ dbztz = (DBZTZ) getFirst("from DBZTZ where id=?", new Object[] { id });
  //    dbztz.setCkcs(Long.valueOf(dbztz.getCkcs().longValue() + 1L));
  //    update(dbztz);
  //    mv.addObject("dbztz", dbztz);
  //    mv.setViewName("/dbztz/view");
  //    return mv;
  //  }

}
