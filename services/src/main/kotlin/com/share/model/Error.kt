package com.share.model

sealed class Error {
    sealed class EventStoreError : Error() {

        data class MError(
            val message: String
        ) : EventStoreError()
    }
}

