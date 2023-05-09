package controllers

import com.mohiva.play.silhouette.test._
import controllers.order.OrderResource
import domain.models._
import domain.models.dto.OrderWithDetails
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
      val orderDetail: OrderDetail = OrderDetail(Some(1L), Some(1L), Some(1L), 1, BigDecimal.apply(1000))
      val orderWithDetails: OrderWithDetails = OrderWithDetails(Some(1L), 1L, LocalDate.now(), BigDecimal.apply(1000), Seq(orderDetail))

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockOderService.findDetail(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(orderWithDetails)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/orders/$id")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK

      val resOrder: OrderWithDetails = Json.fromJson[OrderWithDetails](contentAsJson(result)).get
      verifyOrderWithDetail(resOrder, orderWithDetails)
    }

    "get an order successfully role User" in {

      //Mock response data
      val id = 2L
      val orderDetail: OrderDetail = OrderDetail(Some(1L), Some(2L), Some(2L), 1, BigDecimal.apply(1000))
      val orderWithDetails: OrderWithDetails = OrderWithDetails(Some(2L), 2L, LocalDate.now().plusDays(5),
        BigDecimal.apply(1000), Seq(orderDetail))

      when(mockUserService.retrieve(identityUser.loginInfo)).thenReturn(Future.successful(Some(identityUser)))
      when(mockOderService.findDetail(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(orderWithDetails)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/orders/$id")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identityUser.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK

      val resOrder: OrderWithDetails = Json.fromJson[OrderWithDetails](contentAsJson(result)).get
      verifyOrderWithDetail(resOrder, orderWithDetails)
    }

    "get an order not found with role User" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identityUser.loginInfo)).thenReturn(Future.successful(Some(identityUser)))
      when(mockOderService.findDetail(ArgumentMatchers.eq(id))).thenReturn(Future.successful(None))


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

  "OrderController#create()" should {

    "create an order bad request" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))

      // prepare test request
      val request = FakeRequest(POST, s"/v1/orders")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)
        .withFormUrlEncodedBody(
          "orderDate" -> "2020-10-10",
          "product[0].productId" -> "1l",
          "product[0].quantities" -> "2",
        )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual BAD_REQUEST
    }

    "create a order successfully" in {

      //Mock response data
      val product1: Product = Product(Some(1L), "Keyboad", LocalDate.now().plusYears(1), BigDecimal.apply(1000))
      val products: Seq[Product] = Seq(product1)
      val order: Order = Order(Some(1L), 1L, LocalDate.now().plusDays(5), BigDecimal.apply(1000))
      val orderDetail: OrderDetail = OrderDetail(Some(1L), Some(1L), Some(1L), 2, BigDecimal.apply(2000))
      val orderDetails: Seq[OrderDetail] = Seq(orderDetail)

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockProductService.findAllById(any[Seq[Long]])).thenReturn(Future.successful(products))
      when(mockOderDetailService.createOrderDetails(any[Seq[OrderDetail]])).thenReturn(Future.successful(orderDetails))
      when(mockOderService.save(any[Order])).thenReturn(Future.successful(order))
      when(mockOderService.update(any[Order])).thenReturn(Future.successful(order))


      // prepare test request
      val request = FakeRequest(POST, s"/v1/orders")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)
        .withFormUrlEncodedBody(
          "orderDate" -> "2024-10-10",
          "product[0].productId" -> "1",
          "product[0].quantities" -> "2",
        )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual CREATED
      val resOrder: OrderResource = Json.fromJson[OrderResource](contentAsJson(result)).get
      verifyOrder(resOrder, order)
    }

  }

  "OrderController#update()" should {

    "update an order bad request" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))

      // prepare test request
      val request = FakeRequest(PUT, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)
        .withFormUrlEncodedBody(
          "orderDate" -> LocalDate.now().minusDays(1).toString,
                "product[0].productId" -> "1",
                "product[0].quantities" -> "2",
        )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual BAD_REQUEST
    }

    "update an order successfully with role Admin" in {

      //Mock response data
      val id = 1L
      val order: Order = Order(Some(1L), 1L, LocalDate.now().plusDays(5), BigDecimal.apply(1000))

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockOderService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(order)))
      when(mockOderService.update(any[Order])).thenReturn(Future.successful(order))

      // prepare test request
      val request = FakeRequest(PUT, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)
        .withFormUrlEncodedBody(
          "orderDate" -> LocalDate.now().plusDays(3).toString,
          "product[0].productId" -> "1",
          "product[0].quantities" -> "2",
        )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
    }

    "update an order successfully not found" in {

      //Mock response data
      val id = 1L

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockOderService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(None))

      // prepare test request
      val request = FakeRequest(PUT, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)
        .withFormUrlEncodedBody(
          "orderDate" -> LocalDate.now().plusDays(3).toString,
          "product[0].productId" -> "1",
          "product[0].quantities" -> "2",
        )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual NOT_FOUND
    }

    "update an order successfully with role User" in {

      //Mock response data
      val id = 1L
      val order: Order = Order(Some(1L), 2L, LocalDate.now().plusDays(5), BigDecimal.apply(1000))

      when(mockUserService.retrieve(identityUser.loginInfo)).thenReturn(Future.successful(Some(identityUser)))
      when(mockOderService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(order)))
      when(mockOderService.update(any[Order])).thenReturn(Future.successful(order))

      // prepare test request
      val request = FakeRequest(PUT, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identityUser.loginInfo)
        .withFormUrlEncodedBody(
          "orderDate" -> LocalDate.now().plusDays(3).toString,
          "product[0].productId" -> "1",
          "product[0].quantities" -> "2",
        )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
    }

    "update an order successfully with role User but not owner" in {

      //Mock response data
      val id = 1L
      val order: Order = Order(Some(1L), 1L, LocalDate.now().plusDays(5), BigDecimal.apply(1000))

      when(mockUserService.retrieve(identityUser.loginInfo)).thenReturn(Future.successful(Some(identityUser)))
      when(mockOderService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(order)))
      when(mockOderService.update(any[Order])).thenReturn(Future.successful(order))

      // prepare test request
      val request = FakeRequest(PUT, s"/v1/orders/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identityUser.loginInfo)
        .withFormUrlEncodedBody(
          "orderDate" -> LocalDate.now().plusDays(3).toString,
          "product[0].productId" -> "1",
          "product[0].quantities" -> "2",
        )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual FORBIDDEN
    }

  }

  private def verifyOrder(actual: OrderResource, expected: Order): Unit = {
    actual.id mustEqual expected.id.get
  }

  private def verifyOrderWithDetail(actual: OrderWithDetails, expected: OrderWithDetails): Unit = {
    actual.id.get mustEqual expected.id.get
    actual.orderDetails mustEqual expected.orderDetails
  }
}
