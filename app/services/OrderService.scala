package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.dao.OrderDao
import domain.models.Order

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[OrderServiceImpl])
trait OrderService {
  /**
   * Finds a Orders by id.
   *
   * @param id The Orders id to find.
   * @return The found Orders or None if no Orders for the given id could be found.
   */
  def find(id: Long): Future[Option[Order]]

  /**
   * List all Orders.
   *
   * @return All existing Orders.
   */
  def listAll(): Future[Iterable[Order]]

  /**
   * List all Orders by userId.
   *
   * @return All existing Orders related to one specific user.
   */
  def getOrdersByUserId(userId: Long): Future[Iterable[Order]]

  /**
   * Deletes a order
   * @param id The order's id to delete.
   * @return The deleted order.
   */
  def delete(id: Long): Future[Int]

  /**
   * Saves a order.
   *
   * @param order The order to save.
   * @return The saved order.
   */
  def save(order: Order): Future[Order]

  /**
   * Updates a order.
   *
   * @param order The order to update.
   * @return The updated post.
   */
  def update(order: Order): Future[Order]

}

@Singleton
class OrderServiceImpl @Inject()(orderDao: OrderDao)(implicit ex: ExecutionContext) extends OrderService {
  override def find(id: Long): Future[Option[Order]] = orderDao.find(id)

  override def listAll(): Future[Iterable[Order]] = orderDao.listAll()

  def getOrdersByUserId(userId: Long): Future[Iterable[Order]] = orderDao.getOrdersByUserId(userId)

  override def delete(id: Long): Future[Int] = orderDao.delete(id)

  override def save(order: Order): Future[Order] = orderDao.save(order)

  override def update(order: Order): Future[Order] = orderDao.update(order)

}
