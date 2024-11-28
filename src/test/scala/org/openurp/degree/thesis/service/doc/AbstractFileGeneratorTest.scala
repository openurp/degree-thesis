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

package org.openurp.degree.thesis.service.doc

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class AbstractFileGeneratorTest extends AnyFunSpec with Matchers {
  describe("Generator") {
    val titles = AbstractFileGenerator.splitTitle("abcdefghijklmnopqrstuvwxyz", 24)
    assert(titles._1 == "abcdefghijklmnopqrstuvw-")
    assert(titles._2 == "xyz")

    val titles2 = AbstractFileGenerator.splitTitle("abcdefghijklmnopqrstuvw xyz", 24)
    assert(titles2._1 == "abcdefghijklmnopqrstuvw")
    assert(titles2._2 == "xyz")

    val titles3 = AbstractFileGenerator.splitTitle("关于高性能defgh集成电路klmnopqrstuvw的实现", 24)
    assert(titles3._1 == "关于高性能defgh集成电路k")
    assert(titles3._2 == "lmnopqrstuvw的实现")

    val titles4 = AbstractFileGenerator.splitTitle("关于高性能defgh集成电路的实现", 24)
    assert(titles4._1 == "关于高性能defgh集成电路")
    assert(titles4._2 == "的实现")

    println(AbstractFileGenerator.splitTitle("The Theme of Justice in Agatha Christie’ s Detective Novel—A Case Study of The Witness for the Prosecution and And Then There Were None",24))
  }

}
