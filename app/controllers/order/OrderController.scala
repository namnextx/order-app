package controllers.order

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import domain.models.{Order, OrderDetail, User}
import exception.ValidationError
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import services.{OrderDetailService, OrderService, ProductService, UserService}
import utils.auth.{JWTEnvironment, WithRole}
import utils.logging.RequestMarkerContext
import utils.validation.CustomConstraints._
import play.api.data.Forms._

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


case class ProductFormCreate(productId: Long, quantities: Int)
case class OrderFormInputCreate(orderDate: LocalDate, products: Seq[ProductFormCreate])
case class OrderFormInputUpdate(orderDate: LocalDate)

/**
 * Takes HTTP requests and produces JSON.
 */
class OrderController @Inject() (cc: ControllerComponents,
                                orderService: OrderService,
                                userService: UserService,
                                productService: ProductService,
                                orderDetailService: OrderDetailService,
                                silhouette: Silhouette[JWTEnvironment])
                               (implicit ec: ExecutionContext)
  extends AbstractController(cc) with RequestMarkerContext {

  def SecuredAction: SecuredActionBuilder[JWTEnvironment, AnyContent] = silhouette.SecuredAction

  private val logger = Logger(getClass)

  val productFormCreate: Form[ProductFormCreate] = Form(
    mapping(
      "productId" -> longNumber,
      "quantities" -> number
      )(ProductFormCreate.apply)(ProductFormCreate.unapply))

  private val form: Form[OrderFormInputCreate] = {
    import play.api.data.Forms._
    Form(
      mapping(
        "orderDate" -> localDate("yyyy-MM-dd").verifying(dateInTheFuture),
        "products" -> seq(mapping (
          "productId" -> longNumber,
          "quantities" -> number(min = 1)
        )(ProductFormCreate.apply)(ProductFormCreate.unapply))
      )(OrderFormInputCreate.apply)(OrderFormInputCreate.unapply)
    )
  }

  private val formUpdate: Form[OrderFormInputUpdate] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "orderDate" -> localDate("yyyy-MM-dd")
      )(OrderFormInputUpdate.apply)(OrderFormInputUpdate.unapply)
    )
  }

  def getById(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async { implicit request =>
      logger.trace(s"getById: $id")
      val userRole: String = request.identity.role
      val userId: Long = request.identity.id.getOrElse(0L)

      userRole match {
        case "Admin" =>
          orderService.find(id).map {
            case Some(order) =>
              Ok(Json.toJson(OrderResource.fromOrder(order)))
            case None => NotFound
          }
        case "User" =>
          orderService.find(id).map {
            case Some(order) if userId == order.userId =>
              Ok(Json.toJson(OrderResource.fromOrder(order)))
            case _ => NotFound
          }
      }
    }

  def getAll: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async { implicit request =>
      logger.trace("getAll Orders")
      val userRole: String = request.identity.role
      val userId: Long = request.identity.id.getOrElse(0L)

      userRole match {
        case "Admin" =>
          orderService.listAll().map { orders =>
            Ok(Json.toJson(orders.map(order => OrderResource.fromOrder(order))))
          }
        case "User" =>
          orderService.getOrdersByUserId(userId).map { orders =>
            Ok(Json.toJson(orders.map(order => OrderResource.fromOrder(order))))
          }
      }
    }

  def create(): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async { implicit request =>
      val user:User = request.identity
      logger.trace("create Order: ")
      this.createOrder(user)
    }

  def update(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async { implicit request =>
      logger.trace(s"update order id: $id")
      val identity = request.identity

      updateOrder(Some(id), identity)
    }

  def delete(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async { implicit request =>
      logger.trace(s"Delete order: id = $id")

      val userRole: String = request.identity.role
      val userId: Long = request.identity.id.getOrElse(0L)
      userRole match {
        case "Admin" =>
          orderService.delete(id).map { deletedCnt =>
          if (deletedCnt == 1) Ok(JsString(s"Delete Order $id successfully"))
          else BadRequest(JsString(s"Unable to delete Order $id"))
        }
        case "User" =>
          orderService.find(id).flatMap {
            case Some(existingOrder) if userId == existingOrder.userId =>
              orderService.delete(id).map { deletedCnt =>
                if (deletedCnt == 2) Ok(JsString(s"Delete Order $id successfully"))
                else BadRequest(JsString(s"Unable to delete Order $id"))
              }
            case _ => Future.successful(Forbidden("You are not authorized to access this resource"))
          }
      }
    }


  private def createOrder[A](user: User)(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[OrderFormInputCreate]) = {
      val errorResponse = ValidationError.generateErrorResponse(badForm)
      Future.successful(BadRequest(errorResponse))
    }

    def success(input: OrderFormInputCreate) = {
      // create a Order from given form input
      logger.trace(s"Create new order")
      // create an order
      val newOrder = Order(None, userId = user.id.getOrElse(0L), orderDate = input.orderDate, totalPrice = BigDecimal(0))

      // save order
      val savedOrderFuture = orderService.save(newOrder)
      // Find all product
      val productQuantities: Map[Long, Int] = input.products.map(productFormCreate =>
        productFormCreate.productId -> productFormCreate.quantities).toMap

      val productIds = productQuantities.keys.toSeq
      val productsFuture = productService.findAllById(productIds)

      // Combine the saved order and retrieved products
      for {
        savedOrder <- savedOrderFuture
        products <- productsFuture

        orderDetails = products.flatMap( product => {
          productQuantities.get(product.id.getOrElse(0L)).map(quantity => {
            val price: BigDecimal = product.price * BigDecimal(quantity)
            OrderDetail(None, savedOrder.id, product.id, quantity, price)
          })
        })

        savedDetails <- orderDetailService.createOrderDetails(orderDetails)
        totalPrice = savedDetails.map(_.price).sum
        updatedOrder = savedOrder.copy(totalPrice = totalPrice)

        _ <- orderService.update(updatedOrder)
      } yield {
        logger.trace(s"order created $updatedOrder")
        Created(Json.toJson(OrderResource.fromOrder(updatedOrder)))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

  private def updateOrder[A](id: Option[Long], user: User)(implicit request: Request[A]): Future[Result] = {
    logger.trace("getAll Orders")

    def failure(badForm: Form[OrderFormInputUpdate]) = {
      val errorResponse = ValidationError.generateErrorResponse(badForm)
      Future.successful(BadRequest(errorResponse))
    }

    def success(input: OrderFormInputUpdate) = {

      id match {
        case Some(id) =>
          val userRole: String = user.role
          val userId: Long = user.id.getOrElse(0L)

          // Fetch the exiting order from db
          userRole match {
            case "Admin" =>
              orderService.find(id).flatMap {
                case Some(existingOrder) =>
                  val updateOrder = existingOrder.copy(orderDate = input.orderDate)
                  //save the updated order back to the db
                  orderService.update(updateOrder).map { _ =>
                    Created(Json.toJson(OrderResource.fromOrder(updateOrder)))
                  }
                case None =>
                  //Return None of the order doesn't exit
                  Future.successful(NotFound(JsString(s"Order with ID ${id} not found")))
              }
            case "User" =>
              orderService.find(id).flatMap {
                case Some(existingOrder) if userId == existingOrder.userId =>
                  val updateOrder = existingOrder.copy(orderDate = input.orderDate)
                  //save the updated order back to the db
                  orderService.update(updateOrder).map { _ =>
                    Created(Json.toJson(OrderResource.fromOrder(updateOrder)))
                  }
                case _ => Future.successful(Unauthorized("You are not authorized to access this resource"))
              }
            case _ => Future.successful(Unauthorized("You are not authorized to access this resource"))
          }

        case None =>
          Future.successful(BadRequest(JsString("Missing order ID")))
      }
    }

    formUpdate.bindFromRequest().fold(failure, success)
  }
}
