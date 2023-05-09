package domain.models.dto

import domain.models.OrderDetail
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class OrderWithDetails(id: Option[Long], userId: Long, orderDate: LocalDate, totalPrice: BigDecimal, orderDetails: Seq[OrderDetail])

object OrderWithDetails {

  /**
   * Mapping to read/write a OrderResource out as a JSON value.
   */
  implicit val format: OFormat[OrderWithDetails] = Json.format[OrderWithDetails]
}
