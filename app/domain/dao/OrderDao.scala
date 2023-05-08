package domain.dao

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.models.Order
import domain.tables.{OrderDetailTable, OrderTable}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

@ImplementedBy(classOf[OrderDaoImpl])
trait OrderDao {
  /**
   * Finds a post by id.
   *
   * @param id The post id to find.
   * @return The found post or None if no post for the given id could be found.
   */
  def find(id: Long): Future[Option[Order]]

  /**
   * List all orders.
   *
   * @return All existing orders.
   */
  def listAll(): Future[Iterable[Order]]

  /**
   * List all orders.
   *
   * @return All existing orders.
   */
  def getOrdersByUserId(userId: Long): Future[Iterable[Order]]

  /**
   * Updates a order.
   *
   * @param post The order to update.
   * @return The saved order.
   */
  def update(post: Order): Future[Order]

  /**
   * Deletes a order
   * @param id The order's id to delete.
   * @return The deleted order.
   */
  def delete(id: Long): Future[Int]

  /**
   * Saves a post.
   *
   * @param order The post to save.
   * @return The saved post.
   */
  def save(order: Order): Future[Order]
}

@Singleton
class OrderDaoImpl @Inject()(daoRunner: DaoRunner)(implicit ec: DbExecutionContext)
  extends OrderDao {

  private val orders = TableQuery[OrderTable]
  private val orderDetails = TableQuery[OrderDetailTable]

  override def find(id: Long): Future[Option[Order]] = daoRunner.run {
    orders.filter(_.id === id).result.headOption
  }

  override def listAll(): Future[Iterable[Order]] = daoRunner.run {
    orders.result
  }

  override def getOrdersByUserId(userId: Long): Future[Iterable[Order]] = daoRunner.run {
    orders.filter(_.userId === userId).result
  }

  override def save(order: Order): Future[Order] = daoRunner.run {
    orders returning orders += order
  }

  override def delete(id: Long): Future[Int] = {
    daoRunner.run {
      val deleteAction =  for {
        orderDetailsDeleted <- orderDetails.filter(_.orderId === id).delete
        orderDeleted <- orders.filter(_.id === id).delete
      } yield (orderDeleted + orderDetailsDeleted)

      deleteAction.transactionally
    }



  }

  override def update(order: Order): Future[Order] = daoRunner.run {
    orders.filter(_.id === order.id).update(order).map(_ => order)
  }


}
