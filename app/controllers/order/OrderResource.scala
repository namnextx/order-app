package controllers.order

import domain.models.{Post, Order}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class OrderResource(id: Long,
                         userId: Long,
                         orderDate: LocalDate,
                         totalPrice: BigDecimal)

object OrderResource {

  /**
   * Mapping to read/write a PostResource out as a JSON value.
   */
  implicit val format: OFormat[OrderResource] = Json.format[OrderResource]

  def fromOrder(order: Order): OrderResource =
    OrderResource(order.id.getOrElse(-1), order.userId, order.orderDate, order.totalPrice)
}