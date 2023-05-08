package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.models.Product
import httpclient.ExternalProductClient
import play.api.Configuration
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}


@ImplementedBy(classOf[ExternalProductServiceImpl])
trait ExternalProductService {
  /**
   * List all External Posts.
   *
   * @return All external posts.
   */
  def listAll(): Future[Iterable[Product]]

}

/**
 * Handles actions to External Products.
 *
 * @param client  The HTTP Client instance
 * @param ex      The execution context.
 */
@Singleton
class ExternalProductServiceImpl @Inject()(client: ExternalProductClient, config: Configuration)
                                       (implicit ex: ExecutionContext)
  extends ExternalProductService {

  def getAllProducts: String = config.get[String]("external.products.getAllProducts")


  override def listAll(): Future[Iterable[Product]] = client.get[Seq[Product]](getAllProducts)


}
