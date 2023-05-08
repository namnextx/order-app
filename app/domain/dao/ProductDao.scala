package domain.dao

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.models.Product
import domain.tables.ProductTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

@ImplementedBy(classOf[ProductDaoImpl])
trait ProductDao {
  /**
   * Finds a post by id.
   *
   * @param id The post id to find.
   * @return The found post or None if no post for the given id could be found.
   */
  def find(id: Long): Future[Option[Product]]

  /**
   * List all products.
   *
   * @return All existing products.
   */
  def listAll(): Future[Iterable[Product]]

  /**
   * Updates a product.
   *
   * @param post The product to update.
   * @return The saved product.
   */
  def update(post: Product): Future[Product]

  /**
   * Deletes a product
   * @param id The product's id to delete.
   * @return The deleted product.
   */
  def delete(id: Long): Future[Int]

  /**
   * Saves a post.
   *
   * @param product The post to save.
   * @return The saved post.
   */
  def save(product: Product): Future[Product]

  def findAllByIds(ids: Seq[Long]): Future[Seq[Product]]
}

@Singleton
class ProductDaoImpl @Inject()(daoRunner: DaoRunner)(implicit ec: DbExecutionContext) extends ProductDao {
  private val products = TableQuery[ProductTable]

  override def find(id: Long): Future[Option[Product]] = daoRunner.run {
    products.filter(_.id === id).result.headOption
  }

  override def listAll(): Future[Iterable[Product]] = daoRunner.run {
    products.result
  }

  override def save(product: Product): Future[Product] = daoRunner.run {
    products returning products += product
  }

  override def delete(id: Long): Future[Int] = daoRunner.run {
    products.filter(_.id === id).delete
  }

  override def update(product: Product): Future[Product] = daoRunner.run {
    products.filter(_.id === product.id).update(product).map(_ => product)
  }

  override def findAllByIds(ids: Seq[Long]): Future[Seq[Product]] = daoRunner.run {
    products.filter(_.id.inSet(ids)).result
  }

}
