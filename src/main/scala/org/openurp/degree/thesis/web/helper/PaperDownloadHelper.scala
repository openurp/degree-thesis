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
import org.beangle.commons.concurrent.Workers
import org.beangle.commons.file.zip.Zipper
import org.beangle.commons.io.{Files, IOs}
import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging
import org.beangle.commons.net.http.HttpUtils
import org.beangle.ems.app.EmsApp
import org.openurp.base.std.model.GraduateSeason
import org.openurp.degree.thesis.model.{Writer, *}
import org.openurp.degree.thesis.service.doc.ThesisDocGenerator

import java.io.*
import scala.util.Random

object PaperDownloadHelper extends Logging {

  def zipGroupReport(season: GraduateSeason, group: DefenseGroup, reviews: Seq[ThesisReview]): File = {
    val dir = new File(System.getProperty("java.io.tmpdir") + s"season${season.id}" + Files./ + s"group${group.id}" + Files./ + "batch")
    if (dir.exists()) {
      Files.travel(dir, f => f.delete())
    }
    dir.mkdirs()
    val reviewMap = reviews.map(x => (x.writer, x)).toMap
    group.writers foreach { w =>
      val writer = w.writer
      val review = reviewMap.get(writer)
      IOs.copy(ThesisDocGenerator.genDefense(writer, review, Some(group), None), new FileOutputStream(dir.getAbsolutePath + Files./ + s"${writer.std.code}答辩记录及评分表.docx"))
    }
    val targetZip = new File(System.getProperty("java.io.tmpdir") + s"season${season.id}" + Files./ + s"group${group.id}" + Files./ + "batch.zip")
    Zipper.zip(dir, targetZip)
    if (dir.exists()) {
      Files.travel(dir, f => f.delete())
    }
    targetZip
  }

  def zipGroupPapers(season: GraduateSeason, group: DefenseGroup, papers: Iterable[ThesisPaper]): File = {
    val dir = new File(System.getProperty("java.io.tmpdir") + s"season${season.id}" + Files./ + s"group${group.id}" + Files./ + "batch")
    if (dir.exists()) {
      Files.travel(dir, f => f.delete())
    }
    dir.mkdirs()

    var paperCount = 0
    val blob = EmsApp.getBlobRepository(true)
    papers.foreach { paper =>
      blob.url(paper.filePath) foreach { url =>
        val stdName = StdNamePurifier.purify(paper.writer.std.name)
        val fileName = dir.getAbsolutePath + Files./ + paper.writer.std.code + "_" + stdName + "." + Strings.substringAfterLast(paper.filePath, ".")
        HttpUtils.download(url.openConnection(), new File(fileName))
        paperCount += 1
      }
    }
    val targetZip = new File(System.getProperty("java.io.tmpdir") + s"season${season.id}" + Files./ + s"group${group.id}" + Files./ + "batch.zip")
    Zipper.zip(dir, targetZip)
    if (dir.exists()) {
      Files.travel(dir, f => f.delete())
    }
    targetZip
  }

  def download(dir: File, papers: Iterable[ThesisPaper]): Int = {
    val blob = EmsApp.getBlobRepository(true)
    var paperCount = 0
    papers.foreach { paper =>
      if (paper.filePath.startsWith("/")) {
        blob.url(paper.filePath) foreach { url =>
          val stdName = StdNamePurifier.purify(paper.writer.std.name)
          val fileName = paper.writer.std.code + "_" + stdName + "." + Strings.substringAfterLast(paper.filePath, ".")
          val filePath = dir.getAbsolutePath + Files./ + fileName
          HttpUtils.download(url.openConnection(), new File(filePath))
          paperCount += 1
        }
      }
    }
    paperCount
  }

  def download(dir: File, checks: Iterable[ThesisCheck], naming: ThesisCheck => String): Int = {
    val blob = EmsApp.getBlobRepository(true)
    var paperCount = 0
    checks.foreach { check =>
      check.paperDoc foreach { paper =>
        if (paper.filePath.startsWith("/")) {
          blob.url(paper.filePath) foreach { url =>
            val stdName = StdNamePurifier.purify(paper.writer.std.name)
            val fileName = naming(check)
            val paperFile = new File(dir.getAbsolutePath + Files./ + fileName)
            paperFile.delete()
            HttpUtils.download(url.openConnection(), paperFile)
            paperCount += 1

            check.coverDoc foreach { cover =>
              if (cover.filePath.startsWith("/")) {
                blob.url(cover.filePath) foreach { url =>
                  val coverFile = new File(dir.getAbsolutePath + Files./ + cover.writer.std.code + ".pdf")
                  HttpUtils.download(url.openConnection(), coverFile)
                  if (coverFile.exists() && paperFile.exists()) {
                    try {
                      PDFMerger.mergeFiles(Seq(coverFile, paperFile), paperFile)
                    } catch {
                      case e: Exception => logger.error(s"merge file errors:${coverFile.getAbsolutePath} and ${paperFile.getAbsolutePath}", e)
                    } finally {
                      coverFile.delete()
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    paperCount
  }

  def downloadDocs(dir: File, docs: collection.Map[Writer, Iterable[ThesisDoc]], naming: Option[(Writer) => String] = None): collection.Seq[File] = {
    val blob = EmsApp.getBlobRepository(true)
    val zipFiles = Collections.newBuffer[File]
    docs foreach { case (w, wdocs) =>
      val stdName = StdNamePurifier.purify(w.std.name)
      val stdDocRoot = dir.getAbsolutePath + Files./ + w.std.code + "_" + Random.nextString(8)
      new File(stdDocRoot).mkdirs()
      val innerFiles = Collections.newBuffer[File]
      wdocs foreach { doc =>
        //load session before multiple thread access
        val str = (doc.writer.std.code + " " + doc.filePath + " " + doc.docType.name)
      }
      Workers.work(wdocs, (doc: ThesisDoc) => {
        blob.url(doc.filePath) foreach { url =>
          val fileName = w.std.code + "_" + stdName + s"${doc.docType.name}." + Strings.substringAfterLast(doc.filePath, ".")
          val localFile = new File(stdDocRoot + Files./ + fileName)
          HttpUtils.download(url.openConnection(), localFile)
          if (localFile.exists()) innerFiles.addOne(localFile)
        }
      }, Runtime.getRuntime.availableProcessors)
      val zipName = naming match {
        case None => w.std.code + "_" + stdName + ".zip"
        case Some(n) => n(w)
      }
      println(s"generate ${zipName}")
      val zipFile = new File(dir.getAbsolutePath + Files./ + zipName)
      Zipper.zip(new File(stdDocRoot), innerFiles, zipFile, "utf-8")
      Files.travel(new File(stdDocRoot), f => f.delete())
      new File(stdDocRoot).delete();
      zipFiles.addOne(zipFile)
    }
    zipFiles
  }
}
