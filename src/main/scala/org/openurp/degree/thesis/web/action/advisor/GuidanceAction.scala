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

import org.beangle.webmvc.view.View
import org.openurp.degree.thesis.model.*

class GuidanceAction extends AdvisorSupport {

  def index(): View = {
    val advisor = getAdvisor
    val writers = getWriters
    val guidances = entityDao.findBy(classOf[Guidance], "writer", writers)
    val guidanceMap = guidances.groupBy(x => x.writer)
    put("guidances", guidanceMap)
    put("writers", writers)
    put("withoutGuidances", writers.toBuffer.subtractAll(guidanceMap.keySet))
    forward()
  }

}
