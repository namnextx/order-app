package domain.tables

import domain.models.Order
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDate

class OrderTable(tag: Tag) extends Table[Order](tag, Some("order_v1"), "orders") {
  /** The ID column, which is the primary key, and auto incremented */
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc, O.Unique)

  def userId = column[Long]("user_id")

  /** The user_date column*/
  def orderDate = column[LocalDate]("order_date")

  /** The total_price column*/
  def totalPrice = column[BigDecimal]("total_price")

  def * = (id, userId, orderDate, totalPrice) <> ((Order.apply _).tupled, Order.unapply)

}
