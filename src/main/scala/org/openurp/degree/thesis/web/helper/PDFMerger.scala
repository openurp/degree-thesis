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

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.{PdfCopy, PdfReader}
import org.beangle.commons.io.IOs
import org.beangle.commons.logging.Logging

import java.io.*

object PDFMerger extends Logging {
  def main(args: Array[String]): Unit = {
    val cover = new File("C:\\Users\\duantihua\\Downloads\\202125010803的论文材料\\202125010803_麻敏敏论文封面.pdf")
    val paper = new File("C:\\Users\\duantihua\\Downloads\\202125010803的论文材料\\202125010803_麻敏敏论文正文.pdf")
    val target = new File("C:\\Users\\duantihua\\Downloads\\202125010803的论文材料\\202125010803.pdf")
    mergeFiles(Seq(cover, paper), target)
  }

  /** * pdf合并 * @param inputStreams 要合并的pdf的InputStream数组 * @return 合并后的pdf的二进制内容 */
  def merge(ins: Seq[InputStream], bos: OutputStream): Unit = {
    // 创建一个新的PDF
    val document = new Document()
    val copy = new PdfCopy(document, bos)
    document.open()
    ins foreach { is =>
      val bytes = IOs.readBytes(is)
      if (bytes.length > 0) {
        val reader = new PdfReader(bytes)
        copy.addDocument(reader)
        copy.freeReader(reader)
        reader.close()
      }
    }
    document.close()
    bos.close()
    copy.close()
  }

  def mergeFiles(filePaths: collection.Seq[File], target: File): Unit = {
    val ins = filePaths.flatMap { f =>
      if (f.exists()) {
        if (f.length() == 0) {
          logger.info(s"ignore empty file ${f.getAbsolutePath}")
          None
        } else if (f.getAbsolutePath.endsWith(".pdf") || f.getAbsolutePath.endsWith(".PDF")) {
          Some(new FileInputStream(f))
        } else {
          logger.info(s"illegal pdf file ${f.getAbsolutePath}")
          None
        }
      } else None
    }.toSeq
    val part = new File(target.getAbsolutePath + ".part")
    var os: OutputStream = null
    try {
      os = new FileOutputStream(part)
      merge(ins, os)
      os.close()
      if target.exists() then target.delete()
      part.renameTo(target)
    } finally {
      IOs.close(os)
      if (part.exists()) part.delete()
    }
  }
}
