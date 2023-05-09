package controllers

import com.mohiva.play.silhouette.test._
import controllers.product.{ProductFormInputCreate, ProductResource}
import domain.models._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
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

  "ProductController#delete(id:Long)" should {

    "delete a product successfully" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockProductService.delete(ArgumentMatchers.eq(id))).thenReturn(Future.successful(1))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, s"/v1/products/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
    }

    "delete a product bad request" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockProductService.delete(ArgumentMatchers.eq(id))).thenReturn(Future.successful(0))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, s"/v1/products/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual BAD_REQUEST
    }

  }

  "ProductController#create()" should {

    "create a product bad request" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))

      val formData = Map(
        "productName" -> Seq("Macbook"),
        "expDate" -> Seq(LocalDate.now().minusDays(1)),
      "price" -> Seq("9.99")
      )

      // prepare test request
      val request = FakeRequest(POST, s"/v1/products")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)
        .withFormUrlEncodedBody(
          "productName" -> "Macbook",
          "expDate" -> LocalDate.now().minusDays(1).toString,
          "price" -> "9.99"
        )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual BAD_REQUEST
    }

    "create a product successfully" in {

      //Mock response data

      val product: Product = Product(Some(1L), "Macbook", LocalDate.now().plusDays(3), BigDecimal.apply(9.99))
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockProductService.save(any[Product])).thenReturn(Future.successful(product))


      // prepare test request
      val request = FakeRequest(POST, s"/v1/products")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)
        .withFormUrlEncodedBody(
          "productName" -> "Macbook",
          "expDate" -> LocalDate.now().plusDays(3).toString,
          "price" -> "9.99"
        )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual CREATED
      val resProduct: ProductResource = Json.fromJson[ProductResource](contentAsJson(result)).get
      verifyProduct(resProduct,product)
    }

  }

  "ProductController#update()" should {

    "update a product bad request" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))

      // prepare test request
      val request = FakeRequest(PUT, s"/v1/products/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)
        .withFormUrlEncodedBody(
          "expDate" -> LocalDate.now().minusDays(1).toString,
                "price" -> "9.99"
        )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual BAD_REQUEST
    }

    "update a product successfully" in {

      //Mock response data
      val id = 1L
      val product: Product = Product(Some(1L), "Macbook", LocalDate.now().plusDays(3), BigDecimal.apply(9.99))
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockProductService.find(any[Long])).thenReturn(Future.successful(Some(product)))
      when(mockProductService.update(any[Product])).thenReturn(Future.successful(product))


      // prepare test request
      val request = FakeRequest(PUT, s"/v1/products/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)
        .withFormUrlEncodedBody(
          "expDate" -> LocalDate.now().plusDays(3).toString,
                "price" -> "9.99"
        )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
    }

  }

  "ProductController#getAllExternal()" should {

    "get all product successfully" in {

      //Mock response data
      val product1: Product = Product(Some(1L), "Keyboad", LocalDate.now().plusYears(1), BigDecimal.apply(1000))
      val products: Iterable[Product] = List(product1)

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockExternalProductService.listAll()).thenReturn(Future.successful(products))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/external/products")
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
    1 mustEqual expected.id.get
    actual.productName mustEqual expected.productName
    actual.expDate mustEqual expected.expDate
    actual.price mustEqual expected.price
  }
}
