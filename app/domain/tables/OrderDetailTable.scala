package domain.tables

import domain.models.OrderDetail
import slick.jdbc.PostgresProfile.api._

class OrderDetailTable(tag: Tag) extends Table[OrderDetail](tag, Some("order_v1"), "order_details") {
  /** The ID column, which is the primary key, and auto incremented */
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc, O.Unique)

  def orderId = column[Option[Long]]("order_id")

  def productId = column[Option[Long]]("product_id")

  def quantity = column[Int]("quantity")

  def price = column[BigDecimal]("price")

  def * = (id, orderId, productId, quantity, price) <> ((OrderDetail.apply _).tupled, OrderDetail.unapply)
}
