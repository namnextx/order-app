package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.dao.OrderDetailDao
import domain.models.OrderDetail

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[OrderDetailServiceImpl])
trait OrderDetailService {

  /**
   * Saves a post.
   *
   * @param orderDetails The orderDetails to save.
   * @return The saved order details.
   */
  def createOrderDetails(orderDetails: Seq[OrderDetail]): Future[Seq[OrderDetail]]
}

@Singleton
class OrderDetailServiceImpl @Inject()(orderDetailDao: OrderDetailDao)(implicit ex: ExecutionContext)
  extends OrderDetailService {

   def createOrderDetails(orderDetails: Seq[OrderDetail]): Future[Seq[OrderDetail]] = orderDetailDao.createOrderDetails(orderDetails)
}
