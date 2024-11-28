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

import com.itextpdf.text.pdf.{PdfReader, PdfStamper}
import org.openurp.base.model.AuditStatus
import org.openurp.degree.thesis.model.Proposal

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}

object ThesisPdfGenerator extends AbstractFileGenerator {

  def genProposal(proposal: Proposal): InputStream = {
    val bytes = getTemplate("proposal.pdf") match
      case None => Array.empty[Byte]
      case Some(url) =>
        val data = extract(proposal.writer)
        data.put("proposal_meanings", proposal.meanings)
        data.put("proposal_conditions", proposal.conditions)
        data.put("proposal_outline", proposal.outline)
        data.put("proposal_references", proposal.references)
        data.put("proposal_methods", proposal.methods)
        if (proposal.status == AuditStatus.Passed) {
          addDate("date", proposal.confirmAt, data)
          proposal.advisorOpinion match {
            case Some(o) => data.put("advisor_opinion", o)
            case None => data.put("advisor_opinion", "审查通过")
          }
        } else {
          addDate("date", None, data)
          data.put("advisor_opinion", "")
        }
        val reader = new PdfReader(url.openStream())
        val bos = new ByteArrayOutputStream()
        val ps = new PdfStamper(reader, bos)

        val af = ps.getAcroFields

        data.get("thesis_title1") foreach { t => af.setField("题目1", t) }
        data.get("thesis_title2") foreach { t => af.setField("题目2", t) }
        af.setField("选题名称", data("thesis_title"))
        af.setField("学院", data("department_name"))
        af.setField("专业", data("major_name"))
        af.setField("班级", data("squad_name"))
        af.setField("姓名", data("std_name"))
        af.setField("学号", data("std_code"))

        af.setField("指导老师", data("advisor_name"))
        af.setField("目的及意义", data("proposal_meanings"))
        af.setField("研究现状", data("proposal_conditions"))
        af.setField("论文提纲", data("proposal_outline"))
        af.setField("参考文献", data("proposal_references"))
        af.setField("研究方法", data("proposal_methods"))
        af.setField("教师意见", data("advisor_opinion"))
        ps.close()
        bos.toByteArray

    new ByteArrayInputStream(bytes)
  }

}
