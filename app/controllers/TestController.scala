package controllers

import java.io.{File, FileInputStream, InputStream}
import java.nio.file.Files
import java.nio.file.attribute.{PosixFilePermission, PosixFilePermissions}
import java.util

import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.stream.{IOResult, Materializer}
import akka.stream.scaladsl._
import akka.util.ByteString
import dao.MissionDao
import javax.inject.{Inject, Provider}
import models.DummyPlaceHolder
import org.apache.commons.io.FileUtils
import org.asynchttpclient.netty.handler.WebSocketHandler
import org.reactivestreams.Publisher
import play.api._
import play.api.cache.CacheApi
import play.api.data._
import play.api.data.Forms._
import play.api.http._
import play.api.i18n._
import play.api.libs.Comet
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.streams.{Accumulator, ActorFlow}
import play.api.libs.ws.ahc.AhcCurlRequestLogger
import play.api.libs.ws.{StreamedBody, WSAuthScheme, WSClient, WSRequest}
import play.api.mvc.MultipartFormData.{DataPart, FilePart}
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._
import play.api.routing.Router
import play.core.parsers.Multipart.FileInfo
import utils.Utils

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._
import scala.xml.{Node, NodeSeq}

/**
  * Created by yz on 2018/4/12
  */
class TestController @Inject()(missionDao: MissionDao, tool: Tool)(implicit val system: ActorSystem,
                                                                   implicit val ec: ExecutionContext,
                                                                   implicit val materializer: Materializer,
                                                                   val messagesApi:MessagesApi,
                                                                   val cache: CacheApi
) extends Controller with I18nSupport {

  def test = Action.async{ implicit request =>
    val rs=request.queryString
    println(rs)
    missionDao.test.map{x=>
      Ok(views.html.test.test())
    }

  }

  def test1 = Action { implicit request =>
    val rs=request.body
    println(rs)
    Ok("success")
  }


}





