package controllers.product

import play.api.libs.json.{Json, OFormat}
import domain.models.Product
import java.time.LocalDate

case class ProductResource(id: Long, productName: String, expDate: LocalDate, price: BigDecimal)

object ProductResource {

  /**
   * Mapping to read/write a PostResource out as a JSON value.
   */
  implicit val format: OFormat[ProductResource] = Json.format[ProductResource]

  def fromProduct(product: Product): ProductResource =
    ProductResource(product.id.getOrElse(-1), product.productName, product.expDate, product.price)
}
