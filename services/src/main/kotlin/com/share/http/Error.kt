package com.share.http

import org.apache.logging.log4j.util.Strings


data class Error(
  val target: String = Strings.EMPTY,
  val errorCode: Int,
  val errorMessage: String,
  val errorDetail: String,
  val action: Map<String, Any>? = null
) {
  constructor(
    responseCode: ResponseCode
  ) : this(
    Strings.EMPTY,
    responseCode.responseCode,
    Strings.EMPTY,
    Strings.EMPTY
  )

  companion object {
    private const val CR_ERROR_MESSAGE_SUFFIX = "MESSAGE"
    private const val CR_ERROR_MESSAGE_DETAIL_SUFFIX = "MESSAGE_DETAIL"

    fun serverError(
      target: String,
      responseCode: ResponseCode,
      errorDetail: String
    ): Error {
      return Error(
        target = target,
        errorCode = responseCode.responseCode,
        errorMessage = responseCode.message,
        errorDetail = errorDetail
      )
    }


    fun serverError(responseCode: ResponseCode, errorDetail: String): Error {
      return Error(
        target = Strings.EMPTY,
        errorCode = responseCode.responseCode,
        errorMessage = responseCode.message,
        errorDetail = errorDetail
      )
    }

    fun badRequestError(fieldName: String): Error {
      return Error(
        target = fieldName,
        errorCode = ResponseCode.RC_400.responseCode,
        errorMessage = ResponseCode.RC_400.message,
        errorDetail = ResponseCode.RC_400.messageDetail
      )
    }

    fun notFoundError(errorDetail: String? = null): Error {
      return Error(
        target = Strings.EMPTY,
        errorCode = ResponseCode.RC_404.responseCode,
        errorMessage = ResponseCode.RC_404.message,
        errorDetail = errorDetail ?: ResponseCode.RC_404.messageDetail
      )
    }
  }
}
