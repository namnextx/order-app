package controllers

import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.test._
import domain.dao._
import domain.models._
import fixtures.TestApplication
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.Suite
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import services.{ExternalProductService, OrderService, ProductService, UserService}
import utils.auth.JWTEnvironment

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class ControllerFixture extends PlaySpec with Suite with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures {
  val mockUserService: UserService = mock[UserService]
  val mockProductService: ProductService = mock[ProductService]
  val mockOderService: OrderService = mock[OrderService]
  val mockExternalProductService: ExternalProductService = mock[ExternalProductService]
  val mockDaoRunner: DaoRunner = mock[DaoRunner]
  val mockUserDao: UserDao = mock[UserDao]
  val mockProductDao: ProductDao = mock[ProductDao]

  val password: String = new BCryptPasswordHasher().hash("fakeP@ssw0rd").password
  val identity: User = User(Some(1L), "user-admin@test.com", "Admin", "admin" , "user", Some(password), LocalDate.now, "Hanoi", "1234")
  val identityUser: User = User(Some(2L), "user-nomal@test.com", "User", "Johnny" , "Dev", Some(password), LocalDate.now, "Hanoi", "1234")
  val identityOperator: User = User(Some(3L), "user-operator@test.com", "Operator", "Johnny" , "Dev1", Some(password), LocalDate.now, "Hanoi", "1234")
  implicit val env: Environment[JWTEnvironment] = new FakeEnvironment[JWTEnvironment](Seq(
    identity.loginInfo -> identity,
    identityUser.loginInfo -> identityUser,
    identityOperator.loginInfo -> identityOperator
  ))

  class FakeServiceModule extends AbstractModule with ScalaModule {
    override def configure(): Unit = {
      bind[Environment[JWTEnvironment]].toInstance(env)
      bind[UserService].toInstance(mockUserService)
      bind[OrderService].toInstance(mockOderService)
      bind[ExternalProductService].toInstance(mockExternalProductService)
      bind[ProductService].toInstance(mockProductService)
      bind[DaoRunner].toInstance(mockDaoRunner)
      bind[UserDao].toInstance(mockUserDao)
      bind[ProductDao].toInstance(mockProductDao)
    }
  }

  implicit override lazy val app: Application = TestApplication.appWithOverridesModule(module = new FakeServiceModule())
}
