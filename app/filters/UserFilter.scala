package filters

import java.net.InetAddress

import javax.inject.Inject
import akka.stream.Materializer
import controllers.{Tool, routes}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by yz on 2017/6/13.
  */
class UserFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext,tool: Tool) extends Filter {

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    if (rh.session.get("user").isEmpty && rh.path.contains("/user") && !rh.path.contains("/assets/") &&
      !rh.path.contains("/login")) {
      val domain=tool.getRequestDomain(rh)
      Future.successful(Results.Redirect(s"http://${domain}").flashing("info"->"请先登录!"))
    } else {
      f(rh)
    }
  }

}
