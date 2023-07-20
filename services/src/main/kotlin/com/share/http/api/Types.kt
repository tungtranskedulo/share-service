package com.share.http.api

//
// Base types for API responses
//
sealed class ApiResult<T>
data class ApiSuccessResult<T>(val result: T) : ApiResult<T>()

fun <T :Any> T.toApiSuccessResult(): ApiResult<T> = ApiSuccessResult(result=this)
