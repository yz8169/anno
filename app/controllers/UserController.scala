package controllers

import java.io.File

import javax.inject.Inject
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import dao._
import utils.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.filters.cors.CORSFilter

/**
  * Created by yz on 2018/4/12
  */
case class UserData(account: String, password: String)

class UserController @Inject()(tool: Tool, missionDao: MissionDao, sampleDao: SampleDao) extends Controller {

  def loginBefore = Action { implicit request =>
    Ok(views.html.user.login())
  }

  def toIndex = Action { implicit request =>
    Ok(views.html.user.sampleManage())
  }

  def redirect2Index = Action { implicit request =>
    Redirect(routes.UserController.toIndex())
  }

  def mock=Action{implicit request=>
    Redirect(routes.UserController.toIndex()).withSession("user"->"yz","id"->"2")
  }

  def delete(userId: Int) = Action.async { implicit request =>
    println(request.session)
    if (request.session.get("admin").isDefined && request.session.get("id").get == "1") {
      sampleDao.deleteByUserId(userId).zip(missionDao.deleteByUserId(userId)).map { x =>
        val userIdDir = tool.getUserIdDir(userId)
        Utils.deleteDirectory(userIdDir)
        Ok("success!")
      }
    } else {
      Future.successful(Forbidden("Forbidden!"))
    }
  }

  val userForm = Form(
    mapping(
      "account" -> text,
      "password" -> text
    )(UserData.apply)(UserData.unapply)
  )

  //  def login = Action.async { implicit request =>
  //    val data = userForm.bindFromRequest.get
  //    userDao.selectByUserData(data).map { x =>
  //      x.map { user =>
  //        val file = new File(Utils.userPath, user.id.toString)
  //        Utils.createDirectoryWhenNoExist(file)
  //        val dataFile=new File(file, "data")
  //        Utils.createDirectoryWhenNoExist(dataFile)
  //        val missionFile=new File(file,"mission")
  //        Utils.createDirectoryWhenNoExist(missionFile)
  //        val rSession = request.session + ("user" -> data.account)
  //        Redirect(routes.UserController.toIndex()).withSession(rSession)
  //      }.getOrElse {
  //        Redirect(routes.UserController.loginBefore()).flashing("info" -> "账号或密码错误!")
  //      }
  //    }
  //  }

  def toHome = Action { implicit request =>
    val domain = tool.getRequestDomain
    Redirect(s"http://${domain}/project/platformHome")
  }

  def logout = Action { implicit request =>
    val domain = tool.getRequestDomain
    Redirect(s"http://${domain}/").flashing("info" -> "退出登录成功!").withNewSession
  }


}
