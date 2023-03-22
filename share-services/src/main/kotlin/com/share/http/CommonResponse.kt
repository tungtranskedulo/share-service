package com.share.http


import org.apache.logging.log4j.util.Strings
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class CommonResponse {
  companion object {
    private fun <T, R> badRequest(
      request: R,
      errorField: String,
      errorDetail: String
    ): ResponseEntity<ApiResponse<T, R>> = ResponseEntity.badRequest()
      .body(
        ApiResponse.errorResult(
          request,
          errors = listOf(
            Error(
              errorField,
              ResponseCode.RC_400.responseCode,
              ResponseCode.RC_400.message,
              errorDetail
            )
          )
        )
      )

    fun <T, R> ok(
      request: R,
      response: T?
    ): ResponseEntity<ApiResponse<T, R>> = ResponseEntity.ok(
      ApiResponse.successfulResult(
        request,
        response
      )
    )

    fun <T, R> unauthorized(
      request: R,
      errorField: String,
      errorDetail: String
    ): ResponseEntity<ApiResponse<T, R>> =
      ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(
          ApiResponse.errorResult(
            request = request,
            errors = listOf(
              Error(
                errorField,
                ResponseCode.RC_400.responseCode,
                ResponseCode.RC_400.message,
                errorDetail
              )
            )
          )
        )

    fun <T, R> errorResult(
      request: R,
      errors: List<Error>,
      response: T? = null,
      responseCode: Int = ResponseCode.RC_500.responseCode,
      httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
    ) = ResponseEntity
      .status(httpStatus)
      .body(
        ApiResponse(
          status = ResponseStatus.FAILED,
          responseCode = responseCode,
          response = response,
          request = request,
          errors = errors
        )
      )

    fun <T, R> notFound(
      request: R,
      errorField: String,
      errorDetail: String
    ): ResponseEntity<ApiResponse<T, R>> =
      ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(
          ApiResponse.errorResult(
            request = request,
            errors = listOf(
              Error(
                errorField,
                ResponseCode.RC_404.responseCode,
                ResponseCode.RC_404.message,
                errorDetail
              )
            )
          )
        )

    private fun <T, R> internalServerError(
      request: R,
      errorField: String,
      errorDetail: String
    ): ResponseEntity<ApiResponse<T, R>> =
      ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
          ApiResponse.errorResult(
            request = request,
            errors = listOf(
              Error(
                errorField,
                ResponseCode.RC_500.responseCode,
                ResponseCode.RC_500.message,
                errorDetail
              )
            )
          )
        )

    fun <T, R> ofError(
      request: R,
      error: Error?
    ): ResponseEntity<ApiResponse<T, R>> {
      return error?.let {
        val errorCode = it.errorCode
        when {
          (errorCode.toString().startsWith(ResponseCode.RC_500.responseCode.toString())) -> {
            ResponseEntity
              .status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(
                ApiResponse.errorResult(
                  request = request,
                  errors = listOf(
                    Error(Strings.EMPTY, errorCode, it.errorMessage, it.errorDetail)
                  )
                )
              )
          }
          (errorCode.toString().startsWith(ResponseCode.RC_404.responseCode.toString())) -> {
            ResponseEntity
              .status(HttpStatus.NOT_FOUND)
              .body(
                ApiResponse.errorResult(
                  request = request,
                  errors = listOf(
                    Error(Strings.EMPTY, errorCode, it.errorMessage, it.errorDetail)
                  )
                )
              )
          }
          else -> {
            badRequest(request, it.errorMessage, it.errorDetail)
          }
        }
      } ?: internalServerError(request, ResponseCode.RC_500.message, ResponseCode.RC_500.messageDetail)
    }
  }
}
