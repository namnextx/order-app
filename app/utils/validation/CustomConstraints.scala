package utils.validation

import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.libs.json.Json

import java.time.LocalDate

object CustomConstraints {
  val dateInTheFuture: Constraint[LocalDate] = Constraint("constraints.dateInTheFuture") { date =>
    if (date.isAfter(LocalDate.now())) {
      Valid
    } else {
      Invalid("Date must be in the future")
    }
  }

  val roleValidate: Constraint[String] = Constraint("constraints.dateInTheFuture") { role =>
    if ("Operator".equals(role) || "User".equals(role)) {
      Valid
    } else {
      Invalid("Role of user must be Operator or User")
    }
  }

  val nonEmptyList: Constraint[List[Long]] = Constraint("constraints.nonEmptyList") { map =>
    if (map.nonEmpty) {
      Valid
    } else {
      Invalid("List must not be empty")
    }
  }

}
