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

import org.beangle.commons.bean.Initializing
import org.beangle.commons.logging.Logging
import org.beangle.data.dao.EntityDao
import org.beangle.ems.app.dao.AppDataSourceFactory
import org.beangle.jdbc.engine.{Engine, Engines}
import org.beangle.jdbc.query.JdbcExecutor
import org.beangle.security.Securities
import org.openurp.base.service.SemesterService
import org.openurp.base.std.model.Student
import org.openurp.code.edu.model.{CourseTakeType, ExamMode, GradingMode}
import org.openurp.degree.thesis.model.ThesisReview
import org.openurp.degree.thesis.service.ThesisGradeSyncService
import org.openurp.edu.grade.service.GradeRateService

import java.time.Instant
import javax.sql.DataSource

/** 论文成绩同步服务
 */
class ThesisGradeSyncServiceImpl extends ThesisGradeSyncService, Logging, Initializing {

  private var jdbcExecutor: JdbcExecutor = _

  private var engine: Engine = _

  var semesterService: SemesterService = _

  var datasource: DataSource = _

  var gradeRateService: GradeRateService = _

  var entityDao: EntityDao = _

  override def init(): Unit = {
    val dsf = new AppDataSourceFactory()
    var ds: DataSource = null
    try {
      dsf.name = "base"
      dsf.init()
      ds = dsf.result
    } catch {
      case exception: Exception =>
        logger.info("Using default datasource as openurp datasource")
        ds = datasource
    }
    engine = Engines.forDataSource(ds)
    jdbcExecutor = new JdbcExecutor(ds)
  }

  def sync(review: ThesisReview): Boolean = {
    val std = review.writer.std
    val semester = semesterService.get(std.project, review.writer.season.graduateOn.atDay(1))
    if (review.finalScore.isEmpty) return false
    val rs = jdbcExecutor.query("select cg.id,cg.score from edu.course_grades cg,base.courses kc where cg.course_id=kc.id" +
      " and cg.std_id=? and cg.semester_id=? and (kc.name like '毕业论文%' or kc.name like '%学位论文%')", review.writer.std.id, semester.id)
    var scoreText = review.finalScoreText.get
    if (scoreText.equals("优秀")) {
      scoreText = "优"
    } else if (scoreText.equals("良好")) {
      scoreText = "良"
    }
    if (rs.isEmpty) {
      getCourse(std) foreach { ci =>
        val gradingModeId = GradingMode.RankCn
        val score = review.finalScore.get
        val gp = gradeRateService.getConverter(std.project, new GradingMode(gradingModeId)).calcGp(Some(score)).get
        val id = genCourseGradeId()
        jdbcExecutor.update("insert into edu.course_grades(id,project_id,std_id,course_id,semester_id,course_type_id," +
          "course_take_type_id,grading_mode_id,score,score_text,gp,passed,crn,free_listening,operator,provider," +
          "status,exam_mode_id,created_at,updated_at) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", //20
          id, std.project.id, std.id, ci._1, semester.id, ci._2, CourseTakeType.Normal, gradingModeId, score,
          scoreText, gp, score >= 60, "--", false, Securities.user, review.id + "@ThesisReview", 2,
          ExamMode.Check, Instant.now, Instant.now)
        review.courseGradeSynced = true
        entityDao.saveOrUpdate(review)
      }
    } else {
      val score = review.finalScore.get
      val gradeInfo = rs.head
      if (null == gradeInfo(1) || gradeInfo(1).asInstanceOf[Number].intValue != score) {
        val gradingModeId = GradingMode.RankCn
        val gp = gradeRateService.getConverter(std.project, new GradingMode(gradingModeId)).calcGp(Some(score)).get
        jdbcExecutor.update("update edu.course_grades cg set score=?,score_text=?,gp=?,passed=?,updated_at=? where id = ?",
          score, scoreText, gp, score >= 60, Instant.now, gradeInfo(0))
      }
      review.courseGradeSynced = true
      entityDao.saveOrUpdate(review)
    }
    review.courseGradeSynced
  }

  private def genCourseGradeId(): Long = {
    if (engine.name.toLowerCase.startsWith("oracle")) {
      jdbcExecutor.query("select datetime_id() from dual").headOption.get(0).asInstanceOf[Number].longValue
    } else {
      jdbcExecutor.query("select datetime_id()").headOption.get(0).asInstanceOf[Number].longValue
    }
  }

  private def getCourse(std: Student): Option[(Long, Int)] = {
    val rs = jdbcExecutor.query(
      " select min(kc.id),min(kcz.course_type_id) from " +
        "edu.audit_plan_results jh,edu.audit_group_results kcz,edu.audit_course_results jhkc,base.courses kc"
        + " where jh.id=kcz.plan_result_id and kcz.id=jhkc.group_result_id and jhkc.course_id=kc.id"
        + " and (kc.name like '毕业论文%' or kc.name like '%学位论文%') and jh.std_id=?", std.id)
    if (rs.isEmpty) {
      None
    } else {
      val datas = rs.head
      if (datas(0) == null) None
      else Some(datas(0).asInstanceOf[Number].longValue, datas(1).asInstanceOf[Number].intValue)
    }
  }
}
