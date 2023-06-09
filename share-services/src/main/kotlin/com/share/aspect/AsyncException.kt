package com.share.aspect

import com.share.domain.refresh.services.AsyncFailureService
import com.share.model.ResourceId
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

data class AsyncException(
    val message: String? = null,
    val cause: Throwable?
)

class CouchbaseUserNotFoundException(
    val resourceId: ResourceId,
) : BaseApiException(
    httpStatus = HttpStatus.NOT_FOUND,
    errorType = "not_found",
    message = "Unable to find user with resourceId: $resourceId"
)

abstract class BaseApiException(
    val httpStatus: HttpStatus,
    open val errorType: String,
    override val message: String
) : ResponseStatusException(httpStatus)
