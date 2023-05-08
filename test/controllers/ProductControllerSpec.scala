package controllers

import com.mohiva.play.silhouette.test._
import controllers.product.ProductResource
import controllers.user.UserResource
import domain.models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import play.api.test._
import utils.auth.JWTEnvironment

import java.time.LocalDate
import scala.concurrent.Future

class ProductControllerSpec extends ControllerFixture {

  "ProductController#getById(id:Long)" should {

    "get a product successfully" in {

      //Mock response data
      val id = 1L
      val product: Product = Product(Some(1L), "Macbook", LocalDate.now().plusYears(1), BigDecimal.apply(1000))

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockProductService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(product)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/products/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK

      val resProduct: ProductResource = Json.fromJson[ProductResource](contentAsJson(result)).get
      verifyProduct(resProduct, product)
    }

    "get a product not found" in {

      //Mock response data
      val id = 2L

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockProductService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(None))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/products/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual NOT_FOUND
    }

  }

  "ProductController#getAll()" should {

    "get all product successfully" in {

      //Mock response data
      val product1: Product = Product(Some(1L), "Macbook", LocalDate.now().plusYears(1), BigDecimal.apply(1000))
      val product2: Product = Product(Some(2L), "Iphone 8", LocalDate.now().plusYears(1), BigDecimal.apply(1000))
      val products: Iterable[Product] = List(product1, product2)

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockProductService.listAll()).thenReturn(Future.successful(products))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/products")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val productsResult: Seq[ProductResource] = Json.fromJson[List[ProductResource]](contentAsJson(result)).get
      verifyProduct(productsResult.head, product1)
    }

  }

  private def verifyProduct(actual: ProductResource, expected: Product): Unit = {
    actual.id mustEqual expected.id.get
    actual.productName mustEqual expected.productName
    actual.expDate mustEqual expected.expDate
    actual.price mustEqual expected.price
  }
}
