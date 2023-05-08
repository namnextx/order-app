package domain.tables

import domain.models.Product
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDate

class ProductTable(tag: Tag) extends Table[Product](tag, Some("order_v1"), "products") {

  /** The ID column, which is the primary key, and auto incremented */
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc, O.Unique)

  /** The product name column */
  def productName = column[String]("product_name")

  /** The email column */
  def expDate = column[LocalDate]("exp_date")

  def price = column[BigDecimal]("price")

  def * = (id, productName, expDate, price) <> ((Product.apply _).tupled, Product.unapply)
}
