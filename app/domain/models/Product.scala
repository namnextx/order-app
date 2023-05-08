package domain.models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class Product(id: Option[Long], productName: String, expDate: LocalDate, price: BigDecimal)

object Product {

  /**
   * Mapping to read/write a OrderResource out as a JSON value.
   */
  implicit val format: OFormat[Product] = Json.format[Product]
}