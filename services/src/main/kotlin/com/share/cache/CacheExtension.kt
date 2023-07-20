package com.share.cache

import com.github.benmanes.caffeine.cache.Cache

@Suppress("UNCHECKED_CAST")
fun <K, V> Cache<K, Any>.cachedBy(
    key: K,
    mergeValue: Any? = null,
    onCacheExceptionBlock: ((Exception) -> Exception)? = null,
    block: () -> V?
): V? {
    try {
        val cachedValue: V? = this.getIfPresent(key) as V?
        if (cachedValue != null) {
            if (mergeValue != null) {
                if (cachedValue is Collection<*>) {
                    return (cachedValue + mergeValue).also {
                        this.put(key, it)
                    } as V
                }
            }
            return cachedValue
        }
    } catch (e: Exception) {
        onCacheExceptionBlock?.invoke(e)
        return block()
    }

    return block()?.also {
        this.put(key, it)
    }
}
