package domain.models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class Order(id: Option[Long], userId: Long, orderDate: LocalDate, totalPrice: BigDecimal)


object Order {

  /**
   * Mapping to read/write a OrderResource out as a JSON value.
   */
  implicit val format: OFormat[Order] = Json.format[Order]
}

