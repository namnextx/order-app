package controllers.user

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import javax.inject.{Inject, Singleton}

/**
 * Routes and URLs to the UserResource controller.
 */
@Singleton
class UserRouter @Inject() (controller:UserController) extends SimpleRouter {

  override def routes: Routes = {
    case GET(p"/") =>
      controller.getAll

    case GET(p"/$id") =>
      controller.getById(id.toLong)

    case DELETE(p"/$id") =>
      controller.delete(id.toLong)
  }

}
