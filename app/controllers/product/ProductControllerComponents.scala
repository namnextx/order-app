package controllers.product

import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import services.{ProductService, UserService}

import javax.inject.Inject

/**
 * A wrapped request for post resources.
 *
 * This is commonly used to hold request-specific information like security credentials, and useful shortcut methods.
 */
trait ProductRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider

class ProductRequest[A](request: Request[A], val messagesApi: MessagesApi)
  extends WrappedRequest(request) with ProductRequestHeader

case class ProductControllerComponents @Inject()(
                                                  productService: ProductService,
                                                  userService: UserService,
                                                  actionBuilder: DefaultActionBuilder,
                                                  parsers: PlayBodyParsers,
                                                  messagesApi: MessagesApi,
                                                  langs: Langs,
                                                  fileMimeTypes: FileMimeTypes,
                                                  executionContext: scala.concurrent.ExecutionContext)
  extends ControllerComponents


