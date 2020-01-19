package controllers

import java.io.File
import java.nio.file.Files

import dao._
import javax.inject.Inject
import models.Tables._
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import org.zeroturnaround.zip.ZipUtil
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import utils.Utils

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
  * Created by yz on 2018/4/16
  */
class LoadDataController @Inject()(sampleDao: SampleDao, tool: Tool) extends Controller {

  def loadDataBefore() = Action { implicit request =>
    Ok(views.html.user.loadData())
  }

  case class SampleNameData(sampleName: String)

  val sampleNameForm = Form(
    mapping(
      "sampleName" -> text
    )(SampleNameData.apply)(SampleNameData.unapply)
  )

  def sampleNameCheck = Action.async { implicit request =>
    val data = sampleNameForm.bindFromRequest.get
    val userId = tool.getUserId
    sampleDao.selectBySampleName(userId, data.sampleName).map {
      case Some(y) => Ok(Json.obj("valid" -> false))
      case None => Ok(Json.obj("valid" -> true))
    }
  }


  def loadData = Action.async(parse.multipartFormData) { implicit request =>
    val data = sampleNameForm.bindFromRequest.get
    val describeData = tool.describeForm.bindFromRequest.get
    val tmpDataFile = request.body.file("file").get
    val tmpFile = Files.createTempFile("data", ".fa").toFile
    tmpDataFile.ref.moveTo(tmpFile, true)
    val b = Utils.isFastaFile(tmpFile)
    if (!b) {
      tmpFile.delete()
      Future.successful(Ok(Json.obj("valid" -> "false", "message" -> "请选择一个合法的fasta文件!")))
    } else {
      val attr = Utils.getFastaAttr(tmpFile)
      val userId = tool.getUserId
      val row = SampleRow(0, userId, data.sampleName, describeData.describe, attr.maxLength, attr.minLength, attr.meanLength, attr.size,
        attr.kind, new DateTime())
      sampleDao.insert(row).flatMap(_ => sampleDao.selectBySampleName(userId, data.sampleName).map(_.get)).map { sample =>
        val userIdDir = new File(Utils.userPath, userId.toString)
        Utils.createDirectoryWhenNoExist(userIdDir)
        val dataFile = new File(userIdDir, "data")
        Utils.createDirectoryWhenNoExist(dataFile)
        val missionFile = new File(userIdDir, "mission")
        Utils.createDirectoryWhenNoExist(missionFile)

        val file = tool.getSampleFileById(sample.id)
        FileUtils.copyFile(tmpFile, file)
        Ok("success")
      }
    }
  }


  def splitDataBefore() = Action { implicit request =>
    Ok(views.html.user.splitData())
  }

  case class SplitData(seqNum: Int)

  val splitForm = Form(
    mapping(
      "seqNum" -> number
    )(SplitData.apply)(SplitData.unapply)
  )

  def splitData = Action(parse.multipartFormData) { implicit request =>
    val data = splitForm.bindFromRequest.get
    val tmpDataFile = request.body.file("file").get
    val tmpDir = Files.createTempDirectory("tmpDir").toFile
    val tmpFile = new File(tmpDir, "seq.fa")
    tmpDataFile.ref.moveTo(tmpFile, true)
    val b = Utils.isFastaFile(tmpFile)
    if (!b) {
      Utils.deleteDirectory(tmpDir)
      Ok(Json.obj("valid" -> "false", "message" -> "请选择一个合法的fasta文件!"))
    } else {
      val lines = FileUtils.readLines(tmpFile).asScala
      val map = mutable.LinkedHashMap[String, String]()
      var key = ""
      lines.zipWithIndex.foreach { case (line, i) =>
        if (line.startsWith(">")) {
          key = line
          map += (key -> "")
        } else if (key != "") {
          map(key) += line
        }
      }
      val resultDir = new File(tmpDir, "result")
      map.grouped(data.seqNum).zipWithIndex.foreach { case (map, i) =>
        val buffer = map.flatMap { case (key, value) =>
          ArrayBuffer(key, value)
        }.toBuffer
        FileUtils.writeLines(new File(resultDir, s"part${i}.fa"), buffer.asJava)
      }
      val outFile = new File(tmpDir, "result.zip")
      ZipUtil.pack(resultDir, outFile)
      val base64 = Utils.getBase64Str(outFile)
      Utils.deleteDirectory(tmpDir)
      Ok(Json.toJson(base64))
    }
  }


}
