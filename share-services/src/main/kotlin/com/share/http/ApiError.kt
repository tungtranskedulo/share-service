package com.share.http


class ApiError (
  val code: Int,
  val message: String,
  val detail: String,
  val action: Map<String, String>? = null
) {
  constructor(responseCode: ResponseCode) : this(
    responseCode.responseCode,
    responseCode.message,
    responseCode.messageDetail,
    null
  )

  constructor(responseCode: ResponseCode, ex: Exception): this(
    responseCode.responseCode,
    responseCode.message,
    ex.localizedMessage ?: responseCode.messageDetail,
    null
  )
}
