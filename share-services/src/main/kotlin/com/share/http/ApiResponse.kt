package com.share.http

import java.util.*

//data class ApiResponse<S, Q>(
//  override val id: CBID,
//  override val resourceId: ResourceId,
//  override val tenantId: TenantId,
//  val status: ResponseStatus,
//  val responseCode: Int,
//  val response: S? = null,
//  val request: Q? = null,
//  val errors: List<Error>? = null,
//) : ModelForResource {
//
//  override val type = ApiResponse.type
//
//  companion object {
//    const val type = "RefreshData"
//    const val ID_SUFFIX = type
//  }
//}

data class ApiResponse<S, Q>(
  val id: String = UUID.randomUUID().toString(),
  val uid: String =UUID.randomUUID().toString(),
  val tenantId: String =UUID.randomUUID().toString(),
  val resourceId: String = UUID.randomUUID().toString(),
  val status: ResponseStatus,
  val responseCode: Int,
  val response: S? = null,
  val request: Q? = null,
  val errors: List<Error>? = null,
  val type: String = "refreshData",
) : PublicAPIData {
  companion object {
    fun <S, Q> successfulResult(
      request: Q,
      response: S? = null
    ) = ApiResponse(
      status = ResponseStatus.SUCCESS,
      responseCode = ResponseCode.RC_200.responseCode,
      response = response,
      request = request
    )

    fun <S, Q> ok(
      request: Q? = null,
      response: S? = null
    ) = ApiResponse(
      status = ResponseStatus.SUCCESS,
      responseCode = ResponseCode.RC_200.responseCode,
      response = response,
      request = request
    )

    fun <S, Q> errorResult(
      request: Q? = null,
      errors: List<Error>,
      response: S? = null,
      responseCode: Int = ResponseCode.RC_500.responseCode
    ) = ApiResponse(
      status = ResponseStatus.FAILED,
      responseCode = responseCode,
      response = response,
      request = request,
      errors = errors
    )

//    fun <ResponseType> error(error: ApiError, request: CustomAPIRequest<*>) = ApiResponse(
//      status = ResponseStatus.FAILED,
//      responseCode = 0,
//      response = null,
//      request = request,
//      errors = null
//    )

  }

}
