package domain.dao

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.models.OrderDetail
import domain.tables.OrderDetailTable
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Future

@ImplementedBy(classOf[OrderDetailDaoImpl])
trait OrderDetailDao {

  /**
   * Saves a post.
   *
   * @param order The post to save.
   * @return The saved post.
   */
  def createOrderDetails(orderDetails: Seq[OrderDetail]): Future[Seq[OrderDetail]]

}

@Singleton
class OrderDetailDaoImpl @Inject()(daoRunner: DaoRunner)(implicit ec: DbExecutionContext)
  extends OrderDetailDao {

  private val orderDetailTable = TableQuery[OrderDetailTable]

  override def createOrderDetails(orderDetails: Seq[OrderDetail]): Future[Seq[OrderDetail]] = {
    val insertAction = orderDetails.map(orderDetail => orderDetailTable += orderDetail)
    daoRunner.run(DBIO.sequence(insertAction)).map(_ => orderDetails)
  }

}
