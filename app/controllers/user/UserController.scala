package controllers.user

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import controllers.auth.SilhouetteControllerComponents
import controllers.post.PostFormInput
import domain.models.{Post, User}
import exception.ValidationError
import httpclient.ExternalServiceException
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import services.{ExternalPostService, PostService, UserService}
import utils.auth.{JWTEnvironment, WithRole}
import utils.logging.RequestMarkerContext

import java.time.{LocalDate, LocalDateTime}
import java.util.Optional
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class UserFormInputCreate(email: String,
                               firstName: String,
                               lastName: String,
                               password: String,
                               role: String,
                               birthDate: LocalDate,
                               address: String,
                               phoneNumber: String)


class UserController @Inject() (cc: ControllerComponents,
                                postService: PostService,
                                extPostService: ExternalPostService,
                                userService: UserService,
                                silhouette: Silhouette[JWTEnvironment])
                               (implicit ec: ExecutionContext) extends AbstractController(cc) with RequestMarkerContext {



  def SecuredAction: SecuredActionBuilder[JWTEnvironment, AnyContent] = silhouette.SecuredAction

  private val logger = Logger(getClass)

  private val form: Form[UserFormInputCreate] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "email" -> nonEmptyText,
        "firstName" -> nonEmptyText(),
        "lastName" -> nonEmptyText(maxLength = 128),
        "password" -> nonEmptyText(),
        "role" -> nonEmptyText(minLength = 0),
        "birthDate" -> localDate(""),
        "address" -> nonEmptyText,
        "phoneNumber" -> nonEmptyText
      )(UserFormInputCreate.apply)(UserFormInputCreate.unapply)
    )
  }

  def getAll: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
      logger.trace("getAll Users")
      userService.listAll().map { users =>
        Ok(Json.toJson(users.map(post => UserResource.fromUser(post))))
      }
    }

  def getById(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
      logger.trace(s"getById: $id")
      userService.find(id).map {
        case Some(user) => Ok(Json.toJson(UserResource.fromUser(user)))
        case None => NotFound
      }
    }

  def delete(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
      logger.trace(s"Delete user: id = $id")
      userService.delete(id).map { deletedCnt =>
        if (deletedCnt == 1) Ok(JsString(s"Delete user $id successfully"))
        else BadRequest(JsString(s"Unable to delete user $id"))
      }
    }


}
