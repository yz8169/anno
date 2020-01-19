package controllers

import java.io.File

import dao._
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import models.Tables._
import org.joda.time.DateTime
import play.api.data._
import play.api.data.Forms._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by yz on 2018/4/12
  */
class SampleManageController @Inject()(sampleDao: SampleDao, tool: Tool) extends Controller {

  def toIndex = Action { implicit request =>
    Ok(views.html.user.sampleManage())
  }

  def deleteSampleById(id: Int) = Action.async { implicit request =>
    sampleDao.deleteById(id).map { x =>
      val sampleFile = tool.getSampleFileById(id)
      sampleFile.delete()
      Redirect(routes.SampleManageController.getAllSample())
    }
  }

  def switchKindById(id: Int) = Action.async { implicit request =>
    sampleDao.selectById(id).flatMap { x =>
      val row = if (x.kind == "核酸") {
        x.copy(kind = "蛋白")
      } else x.copy(kind = "核酸")
      sampleDao.update(row)
    }.map { x =>
      Redirect(routes.SampleManageController.getAllSample())
    }
  }

  def getAllSample = Action.async {implicit request=>
    val userId=tool.getUserId
    sampleDao.selectAll(userId).map { x =>
      val array = x.map { y =>
        val deleteStr = "<a title='删除' onclick=\"deleteSample('" + y.id + "')\" style='cursor: pointer;'><span><em class='fa fa-close'></em></span></a>"
        val updateDescribeStr = "<a title='更改描述' onclick=\"updateDescribe('" + y.id + "')\" style='cursor: pointer;'><span><em class='fa fa-edit'></em></span></a>"
        val kindStr = "<a title='变更类型' onclick=\"switchKind('" + y.id + "')\" style='cursor: pointer;'><span><em class='fa fa-refresh'></em></span></a>"
        Json.obj("samplename" -> y.samplename, "uploadtime" -> y.uploadtime.toString("yyyy-MM-dd HH:mm:ss"),
          "max" -> y.max, "min" -> y.min, "mean" -> y.mean, "size" -> y.size,
          "kind" -> (y.kind + "&nbsp" + kindStr),
          "describe" -> (y.describe + "&nbsp;" * 2 + updateDescribeStr),
          "operate" -> deleteStr)
      }
      Ok(Json.toJson(array))
    }
  }

  case class IdData(id: Int)

  case class nameData(projectName: String)

  val idForm = Form(
    mapping(
      "id" -> number
    )(IdData.apply)(IdData.unapply)
  )

  def getSampleById(id: Int) = Action.async { implicit request =>
    sampleDao.selectById(id).map { x =>
      Ok(Json.obj("describe" -> x.describe, "id" -> x.id))
    }
  }


  def updateDescribe = Action.async { implicit request =>
    val data = tool.describeForm.bindFromRequest().get
    val idData = idForm.bindFromRequest().get
    sampleDao.selectById(idData.id).flatMap { x =>
      val row = x.copy(describe = data.describe)
      sampleDao.update(row)
    }.map { x =>
      Redirect(routes.SampleManageController.getAllSample())
    }

  }


}
