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

import org.beangle.commons.collection.Collections
import org.openurp.base.hr.model.Teacher
import org.openurp.degree.thesis.model.Writer

import scala.collection.mutable
import scala.util.Random

object RandomGroupHelper {

  def generate(writers: collection.Seq[Writer], teachers: collection.Seq[Teacher], count: Int): Seq[RandomGroup] = {
    val groups = Collections.newBuffer[RandomGroup]
    val remindTeacherNum = teachers.size % count
    val remindWriterNum = teachers.size % count

    (0 until count) foreach { i =>
      val teacherCnt = teachers.size / count + (if i < remindTeacherNum then 1 else 0)
      val writerCnt = writers.size / count + (if i < remindWriterNum then 1 else 0)
      groups.addOne(new RandomGroup(teacherCnt, writerCnt))
    }
    val teacherWriters = writers.groupBy(_.advisor.get.teacher).map(x => (x._1, x._2.toBuffer))
    val writerSet = Collections.newSet[Writer] ++= writers
    val teacherIter = Random.shuffle(teachers).iterator
    groups.foreach { group =>
      (0 until group.teacherCnt) foreach { i =>
        val teacher = teacherIter.next()
        group.teachers.addOne(teacher)
      }
      addWriters(group, writerSet, teacherWriters)
    }

    writerSet foreach { writer =>
      val matchedGroups = groups.filter(g => !g.teachers.contains(writer.advisor.get.teacher))
      matchedGroups.sortBy(_.writers.size).headOption foreach { matchedGroup =>
        matchedGroup.writers.addOne(writer)
      }
    }
    groups.toSeq
  }

  private def addWriters(group: RandomGroup, writers: mutable.Set[Writer], teacherWriters: Map[Teacher, mutable.Buffer[Writer]]): Unit = {
    teacherWriters foreach { case (teacher, tws) =>
      val freeCnt = group.freeCnt
      if (tws.nonEmpty && freeCnt > 0 && !group.teachers.contains(teacher)) {
        if (freeCnt >= tws.size) {
          group.writers ++= tws
          writers --= tws
          tws.clear()
        } else {
          val leftN = tws.take(freeCnt)
          group.writers ++= leftN
          writers --= leftN
          tws --= leftN
        }
      }
    }
  }
}

class RandomGroup(val teacherCnt: Int, val writerCnt: Int) {
  val writers = Collections.newBuffer[Writer]
  val teachers = Collections.newSet[Teacher]

  def freeCnt = writerCnt - writers.size

  def orderedWriters: mutable.Buffer[Writer] = {
    writers.toBuffer.sortBy(x => x.advisor.get.code + "_" + x.code)
  }
}
