package controllers.auth

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.user.{UserFormInputCreate, UserResource}
import domain.models.User
import exception.ValidationError
import play.api.data.Form
import play.api.i18n.Lang
import play.api.libs.json.{JsString, Json, OFormat}
import play.api.mvc.{Action, AnyContent, Request, Result}
import utils.auth.WithRole
import utils.validation.CustomConstraints._

import scala.concurrent.{ExecutionContext, Future}

class AuthController @Inject() (components: SilhouetteControllerComponents)
                               (implicit ex: ExecutionContext)
  extends SilhouetteController(components) {

  case class SignInModel(email: String, password: String)

  implicit val signInFormat: OFormat[SignInModel] = Json.format[SignInModel]

  implicit val userFormat: OFormat[IdentityType] = Json.format[User]


  private val form: Form[UserFormInputCreate] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "email"       -> nonEmptyText,
        "firstName"   -> nonEmptyText(),
        "lastName"    -> nonEmptyText(maxLength = 128),
        "password"    -> nonEmptyText(),
        "role"        ->     nonEmptyText(minLength = 0).verifying(roleValidate),
        "birthDate"   -> localDate("yyyy-MM-dd"),
        "address"     -> nonEmptyText,
        "phoneNumber" -> nonEmptyText
      )(UserFormInputCreate.apply)(UserFormInputCreate.unapply)
    )
  }

  /**
   * Handles sign up request
   *
   * @return The result to display.
   */
  def signUp: Action[AnyContent] = UnsecuredAction.async { implicit request: Request[AnyContent] =>

    implicit val lang: Lang = supportedLangs.availables.head

    request.body.asJson.flatMap(_.asOpt[User]) match {
      case Some(newUser) if newUser.password.isDefined =>
        userService.retrieve(LoginInfo(CredentialsProvider.ID, newUser.email)).flatMap {
          case Some(_) =>
            Future.successful(Conflict(JsString(messagesApi("user.already.exist"))))
          case None =>
            val authInfo = passwordHasherRegistry.current.hash(newUser.password.get)
            val user = newUser.copy(password = Some(authInfo.password))
            userService.save(user).map(u => Ok(Json.toJson(u.copy(password = None))))
        }
      case _ => Future.successful(BadRequest(JsString(messagesApi("invalid.body"))))
    }
  }

  /**
   * Handles sign in request
   *
   * @return JWT token in header if login is successful or Bad request if credentials are invalid
   */
  def signIn: Action[AnyContent] = UnsecuredAction.async { implicit request: Request[AnyContent] =>

    implicit val lang: Lang = supportedLangs.availables.head

    request.body.asJson.flatMap(_.asOpt[SignInModel]) match {
      case Some(signInModel) =>

        val credentials = Credentials(signInModel.email, signInModel.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(_) =>
              for {
                authenticator <- authenticatorService.create(loginInfo)
                token <- authenticatorService.init(authenticator)
                result <- authenticatorService.embed(token, Ok)
              } yield {
                logger.debug(s"User ${loginInfo.providerKey} signed success")
                result
              }
            case None => Future.successful(BadRequest(JsString(messagesApi("could.not.find.user"))))
          }
        }.recover {
          case _: ProviderException => BadRequest(JsString(messagesApi("invalid.credentials")))
        }
      case None => Future.successful(BadRequest(JsString(messagesApi("could.not.find.user"))))
    }
  }

  def create: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async { implicit request =>
      logger.trace("create new user")
      this.createUser()
    }

  private def createUser[A]()(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[UserFormInputCreate]) = {
      val errorResponse = ValidationError.generateErrorResponse(badForm)
      Future.successful(BadRequest(errorResponse))
    }

    def success(input: UserFormInputCreate) = {
      // create a product from given form input
      val authInfo = passwordHasherRegistry.current.hash(input.password)
      val newUser = User(None, input.email,input.role, input.firstName, input.lastName,
                         None, input.birthDate, input.address, input.phoneNumber)

      val user = newUser.copy(password = Some(authInfo.password))
      userService.save(user).map { user =>
        logger.info(s"Create new user $newUser")
        Created(Json.toJson(UserResource.fromUser(user)))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

}
