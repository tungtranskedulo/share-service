package com.share.http


data class MobileApiResponse<ResponseType>(
  val response: ResponseType?,
  val status: ResponseStatus,
  val error: ApiError? = null
): PublicAPIData
