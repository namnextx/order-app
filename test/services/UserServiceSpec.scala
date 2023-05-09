package services

import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import controllers.user.UserResource
import domain.dao.UserDao
import domain.models.User
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import java.time.LocalDate
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class UserServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures {

  val mockUserDao: UserDao = mock[UserDao]

  val userService: UserService = new UserServiceImpl(mockUserDao)(global)

  "UserService#find(id: Long)" should {
    "get a user by id successfully" in {
      val password: String = new BCryptPasswordHasher().hash("fakeP@ssw0rd").password
      val user: User = User(Some(2L), "user-admin@test.com", "User", "johnny" , "dev", Some(password), LocalDate.now, "Hanoi", "1234")
      when(mockUserDao.find(anyLong())).thenReturn(Future.successful(Some(user)))

      val result = userService.find(1L).futureValue
      result.isEmpty mustBe false
      val actual = result.get
      verifyUser(actual,user)
    }

    "get user by id not found" in {
      when(mockUserDao.find(anyLong())).thenReturn(Future.successful(None))

      val result = userService.find(1L).futureValue
      result.isEmpty mustBe true
    }
  }

  "UserService#listAll()" should {
    "get all users successfully" in {
      val password: String = new BCryptPasswordHasher().hash("fakeP@ssw0rd").password
      val user: User = User(Some(2L), "user-admin@test.com", "User", "johnny" , "dev", Some(password), LocalDate.now, "Hanoi", "1234")
      val users: Iterable[User] = List(user)
      when(mockUserDao.listAll()).thenReturn(Future.successful(users))

      val result = userService.listAll().futureValue
      result.isEmpty mustBe false
      val actual = result.head
      verifyUser(actual,user)
    }


    "get all users successfully empty" in {
      when(mockUserDao.listAll()).thenReturn(Future.successful(None))

      val result = userService.listAll().futureValue
      result.isEmpty mustBe true
    }
  }

  "UserService#save()" should {
    "save successfully" in {
      val password: String = new BCryptPasswordHasher().hash("fakeP@ssw0rd").password
      val user: User = User(Some(2L), "user-admin@test.com", "User", "johnny" , "dev", Some(password), LocalDate.now, "Hanoi", "1234")

      when(mockUserDao.save(user)).thenReturn(Future.successful(user))

      val actual = userService.save(user).futureValue

      verifyUser(actual,user)
    }
  }


  "UserService#update()" should {
    "update successfully" in {
      val password: String = new BCryptPasswordHasher().hash("fakeP@ssw0rd").password
      val user: User = User(Some(2L), "user-admin@test.com", "User", "johnny" , "dev", Some(password), LocalDate.now, "Hanoi", "1234")

      when(mockUserDao.update(user)).thenReturn(Future.successful(user))

      val actual = userService.update(user).futureValue

      verifyUser(actual,user)
    }
  }


  "UserService#delete()" should {
    "delete successfully" in {
      when(mockUserDao.delete(1L)).thenReturn(Future.successful(1))

      val actual = userService.delete(1L).futureValue

      actual mustEqual 1L
    }
  }

  private def verifyUser(actual: User, expected: User): Unit = {
    actual.id.get mustEqual expected.id.get
    actual.email mustEqual expected.email
    actual.role mustEqual expected.role
    actual.firstName mustEqual expected.firstName
    actual.lastName mustEqual expected.lastName
    actual.birthDate mustEqual expected.birthDate
    actual.address mustEqual expected.address
    actual.phoneNumber mustEqual expected.phoneNumber
  }

}
