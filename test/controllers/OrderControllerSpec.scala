package controllers

import com.mohiva.play.silhouette.test._
import controllers.order.OrderResource
import controllers.product.ProductResource
import domain.models._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.FORBIDDEN
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import play.api.test._
import utils.auth.JWTEnvironment

import java.time.LocalDate
import scala.concurrent.Future

class OrderControllerSpec extends ControllerFixture {

  "OrderController#getById(id:Long)" should {

    "get an order successfully role Admin" in {

      //Mock response data
      val id = 1L
      val order: Order = Order(Some(1L), 1L, LocalDate.now().plusDays(5), BigDecimal.apply(1000))

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockOderService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(order)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK

      val resOrder: OrderResource = Json.fromJson[OrderResource](contentAsJson(result)).get
      verifyOrder(resOrder, order)
    }

    "get an order successfully role User" in {

      //Mock response data
      val id = 2L
      val order: Order = Order(Some(2L), 2L, LocalDate.now().plusDays(5), BigDecimal.apply(1000))

      when(mockUserService.retrieve(identityUser.loginInfo)).thenReturn(Future.successful(Some(identityUser)))
      when(mockOderService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(order)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identityUser.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK

      val resOrder: OrderResource = Json.fromJson[OrderResource](contentAsJson(result)).get
      verifyOrder(resOrder, order)
    }

    "get an order not found with role User" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identityUser.loginInfo)).thenReturn(Future.successful(Some(identityUser)))
      when(mockOderService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(None))


      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identityUser.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual NOT_FOUND
    }

    "get an order not found with role Operator" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identityOperator.loginInfo)).thenReturn(Future.successful(Some(identityOperator)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identityOperator.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual FORBIDDEN
    }

  }

  "OrderController#getAll()" should {

    "get all orders successfully role Admin" in {

      //Mock response data
      val order: Order = Order(Some(1L), 1L, LocalDate.now().plusDays(5), BigDecimal.apply(1000))
      val orders: Iterable[Order] = List(order)

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockOderService.listAll()).thenReturn(Future.successful(orders))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/orders")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val productsResult: Seq[OrderResource] = Json.fromJson[List[OrderResource]](contentAsJson(result)).get
      verifyOrder(productsResult.head, order)
    }

    "get all orders successfully role User" in {

      //Mock response data
      val order: Order = Order(Some(1L), 1L, LocalDate.now().plusDays(5), BigDecimal.apply(1000))
      val orders: Iterable[Order] = List(order)

      when(mockUserService.retrieve(identityUser.loginInfo)).thenReturn(Future.successful(Some(identityUser)))
      when(mockOderService.getOrdersByUserId(ArgumentMatchers.eq(identityUser.id.get))).thenReturn(Future.successful(orders))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/orders")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identityUser.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val productsResult: Seq[OrderResource] = Json.fromJson[List[OrderResource]](contentAsJson(result)).get
      verifyOrder(productsResult.head, order)
    }

  }

  "OrderController#delete(id:Long)" should {

    "delete an order successfully role Admin" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockOderService.delete(ArgumentMatchers.eq(id))).thenReturn(Future.successful(1))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
    }

    "delete an order bad request role Admin" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockOderService.delete(ArgumentMatchers.eq(id))).thenReturn(Future.successful(0))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual BAD_REQUEST
    }

    "delete an order successfully role User" in {

      //Mock response data
      val id = 1L
      val order: Order = Order(Some(1L), 2L, LocalDate.now().plusDays(5), BigDecimal.apply(1000))

      when(mockUserService.retrieve(identityUser.loginInfo)).thenReturn(Future.successful(Some(identityUser)))
      when(mockOderService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(order)))
      when(mockOderService.delete(ArgumentMatchers.eq(id))).thenReturn(Future.successful(2))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identityUser.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
    }

    "delete an order bad request role User" in {

      //Mock response data
      val id = 1L
      val order: Order = Order(Some(1L), 2L, LocalDate.now().plusDays(5), BigDecimal.apply(1000))
      when(mockUserService.retrieve(identityUser.loginInfo)).thenReturn(Future.successful(Some(identityUser)))
      when(mockOderService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(order)))
      when(mockOderService.delete(ArgumentMatchers.eq(id))).thenReturn(Future.successful(0))


      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identityUser.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual BAD_REQUEST
    }

    "delete an order return bad request with role User and not owner" in {

      //Mock response data
      val id = 1L
      val order: Order = Order(Some(1L), 1L, LocalDate.now().plusDays(5), BigDecimal.apply(1000))
      when(mockUserService.retrieve(identityUser.loginInfo)).thenReturn(Future.successful(Some(identityUser)))
      when(mockOderService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(order)))
      when(mockOderService.delete(ArgumentMatchers.eq(id))).thenReturn(Future.successful(0))


      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identityUser.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual FORBIDDEN
    }

  }
  /*
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

     }*/

  private def verifyOrder(actual: OrderResource, expected: Order): Unit = {
    actual.id mustEqual expected.id.get
  }
}
