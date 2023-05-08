package exception

import play.api.data.Form
import play.api.libs.json.{JsObject, Json}

object ValidationError {
  def generateErrorResponse(badForm: Form[_]) = {
    val fieldErrors = badForm.errors.map { error =>
      Json.obj(
        "field" -> error.key,
        "message" -> error.message
      )
    }

    Json.obj(
      "status" -> "Validate Error",
      "errors" -> fieldErrors
    )

  }
}
