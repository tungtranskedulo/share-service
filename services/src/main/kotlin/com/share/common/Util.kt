package com.share.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

object Util {

    suspend fun <A, B> Iterable<A>.pmap(limitConcurrentCoroutine: Int, f: suspend (A) -> B): List<B> = coroutineScope {
        //if (this@pmap.count() == 0) return@coroutineScope emptyList()
        val requestSemaphore = Semaphore(limitConcurrentCoroutine)
        map { async(Dispatchers.IO) { requestSemaphore.withPermit { f(it) }  } }.awaitAll()
    }

}
