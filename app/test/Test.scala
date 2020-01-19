package test

import java.io.{File, FileInputStream, InputStream, InputStreamReader}
import java.nio.file.{Files, Paths}

import akka.actor._
import akka.stream.ActorMaterializer

import scala.io.Source
import akka.stream.scaladsl._
import akka.util.ByteString
import org.apache.commons.io.FileUtils
import org.biojava.nbio.core.sequence.io._
import org.biojava.nbio.core.util.SequenceTools
import utils.{ExecCommand, Utils}

import collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.util._
import scala.util.control.Exception


/**
  * Created by yz on 2018/4/13
  */
object Test {

  def main(args: Array[String]): Unit = {
    val startTime = System.currentTimeMillis()
    val file = new File("E:\\anno_database\\user\\2\\mission\\18\\result\\kegg\\pathway.txt")
    val cleanFile = new File("E:\\anno_database\\user\\2\\mission\\18\\result\\kegg\\pathway.clean.txt")
    val lines = FileUtils.readLines(file).asScala
    val newLines = lines.filter(_.split("\t").size == 2).filter(_.contains("http")).map { line =>
      val lines = line.split("\t")
      lines(1) = lines(1).takeWhile(_ != '|')
      lines.mkString("\t")
    }
    FileUtils.writeLines(cleanFile,newLines.asJava)


    val arrayBuffer = ArrayBuffer[String]()

    getLines(new File("D:\\workspaceForIDEA\\anno\\app"))
    getLines(new File("D:\\workspaceForIDEA\\anno\\pythons"))
    getLines(new File("D:\\workspaceForIDEA\\anno\\conf"))
    getLines(new File("D:\\workspaceForIDEA\\anno\\perls"))
    getLines(new File("D:\\workspaceForIDEA\\anno\\public\\javascripts"))
    //    getLines(new File("D:\\workspaceForIDEA\\anno\\public\\stylesheets\\my.css"))
    println(arrayBuffer.size)
    arrayBuffer ++= FileUtils.readLines(new File("D:\\workspaceForIDEA\\anno\\public\\stylesheets\\my.css")).asScala
    FileUtils.writeLines(new File("E:\\code.txt"), arrayBuffer.asJava)

    def getLines(file: File): ArrayBuffer[String] = {
      for (f <- file.listFiles()) {
        if (f.isDirectory) {
          getLines(f)
        } else {
          arrayBuffer ++= FileUtils.readLines(f).asScala
        }
      }
      arrayBuffer
    }


    println(Utils.getTime(startTime))


  }

}
