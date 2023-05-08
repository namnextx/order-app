package controllers.order

import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, DefaultActionBuilder, MessagesRequestHeader, PlayBodyParsers, PreferredMessagesProvider, Request, WrappedRequest}
import services.{OrderService, ProductService, UserService}

import javax.inject.Inject

trait OrderRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider

class OrderRequest[A](request: Request[A], val messagesApi: MessagesApi)
  extends WrappedRequest(request) with OrderRequestHeader


case class OrderControllerComponents @Inject()(
                                              //                                               postActionBuilder: PostActionBuilder,
                                              orderService: OrderService,
                                              userService: UserService,
                                              productService: ProductService,
                                              actionBuilder: DefaultActionBuilder,
                                              parsers: PlayBodyParsers,
                                              messagesApi: MessagesApi,
                                              langs: Langs,
                                              fileMimeTypes: FileMimeTypes,
                                              executionContext: scala.concurrent.ExecutionContext)
  extends ControllerComponents
