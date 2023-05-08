package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.dao.ProductDao
import domain.models.Product


import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ProductServiceImpl])
trait ProductService {
  /**
   * Finds a products by id.
   *
   * @param id The products id to find.
   * @return The found products or None if no products for the given id could be found.
   */
  def find(id: Long): Future[Option[Product]]

  /**
   * List all products.
   *
   * @return All existing products.
   */
  def listAll(): Future[Iterable[Product]]

  /**
   * Deletes a product
   *
   * @param id The product's id to delete.
   * @return The deleted product.
   */
  def delete(id: Long): Future[Int]

  /**
   * Saves a product.
   *
   * @param product The product to save.
   * @return The saved product.
   */
  def save(product: Product): Future[Product]

  /**
   * Updates a product.
   *
   * @param product The product to update.
   * @return The updated post.
   */
  def update(product: Product): Future[Product]

  def findAllById(productIds: Seq[Long]): Future[Seq[Product]]

}

@Singleton
class ProductServiceImpl @Inject()(productDao: ProductDao)(implicit ex: ExecutionContext) extends ProductService {
  override def find(id: Long): Future[Option[Product]] = productDao.find(id)

  override def listAll(): Future[Iterable[Product]] = productDao.listAll()

  override def delete(id: Long): Future[Int] = productDao.delete(id)

  override def save(product: Product): Future[Product] = productDao.save(product)

  override def update(product: Product): Future[Product] = productDao.update(product)

  override def findAllById(productIds: Seq[Long]): Future[Seq[Product]] = productDao.findAllByIds(productIds)

}
