package controllers.user

import controllers.post.PostResource
import domain.models.{Post, User}
import play.api.libs.json.{Json, OFormat}

import java.time.{LocalDate, LocalDateTime}

case class UserResource(id: Long,
                        email: String,
                        role: String,
                        lastName: String,
                        birthDate: LocalDate,
                        address: String,
                        phoneNumber: String)

object UserResource {

  /**
   * Mapping to read/write a PostResource out as a JSON value.
   */
  implicit val format: OFormat[UserResource] = Json.format[UserResource]

  def fromUser(user: User): UserResource =
    UserResource(user.id.getOrElse(-1), user.email, user.role, user.lastName, user.birthDate, user.address, user.phoneNumber)
}
