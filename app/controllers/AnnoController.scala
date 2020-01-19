package controllers

import java.io.File
import java.nio.file.Files

import akka.actor._
import akka.stream.Materializer
import dao._
import javax.inject.Inject
import models.Tables._
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import org.zeroturnaround.zip.ZipUtil
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import utils.Utils

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.JavaConverters._

/**
  * Created by yz on 2018/4/18
  */
case class FileData(missionId: Int, kegg: Option[String], keggFileNames: Seq[String], cog: Option[String], cogFileNames: Seq[String],
                    nr: Option[String], nrFileNames: Seq[String], go: Option[String], goFileNames: Seq[String],
                    swiss: Option[String], swissFileNames: Seq[String]
                   )

class AnnoController @Inject()(tool: Tool, sampleDao: SampleDao, missionDao: MissionDao)(implicit val system: ActorSystem,
                                                                                         implicit val materializer: Materializer) extends Controller {

  def annoBefore = Action { implicit request =>
    Ok(views.html.user.anno())
  }

  case class KeggData(missionName: String, sampleName: String, dbType: String,
                      annoMethods: Seq[String],
                      eValue: String, maxTargetSeqs: String,
                      eggEValue: String, eggMaxTargetSeqs: String, nrEValue: String, nrMaxTargetSeqs: String,
                      swissEValue: String, swissMaxTargetSeqs: String
                     )

  val keggForm = Form(
    mapping(
      "missionName" -> text,
      "sampleName" -> text,
      "dbType" -> text,
      "annoMethods" -> seq(text),
      "eValue" -> text,
      "maxTargetSeqs" -> text,
      "eggEValue" -> text,
      "eggMaxTargetSeqs" -> text,
      "nrEValue" -> text,
      "nrMaxTargetSeqs" -> text,
      "swissEValue" -> text,
      "swissMaxTargetSeqs" -> text
    )(KeggData.apply)(KeggData.unapply)
  )

  def anno = Action { implicit request =>
    val data = keggForm.bindFromRequest().get
    val dataFile = tool.getSampleFileByName(data.sampleName)
    val userId = tool.getUserId
    val row = MissionRow(0, data.missionName, userId, data.sampleName, new DateTime(), None, "running")
    val f = missionDao.insert(row).flatMap(_ => missionDao.selectByMissionName(userId, data.missionName)).flatMap { mission =>
      val missionId = mission.id
      val missionIdDir = new File(tool.getUserMissionPath, missionId.toString)
      Utils.createDirectoryWhenNoExist(missionIdDir)
      val workspaceDir = new File(missionIdDir, "workspace")
      Utils.createDirectoryWhenNoExist(workspaceDir)
      val resultDir = new File(missionIdDir, "result")
      Utils.createDirectoryWhenNoExist(resultDir)
      val sample = Utils.execFuture(sampleDao.selectBySampleName(userId, data.sampleName)).get
      val blastFile = if (sample.kind == "蛋白") {
        Utils.blastpFile
      } else {
        Utils.blastxFile
      }
      val task = if (sample.kind == "蛋白") "blastp-fast" else "blastx-fast"
      val commands = ArrayBuffer[String]()
      val threadNum = 4
      val mergeCommands = ArrayBuffer(s"${Utils.anno}/anno_st/mergeAnnotation.pl -fa ${dataFile}")
      val keggDir = new File(resultDir, "kegg")
      if (data.annoMethods.contains("kegg")) {
        Utils.createDirectoryWhenNoExist(keggDir)
        val keggDb = if (data.dbType != "all" && data.dbType != "Viruses") {
          new File(Utils.keggDir, s"${data.dbType}.pep.fasta")
        } else new File(Utils.keggDir, s"ko.pep.fasta")
        val keggCommand =
          s"""
             |${blastFile} -db ${keggDb} -query ${dataFile} -out ko.txt -task ${task} -evalue ${data.eValue} -outfmt 6 -num_threads ${threadNum} -max_target_seqs ${data.maxTargetSeqs}
             |python ${Utils.anno}/kobas2.0/scripts/annotate.py -i ko.txt -t blastout:tab -s ko -o ${new File(keggDir, "pathway.txt")}
           """.stripMargin
        commands += keggCommand
        mergeCommands += s"-kobas ${new File(keggDir, "pathway.txt")}"
      }
      if (data.annoMethods.contains("cog")) {
        val eggNOGDir = new File(resultDir, "eggNOG")
        Utils.createDirectoryWhenNoExist(eggNOGDir)
        val eggNOGCommand =
          s"""
             |${blastFile} -db ${Utils.eggNOGDb} -query ${dataFile} -out egg.txt -task ${task} -evalue ${data.eggEValue} -outfmt 6 -num_threads ${threadNum} -max_target_seqs ${data.eggMaxTargetSeqs}
             |perl ${Utils.anno}/eggNOG/4-2.eggNOG_anno.pl -i egg.txt -o ${new File(eggNOGDir, "COG.anno.xls")} -m ${Utils.anno}/eggNOG
             |perl ${Utils.anno}/eggNOG/4-3.cog_profile.pl ${new File(eggNOGDir, "COG.anno.xls")}  ${Utils.anno}/eggNOG/eggnogv4.funccats.txt ${new File(eggNOGDir, "COG.summary.xls")}
             |perl ${Utils.anno}/eggNOG/cog-bar.pl -i pic_COG -o  ${new File(eggNOGDir, "COG.bar.pdf")}
           """.stripMargin
        commands += eggNOGCommand
        mergeCommands += s"-cog ${new File(eggNOGDir, "COG.anno.xls")}"
      }
      val nrDir = new File(resultDir, "NR")
      if (data.annoMethods.contains("nr")) {
        Utils.createDirectoryWhenNoExist(nrDir)
        val trMap = Map("Bacteria" -> "d__Bacteria.fasta",
          "Animals" -> "d__Eukaryota__k__Metazoa.fasta",
          "Plants" -> "d__Eukaryota__k__Viridiplantae.fasta",
          "Fungi" -> "d__Eukaryota__k__Fungi.fasta",
          "Archaea" -> "d__Archaea.fasta",
          "Viruses" -> "d__Viruses.fasta")
        val nrDb = if (data.dbType == "all") {
          new File(Utils.dbDir, "nr/nr")
        } else {
          new File(Utils.dbDir, s"split_nr/${trMap(data.dbType)}")
        }
        val nrCommand =
          s"""
             |${blastFile} -db ${nrDb} -query ${dataFile} -out  nr.xml -task ${task} -evalue ${data.nrEValue} -outfmt 5 -num_threads ${threadNum} -max_target_seqs ${data.nrMaxTargetSeqs}
             |${Utils.blast2Table} -format 10 -xml nr.xml -header -top >${new File(nrDir, "nr.table.xls")}
           """.stripMargin
        commands += nrCommand
        mergeCommands += s"-nr ${new File(nrDir, "nr.table.xls")}"
      }
      if (data.annoMethods.contains("go")) {
        val goDir = new File(resultDir, "GO")
        Utils.createDirectoryWhenNoExist(goDir)
        val goCommand =
          s"""
             |cd ${Utils.b2gDir}
             |java -Xms128m -Xmx128000m -cp *:ext/*: es.blast2go.prog.B2GAnnotPipe -in ${new File(workspaceDir, "nr.xml")} -fas ${dataFile} -out ${new File(workspaceDir, "go")} -prop b2gPipe.properties -v -annot -wiki html_template.html
             |cd ${workspaceDir}
             |perl ${Utils.anno}/anno_st/mergeGO.pl go.annot >${new File(goDir, "GO.list")}
             |${Utils.anno}/anno_st/gene-ontology.pl -i ${new File(goDir, "GO.list")} -l 2 -list GO.level2.list >${new File(goDir, "level2.go.txt")}
             |${Utils.anno}/anno_st/go-bar.pl -i ${new File(goDir, "level2.go.txt")}
             |cp level2.go.txt.pdf ${new File(goDir, "level2.go.txt.pdf")}
           """.stripMargin
        commands += goCommand
        mergeCommands += s"-go ${new File(goDir, "GO.list")}"
      }
      if (data.annoMethods.contains("swiss")) {
        val swissDir = new File(resultDir, "Swissprot")
        Utils.createDirectoryWhenNoExist(swissDir)
        val swissCommand =
          s"""
             |${blastFile} -db ${Utils.swissDb} -query ${dataFile} -out  swiss.xml -task ${task} -evalue ${data.swissEValue} -outfmt 5 -num_threads ${threadNum} -max_target_seqs ${data.swissMaxTargetSeqs}
             |${Utils.blast2Table} -format 10 -xml swiss.xml -header -top >${new File(swissDir, "swiss.table.xls")}
           """.stripMargin
        commands += swissCommand
        mergeCommands += s"-swissport ${new File(swissDir, "swiss.table.xls")}"
      }
      mergeCommands += s">${new File(resultDir, "annotation.table.xls")}"
      commands += mergeCommands.mkString(" ")
      val logFile = new File(missionIdDir, "log.txt")
      val execCommand = Utils.callScript(workspaceDir, shBuffer = commands)
      val updateMission = if (execCommand.isSuccess) {
        if (data.annoMethods.contains("kegg")) {
          val keggFile=new File(keggDir,"pathway.txt")
          val cleanFile=new File(keggDir,"pathway.clean.txt")
          val lines = FileUtils.readLines(keggFile).asScala
          val newLines = lines.filter(_.split("\t").size == 2).filter(_.contains("http")).map { line =>
            val lines = line.split("\t")
            lines(1) = lines(1).takeWhile(_ != '|')
            lines.mkString("\t")
          }
          FileUtils.writeLines(cleanFile,newLines.asJava)
        }
        FileUtils.writeStringToFile(logFile, "Run successed!", "UTF-8")
        mission.copy(state = "success", endtime = Some(new DateTime()))
      } else {
        FileUtils.writeStringToFile(logFile, execCommand.getErrStr, "UTF-8")
        mission.copy(state = "error", endtime = Some(new DateTime()))
      }
      missionDao.update(updateMission)
    }
    f
    Ok(Json.toJson("success"))
  }

  def annoResult = Action { implicit request =>
    Ok(views.html.user.annoResult())
  }

  def resultViewBefore(missionId: Int) = Action { implicit request =>
    val resultDir = tool.getResultDirById(missionId)
    val keggDir = new File(resultDir, "kegg")
    val kegg = if (keggDir.exists()) Some("kegg") else None
    val keggFileNames = if (keggDir.exists()) keggDir.listFiles().map(_.getName) else Array[String]()
    val cogDir = new File(resultDir, "eggNOG")
    val cog = if (cogDir.exists()) Some("eggNOG") else None
    val cogFileNames = if (cogDir.exists()) cogDir.listFiles().map(_.getName) else Array[String]()
    val nrDir = new File(resultDir, "NR")
    val nr = if (nrDir.exists()) Some("NR") else None
    val nrFileNames = if (nrDir.exists()) nrDir.listFiles().map(_.getName) else Array[String]()
    val goDir = new File(resultDir, "GO")
    val go = if (goDir.exists()) Some("GO") else None
    val goFileNames = if (goDir.exists()) goDir.listFiles().map(_.getName) else Array[String]()
    val swissDir = new File(resultDir, "Swissprot")
    val swiss = if (swissDir.exists()) Some("Swissprot") else None
    val swissFileNames = if (swissDir.exists()) swissDir.listFiles().map(_.getName) else Array[String]()
    val data = FileData(missionId, kegg, keggFileNames, cog, cogFileNames, nr, nrFileNames, go, goFileNames, swiss, swissFileNames)
    Ok(views.html.user.resultView(data))
  }

  def getAllMission = Action.async { implicit request =>
    val userId = tool.getUserId
    missionDao.selectAll(userId).map { x =>
      val array = getObjByMissions(x)
      Ok(Json.toJson(array))
    }
  }

  def deleteMissionById(id: Int) = Action.async { implicit request =>
    missionDao.deleteById(id).map { x =>
      val missionIdDir = tool.getMissionIdDirById(id)
      Utils.deleteDirectory(missionIdDir)
      Redirect(routes.AnnoController.getAllMission())
    }
  }


  def getObjByMissions(missions: Seq[MissionRow]) = {
    val array = missions.map { y =>
      val deleteStr = "<a title='删除' onclick=\"deleteMission('" + y.id + "')\" style='cursor: pointer;'><span><em class='fa fa-close'></em></span></a>"
      val downloadStr = s"<a title='下载结果' href='${routes.AnnoController.downloadResult(y.id)}' style='cursor: pointer;'><span><em class='fa fa-download'></em></span></a>"
      val viewStr = "<a title='日志' onclick=\"viewLog('" + y.id + "')\" style='cursor: pointer;'><span><em class='fa fa-file-text'></em></span></a>"
      val resultView = s"<a title='结果列表' href='${routes.AnnoController.resultViewBefore(y.id)}' style='cursor: pointer;'><span><em class='fa fa-eye'></em></span></a>"
      val endTimeStr = y.endtime.map { endTime =>
        endTime.toString("yyyy-MM-dd HH:mm:ss")
      }.getOrElse("暂无")
      val operates = ArrayBuffer[String]()
      if (y.state == "success") {
        operates += downloadStr
        operates += resultView
      }
      if (y.state != "running") {
        operates += viewStr
      }
      operates += deleteStr
      val missionIdStr=if(y.state=="success"){
        s"<a href='${routes.AnnoController.resultViewBefore(y.id)}' style='cursor: pointer;'>${y.missionname}</a>"
      } else y.missionname

      Json.obj("missionname" ->missionIdStr, "samplename" -> y.samplename,
        "state" -> y.state, "starttime" -> y.starttime.toString("yyyy-MM-dd HH:mm:ss"),
        "endtime" -> endTimeStr, "operate" -> operates.mkString("&nbsp;"))
    }
    Json.toJson(array)
  }

  case class missionIdData(missionId: Int)

  val missionIdForm = Form(
    mapping(
      "missionId" -> number
    )(missionIdData.apply)(missionIdData.unapply)
  )


  def getLogContent = Action.async { implicit request =>
    val userId = tool.getUserId
    val data = missionIdForm.bindFromRequest().get
    missionDao.selectByMissionId(userId, data.missionId).map { mission =>
      val missionIdDir = tool.getMissionIdDirById(data.missionId)
      val logFile = new File(missionIdDir, s"log.txt")
      val logStr = FileUtils.readFileToString(logFile, "UTF-8")
      Ok(Json.toJson(logStr))
    }
  }

  def downloadResult(missionId: Int) = Action.async { implicit request =>
    val userId = tool.getUserId
    missionDao.selectByMissionId(userId, missionId).map { mission =>
      val missionIdDir = tool.getMissionIdDirById(missionId)
      val resultDir = new File(missionIdDir, "result")
      val resultFile = new File(missionIdDir, s"result.zip")
      if (!resultFile.exists()) ZipUtil.pack(resultDir, resultFile)
      Ok.sendFile(resultFile).withHeaders(
        CACHE_CONTROL -> "max-age=3600",
        CONTENT_DISPOSITION -> s"attachment; filename=${mission.missionname}.zip",
        CONTENT_TYPE -> "application/x-download"
      )
    }
  }

  case class DownloadFileData(missionId: Int, kind: String, fileName: String)

  val downloadFileForm = Form(
    mapping(
      "missionId" -> number,
      "kind" -> text,
      "fileName" -> text
    )(DownloadFileData.apply)(DownloadFileData.unapply)
  )

  def downloadFile = Action { implicit request =>
    val data = downloadFileForm.bindFromRequest().get
    val resultDir = tool.getResultDirById(data.missionId)
    val file = new File(resultDir, s"${data.kind}/${data.fileName}")
    Ok.sendFile(file).withHeaders(
        CACHE_CONTROL -> "max-age=3600",
        CONTENT_DISPOSITION -> s"attachment; filename=${data.fileName}",
        CONTENT_TYPE -> "application/x-download"
      )
  }

  def updateMissionSocket = WebSocket.accept[JsValue, JsValue] { implicit request =>
    val userId = tool.getUserId
    var beforeMissions = Utils.execFuture(missionDao.selectAll(userId))
    var currentMissions = beforeMissions
    ActorFlow.actorRef(out => Props(new Actor {
      override def receive: Receive = {
        case msg: JsValue if (msg \ "info").as[String] == "start" =>
          out ! getObjByMissions(beforeMissions)
          system.scheduler.scheduleOnce(3 seconds, self, Json.obj("info" -> "update"))
        case msg: JsValue if (msg \ "info").as[String] == "update" =>
          missionDao.selectAll(userId).map { missions =>
            currentMissions = missions
            if (currentMissions.size != beforeMissions.size) {
              out ! getObjByMissions(currentMissions)
            } else {
              val b = currentMissions.zip(beforeMissions).forall { case (currentMission, beforeMission) =>
                currentMission.id == beforeMission.id && currentMission.state == beforeMission.state
              }
              if (!b) {
                out ! getObjByMissions(currentMissions)
              }
            }
            beforeMissions = currentMissions
            system.scheduler.scheduleOnce(3 seconds, self, Json.obj("info" -> "update"))
          }
        case _ =>
          self ! PoisonPill
      }

      override def postStop(): Unit = {
        self ! PoisonPill
      }

    }))

  }

}
