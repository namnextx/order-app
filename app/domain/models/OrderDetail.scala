package domain.models

import play.api.libs.json.{Json, OFormat}

case class OrderDetail(id: Option[Long], orderId: Option[Long], productId: Option[Long], quantity: Int, price: BigDecimal)


object OrderDetail {

    /**
     * Mapping to read/write a OrderResource out as a JSON value.
     */
    implicit val format: OFormat[OrderDetail] = Json.format[OrderDetail]
}
