package controllers

import java.io.File

import dao._
import javax.inject.Inject
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.RequestHeader
import utils.Utils

/**
  * Created by yz on 2018/4/18
  */
class Tool @Inject()(sampleDao: SampleDao) {

  def getUserIdPath(implicit request: RequestHeader) = {
    val userId = getUserId
    new File(Utils.userPath, userId.toString)
  }

  def getRequestDomain(implicit request: RequestHeader)={
    request.domain
  }

  def getUserDataPath(implicit request: RequestHeader) = {
    val userIdFile = getUserIdPath
    new File(userIdFile, "data")
  }

  def getUserMissionPath(implicit request: RequestHeader) = {
    val userIdFile = getUserIdPath
    new File(userIdFile, "mission")
  }

  def getSampleFileById(sampleId: Int)(implicit request: RequestHeader) = {
    val userDataFile = getUserDataPath
    new File(userDataFile, s"${sampleId}.fa")
  }

  def getMissionIdDirById(missionId: Int)(implicit request: RequestHeader) = {
    val userMissionFile = getUserMissionPath
    new File(userMissionFile, missionId.toString)
  }

  def getResultDirById(missionId: Int)(implicit request: RequestHeader) = {
    val missionIdDir = getMissionIdDirById(missionId)
    new File(missionIdDir, "result")
  }

  def getSampleId(sampleName: String)(implicit request: RequestHeader) = {
    val userId = getUserId
    val sample = Utils.execFuture(sampleDao.selectBySampleName(userId, sampleName)).get
    sample.id
  }

  def getSampleFileByName(sampleName: String)(implicit request: RequestHeader) = {
    val sampleId = getSampleId(sampleName)
    getSampleFileById(sampleId)
  }

  case class DescribeData(describe: String)

  val describeForm = Form(
    mapping(
      "describe" -> text
    )(DescribeData.apply)(DescribeData.unapply)
  )

  def getUserId(implicit request: RequestHeader) = {
    request.session.get("id").get.toInt
  }

  def getUserIdDir(userId:Int) = {
    new File(Utils.userPath, userId.toString)
  }

}
