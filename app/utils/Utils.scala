package utils

import java.io.{File, FileInputStream}

import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.lang3.StringUtils
import javax.imageio.ImageIO
import org.apache.commons.codec.binary.Base64
import org.biojava.nbio.core.sequence.io.FastaReaderHelper
import play.api.mvc.RequestHeader

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.math.log10

object Utils {

  val windowsPath = "E:\\anno_database"
  val linuxPath = "/mnt/sdb/yinzheng/projects/play/anno_database"
  val isWindows = if (new File(windowsPath).exists()) true else false
  val path = if (new File(windowsPath).exists()) windowsPath else linuxPath
  val userPath = new File(path, "user")
  val binPath = new File(path, "bin")
  val blastpFile = new File(binPath, "ncbi-blast-2.6.0+/bin/blastp")
  val blastxFile = new File(binPath, "ncbi-blast-2.6.0+/bin/blastx")
  val dbDir = new File(binPath, "Database")
  val keggDir = new File(binPath, "Database/kobas/seq_pep")
  val eggNOGDb = new File(binPath, "Database/eggNOG/eggNOG")
  val swissDb = new File(binPath, "Database/Swissprot/swissprot.fa")
  val b2gDir = new File(binPath, "b2g4pipe")
  val blast2Table = new File(binPath, "Anno/anno_st/Blast2table")
  val anno = new File(binPath, "Anno")
  val plScript = new File(binPath, "perls")

  def getDataPath={

  }



  //  val binPath = new File(path, "bin")
  //  val rPath = {
  //    val rPath = "D:\\workspaceForIDEA\\bioTechPlatform\\app\\rScripts"
  //    val linuxPath = "/root/projects/play/bioTech_database/rScripts"
  //    if (new File(rPath).exists()) rPath else linuxPath
  //  }
  //
  //  val Rscript = {
  //    val windowsPath = "D:\\workspaceForIDEA\\bioTechPlatform\\app\\rScripts"
  //    val linuxPath = "/root/projects/R/R-3.3.2/bin/Rscript"
  //    if (new File(windowsPath).exists()) "Rscript" else linuxPath
  //  }
  //
  //  val pyPath = {
  //    val path = "D:\\workspaceForIDEA\\bioTechPlatform\\pyScripts"
  //    val linuxPath = "/root/projects/play/bioTech_database/pyScripts"
  //    if (new File(path).exists()) path else linuxPath
  //  }
  //
  //  val goPy = {
  //    val path = "C:\\Python\\python.exe"
  //    if (new File(path).exists()) path else "python"
  //  }
  //
  //  val pyScript =
  //    """
  //      |<script>
  //      |Plotly.Plots.resize(document.getElementById($('#charts').children().eq(0).attr("id")));
  //      |window.addEventListener("resize", function (ev) {
  //      |				Plotly.Plots.resize(document.getElementById($('#charts').children().eq(0).attr("id")));
  //      |					})
  //      |</script>
  //      |
  //    """.stripMargin
  //
  def createDirectoryWhenNoExist(file: File): Unit = {
    if (!file.exists && !file.isDirectory) file.mkdir
  }

    def callScript(tmpDir: File, shBuffer: ArrayBuffer[String]) = {
      val execCommand = new ExecCommand
      val runFile = if (Utils.isWindows) {
        new File(tmpDir, "run.bat")
      } else {
        new File(tmpDir, "run.sh")
      }
      FileUtils.writeLines(runFile, shBuffer.asJava)
      val shCommand = runFile.getAbsolutePath
      if (Utils.isWindows) {
        execCommand.exec(shCommand, tmpDir)
      } else {
        val useCommand = "chmod +x " + runFile.getAbsolutePath
        val dos2Unix="dos2unix "+ runFile.getAbsolutePath
        execCommand.exec(dos2Unix,useCommand, shCommand, tmpDir)
      }
      execCommand
    }


    def deleteDirectory(direcotry: File) = {
      try {
        FileUtils.deleteDirectory(direcotry)
      } catch {
        case _ =>
      }
    }

  def getTime(startTime: Long) = {
    val endTime = System.currentTimeMillis()
    (endTime - startTime) / 1000.0
  }

  def isFastaFile(file: File) = {
    val proteins = FastaReaderHelper.readFastaProteinSequence(file).asScala
    val dnas = FastaReaderHelper.readFastaDNASequence(file).asScala
    if (proteins.size == 0 && dnas.size == 0) {
      false
    } else true
  }

  def getFastaAttr(file: File) = {
    case class FastaAttr(kind: String, maxLength: Int, minLength: Int, size: Int, meanLength: Int)
    val proteins = FastaReaderHelper.readFastaProteinSequence(file).asScala
    val dnas = FastaReaderHelper.readFastaDNASequence(file).asScala
    if (dnas.size >= proteins.size) {
      val sortSeqNum = dnas.values.map(x => x.getSequenceAsString.size).toBuffer.sorted
      FastaAttr("核酸", sortSeqNum.last, sortSeqNum.head, sortSeqNum.size, sortSeqNum.sum / sortSeqNum.size)
    } else {
      val sortSeqNum = proteins.values.map(x => x.getSequenceAsString.size).toBuffer.sorted
      FastaAttr("蛋白", sortSeqNum.last, sortSeqNum.head, sortSeqNum.size, sortSeqNum.sum / sortSeqNum.size)
    }
  }

  //
  //  def isDoubleP(value: String, p: Double => Boolean): Boolean = {
  //    try {
  //      val dbValue = value.toDouble
  //      p(dbValue)
  //    } catch {
  //      case _: Exception =>
  //        false
  //    }
  //  }
  //
  //  def addIdInHeader(dataFile: File) = {
  //    val buffer = FileUtils.readLines(dataFile).asScala
  //    buffer(0) = "ID\t" + buffer(0)
  //    FileUtils.writeLines(dataFile, buffer.asJava)
  //  }
  //
  //  def getGroupNum(content: String) = {
  //    content.split(";").size
  //  }
  //
  //  def productGroupFile(tmpDir: File, content: String) = {
  //    val groupFile = new File(tmpDir, "group.txt")
  //    val groupLines = ArrayBuffer("Sample\tGroup")
  //    groupLines ++= content.split(";").flatMap { group =>
  //      val groupName = group.split(":")(0)
  //      val sampleNames = group.split(":")(1).split(",")
  //      sampleNames.map { sampleName =>
  //        sampleName + "\t" + groupName
  //      }
  //    }
  //    FileUtils.writeLines(groupFile, groupLines.asJava)
  //  }
  //
  //  def getMap(content: String) = {
  //    val map = mutable.LinkedHashMap[String, mutable.Buffer[String]]()
  //    content.split(";").foreach { x =>
  //      val columns = x.split(":")
  //      map += (columns(0) -> columns(1).split(",").toBuffer)
  //    }
  //    map
  //  }
  //
  //  def getGroupNames(content: String) = {
  //    val map = getMap(content)
  //    map.keys.toBuffer
  //
  //  }
  //
  //  def replaceByRate(dataFile: File, rate: Double) = {
  //    val buffer = FileUtils.readLines(dataFile).asScala
  //    val array = buffer.map(_.split("\t"))
  //    val minValue = array.drop(1).flatMap(_.tail).filter(Utils.isDoubleP(_, _ > 0)).map(_.toDouble).min
  //    val rateMinValue = minValue * rate
  //    for (i <- 1 until array.length; j <- 1 until array(i).length) {
  //      if (Utils.isDoubleP(array(i)(j), _ == 0)) array(i)(j) = rateMinValue.toString
  //      if (StringUtils.isBlank(array(i)(j))) array(i)(j) = rateMinValue.toString
  //    }
  //    FileUtils.writeLines(dataFile, array.map(_.mkString("\t")).asJava)
  //  }
  //
  //  def replaceByValue(dataFile: File, assignValue: String) = {
  //    val buffer = FileUtils.readLines(dataFile).asScala
  //    val array = buffer.map(_.split("\t"))
  //    for (i <- 1 until array.length; j <- 1 until array(i).length) {
  //      if (Utils.isDoubleP(array(i)(j), _ == 0)) array(i)(j) = assignValue
  //      if (StringUtils.isBlank(array(i)(j))) array(i)(j) = assignValue
  //    }
  //    FileUtils.writeLines(dataFile, array.map(_.mkString("\t")).asJava)
  //  }
  //
  //  def relace0byNan(dataFile: File) = {
  //    val buffer = FileUtils.readLines(dataFile).asScala
  //    val array = buffer.map(_.split("\t"))
  //    for (i <- 1 until array.length; j <- 1 until array(i).length) {
  //      if (Utils.isDoubleP(array(i)(j), _ == 0)) array(i)(j) = "NA"
  //    }
  //    FileUtils.writeLines(dataFile, array.map(_.mkString("\t")).asJava)
  //  }
  //
  //  def innerNormal(dataFile: File, colName: String, rowName: String) = {
  //    val buffer = FileUtils.readLines(dataFile).asScala
  //    val array = buffer.map(_.split("\t"))
  //    val colNum = array.take(1).flatten.indexOf(colName)
  //    val rowNum = array.map(_ (0)).indexOf(rowName)
  //    val div = array(rowNum)(colNum).toDouble
  //    val divArray = array(rowNum).drop(1).map(div / _.toDouble)
  //    for (i <- 1 until array.length; j <- 1 until array(i).length) {
  //      array(i)(j) = (array(i)(j).toDouble * divArray(j - 1)).toString
  //    }
//      FileUtils.writeLines(dataFile, array.map(_.mkString("\t")).asJava)
  //  }

    def execFuture[T](f: Future[T]): T = {
      Await.result(f, Duration.Inf)
    }


  //  def peakAreaNormal(dataFile: File, coefficient: Double) = {
  //    val buffer = FileUtils.readLines(dataFile).asScala
  //    val array = buffer.map(_.split("\t"))
  //    val sumArray = new Array[Double](array(0).length)
  //    for (i <- 1 until array.length; j <- 1 until array(i).length) {
  //      sumArray(j) += array(i)(j).toDouble
  //    }
  //    for (i <- 1 until array.length; j <- 1 until array(i).length) {
  //      array(i)(j) = (coefficient * array(i)(j).toDouble / sumArray(j)).toString
  //    }
  //    FileUtils.writeLines(dataFile, array.map(_.mkString("\t")).asJava)
  //  }
  //
  //  def log2(x: Double) = log10(x) / log10(2.0)
  //
  //  def getStdErr(values: Array[Double]) = {
  //    val standardDeviation = new StandardDeviation
  //    val stderr = standardDeviation.evaluate(values) / Math.sqrt(values.length)
  //    stderr
  //  }
  //
  //  def pdf2png(tmpDir: File, fileName: String) = {
  //    val pdfFile = new File(tmpDir, fileName)
  //    val outFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".png"
  //    val outFile = new File(tmpDir, outFileName)
  //    val document = PDDocument.load(pdfFile)
  //    val renderer = new PDFRenderer(document)
  //    ImageIO.write(renderer.renderImage(0, 3), "png", outFile)
  //    document.close()
  //  }
  //
  //  def getInfoByFile(file: File) = {
  //    val lines = FileUtils.readLines(file).asScala
  //    val columnNames = lines.head.split("\t").drop(1)
  //    val array = lines.drop(1).map { line =>
  //      val columns = line.split("\t")
  //      val map = mutable.Map[String, String]()
  //      map += ("geneId" -> columns(0))
  //      columnNames.zip(columns.drop(1)).foreach { case (columnName, data) =>
  //        map += (columnName -> data)
  //      }
  //      map
  //    }
  //    (columnNames, array)
  //  }
  //
  //  def checkFile(file: File): (Boolean, String) = {
  //    val buffer = FileUtils.readLines(file).asScala
  //    val headers = buffer.head.split("\t")
  //    var error = ""
  //    if (headers.size < 2) {
  //      error = "错误：文件列数小于2！"
  //      return (false, error)
  //    }
  //    val headersNoHead = headers.drop(1)
  //    val repeatElement = headersNoHead.diff(headersNoHead.distinct).distinct.headOption
  //    repeatElement match {
  //      case Some(x) => val nums = headers.zipWithIndex.filter(_._1 == x).map(_._2 + 1).mkString("(", "、", ")")
  //        error = "错误：样品名" + x + "在第" + nums + "列重复出现！"
  //        return (false, error)
  //      case None =>
  //    }
  //
  //    val ids = buffer.drop(1).map(_.split("\t")(0))
  //    val repeatid = ids.diff(ids.distinct).distinct.headOption
  //    repeatid match {
  //      case Some(x) => val nums = ids.zipWithIndex.filter(_._1 == x).map(_._2 + 2).mkString("(", "、", ")")
  //        error = "错误：第一列:" + x + "在第" + nums + "行重复出现！"
  //        return (false, error)
  //      case None =>
  //    }
  //
  //    val headerSize = headers.size
  //    for (i <- 1 until buffer.size) {
  //      val columns = buffer(i).split("\t")
  //      if (columns.size != headerSize) {
  //        error = "错误：数据文件第" + (i + 1) + "行列数不对！"
  //        return (false, error)
  //      }
  //
  //    }
  //
  //    for (i <- 1 until buffer.size) {
  //      val columns = buffer(i).split("\t")
  //      for (j <- 1 until columns.size) {
  //        val value = columns(j)
  //        if (!isDouble(value)) {
  //          error = "错误：数据文件第" + (i + 1) + "行第" + (j + 1) + "列不为数字！"
  //          return (false, error)
  //        }
  //      }
  //    }
  //    (true, error)
  //  }
  //
  //  def isDouble(value: String): Boolean = {
  //    try {
  //      value.toDouble
  //    } catch {
  //      case _: Exception =>
  //        return false
  //    }
  //    true
  //  }
  //
    def getBase64Str(imageFile: File): String = {
      val inputStream = new FileInputStream(imageFile)
      val bytes = IOUtils.toByteArray(inputStream)
      val bytes64 = Base64.encodeBase64(bytes)
      inputStream.close()
      new String(bytes64)
    }

}
