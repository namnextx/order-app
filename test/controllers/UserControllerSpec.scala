package controllers

import com.mohiva.play.silhouette.test._
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

class UserControllerSpec extends ControllerFixture {

  "UserController#getById(id:Long)" should {

    "get an user successfully" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockUserService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(Some(identity)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/users/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val resUser: UserResource = Json.fromJson[UserResource](contentAsJson(result)).get
      verifyUser(resUser, identity)
    }

    "get an user not found" in {

      //Mock response data
      val id = 2L

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockUserService.find(ArgumentMatchers.eq(id))).thenReturn(Future.successful(None))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/users/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual NOT_FOUND
    }

  }

  "UserController#getAll()" should {

    "get all users successfully" in {

      //Mock response data
      val user: User = User(Some(2L), "user-admin@test.com", "User", "johnny" , "dev", Some(password), LocalDate.now, "Hanoi", "1234")
      val users: Iterable[User] = List(identity, user)

      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockUserService.listAll()).thenReturn(Future.successful(users))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/v1/users")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val usersResult: Seq[UserResource] = Json.fromJson[List[UserResource]](contentAsJson(result)).get
      verifyUser(usersResult.head, identity)

    }

  }

  "UserController#delete(id:Long)" should {

    "delete an user successfully" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockUserService.delete(ArgumentMatchers.eq(id))).thenReturn(Future.successful(1))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, s"/v1/users/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
    }

    "delete an user bad request" in {

      //Mock response data
      val id = 1L
      when(mockUserService.retrieve(identity.loginInfo)).thenReturn(Future.successful(Some(identity)))
      when(mockUserService.delete(ArgumentMatchers.eq(id))).thenReturn(Future.successful(0))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, s"/v1/users/${id}")
        .withHeaders(HOST -> "localhost:8080")
        .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual BAD_REQUEST
    }

  }

  private def verifyUser(actual: UserResource, expected: User): Unit = {
    actual.id mustEqual expected.id.get
    actual.email mustEqual expected.email
    actual.role mustEqual expected.role
    actual.firstName mustEqual expected.firstName
    actual.lastName mustEqual expected.lastName
    actual.birthDate mustEqual expected.birthDate
    actual.address mustEqual expected.address
    actual.phoneNumber mustEqual expected.phoneNumber
  }

}
