package com.share.http

const val INTERNAL_SERVER_ERROR = "Internal Server Error"
const val INVALID_REQUEST = "Invalid Request"

enum class ResponseCode(
  val responseCode: Int,
  val message: String,
  val messageDetail: String
) {
  RC_200(200, "Success", "Request has been processed successfully"),
  RC_400(400, INVALID_REQUEST, "Request parameters were invalid"),
  RC_401(401, "Unauthorized", "You are not authorized to access the resource"),
  RC_403(403, "User is not authorized", "Please login to access this feature."),

  //Common
  RC_404(404, "Not found", "Data related to provided parameters not found"),
  RC_500(500, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR),
}
