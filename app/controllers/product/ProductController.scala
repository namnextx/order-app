package controllers.product

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import domain.models.Product
import exception.ValidationError
import httpclient.ExternalServiceException
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import services.{ExternalProductService, ProductService, UserService}
import utils.auth.{JWTEnvironment, WithRole}
import utils.logging.RequestMarkerContext
import utils.validation.CustomConstraints._

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class ProductFormInputCreate(productName: String, expDate: LocalDate, price: BigDecimal)
case class ProductFormInputUpdate(expDate: LocalDate, price: BigDecimal)

/**
 * Takes HTTP requests and produces JSON.
 */
class ProductController @Inject() (cc: ControllerComponents,
                                   productService: ProductService,
                                   externalProductService: ExternalProductService,
                                   userService: UserService,
                                   silhouette: Silhouette[JWTEnvironment])
                                   (implicit ec: ExecutionContext)
  extends AbstractController(cc) with RequestMarkerContext {

  def SecuredAction: SecuredActionBuilder[JWTEnvironment, AnyContent] = silhouette.SecuredAction

  private val logger = Logger(getClass)

  private val form: Form[ProductFormInputCreate] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "productName" -> nonEmptyText(minLength = 3),
        "expDate" -> localDate("yyyy-MM-dd").verifying(dateInTheFuture),
        "price" -> bigDecimal(22, 4)
      )(ProductFormInputCreate.apply)(ProductFormInputCreate.unapply)
    )
  }

  private val formUpdate: Form[ProductFormInputUpdate] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "expDate" -> localDate("yyyy-MM-dd").verifying(dateInTheFuture),
        "price" -> bigDecimal(22, 4)
      )(ProductFormInputUpdate.apply)(ProductFormInputUpdate.unapply)
    )
  }

  def getById(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "Operator", "User")).async { implicit request =>
      logger.trace(s"getById: $id")
      productService.find(id).map {
        case Some(product) => Ok(Json.toJson(ProductResource.fromProduct(product)))
        case None => NotFound
      }
    }

  def getAll: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "Operator", "User")).async { implicit request =>
      logger.trace("getAll products")
      productService.listAll().map { products =>
        Ok(Json.toJson(products.map(product => ProductResource.fromProduct(product))))
      }
    }

  def create: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "Operator")).async { implicit request =>
      logger.trace("create product: ")
      createProduct(None)
    }

  def update(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "Operator")).async { implicit request =>
      logger.trace(s"update product id: $id")
      updateProduct(Some(id))
    }

  def delete(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "Operator")).async { implicit request =>
      logger.trace(s"Delete product: id = $id")
      productService.delete(id).map { deletedCnt =>
        if (deletedCnt == 1) Ok(JsString(s"Delete product $id successfully"))
        else BadRequest(JsString(s"Unable to delete product $id"))
      }
    }

  //External product
  def getAllExternal: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "Operator")).async { implicit request =>
      logger.trace("getAll External products")

      // try/catch Future exception with transform
      externalProductService.listAll().transform {
        case Failure(exception) => handleExternalError(exception)
        case Success(products) => Try(Ok(Json.toJson(products.map(product => ProductResource.fromProduct(product)))))
      }
    }

  private def handleExternalError(throwable: Throwable): Try[Result] = {
    throwable match {
      case ese: ExternalServiceException =>
        logger.trace(s"An ExternalServiceException occurred: ${ese.getMessage}")
        if (ese.error.isEmpty)
          Try(BadRequest(JsString(s"An ExternalServiceException occurred. statusCode: ${ese.statusCode}")))
        else Try(BadRequest(Json.toJson(ese.error.get)))
      case _ =>
        logger.trace(s"An other exception occurred on getAllExternal: ${throwable.getMessage}")
        Try(BadRequest(JsString("Unable to create an external product")))
    }
  }

  private def createProduct[A](id: Option[Long])(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[ProductFormInputCreate]) = {
      val errorResponse = ValidationError.generateErrorResponse(badForm)
      Future.successful(BadRequest(errorResponse))
    }

    def success(input: ProductFormInputCreate) = {
      // create a product from given form input

      val newProduct = Product(id, input.productName, input.expDate, input.price)
      productService.save(newProduct).map { product =>
        logger.info(s"Create new product $newProduct")
        Created(Json.toJson(ProductResource.fromProduct(product)))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

  private def updateProduct[A](id: Option[Long])(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[ProductFormInputUpdate]) = {
      val errorResponse = ValidationError.generateErrorResponse(badForm)
      Future.successful(BadRequest(errorResponse))
    }

    def success(input: ProductFormInputUpdate) = {
      id match {
        case Some(id) =>
          // Fetch the exiting order from db
          productService.find(id).flatMap {
            case Some(existingProduct) =>
              val updateOrder = existingProduct.copy(expDate = input.expDate, price = input.price)
              //save the updated order back to the db
              productService.update(updateOrder).map { _ =>
                Ok(Json.toJson("Product updated successfully"))
              }
            case None =>
              //Return None of the order doesn't exit
              Future.successful(NotFound(JsString(s"Order with ID ${id} not found")))
          }
        case None =>
          Future.successful(BadRequest(JsString("Missing order ID")))
      }

    }

    formUpdate.bindFromRequest().fold(failure, success)
  }
}
