package com.share.cache

import com.share.config.JsonObject
import com.share.model.CBID
import com.share.model.ObjectId
import com.share.model.TenantId
import mu.KotlinLogging
import org.springframework.stereotype.Component

data class MexStaticData(
    val id: CBID,
    val tenantId: TenantId,
    val packageId: ObjectId,
    val data: String
) {

    companion object {
        const val type = "MexStaticData"
        const val ID_SUFFIX = type
    }
}

data class MexDefinition(
    val id: CBID,
    val tenantId: TenantId,
    val uid: ObjectId,
    val name: String,
    val customFunction: String?
) {
    val type = MexDefinition.type

    companion object {
        const val type = "MexDefinition"
    }
}

private val log = KotlinLogging.logger {}

@Component
class MexStaticDataCache(
) : CouchbaseCache<MexStaticData?>() {

    override suspend fun get(
        persistExecution: suspend (MexStaticData?) -> Any,
        forceRefresh: Boolean,
        params: Array<out Any?>
    ): MexStaticData? {
        val mexDefinition: MexDefinition? = params.getOrNull(0) as? MexDefinition

        return mexDefinition?.let {
            cacheByTenantActivity(
                { getStaticData(mexDefinition) },
                persistExecution,
                forceRefresh,
                arrayOf(mexDefinition.tenantId, mexDefinition.uid.value)
            )
        }
    }

    private suspend fun getStaticData(mexDefinition: MexDefinition): MexStaticData? {
        return try {
            with(mexDefinition) {
                MexStaticData(
                    id = CBID.build(tenantId.value, uid.toString()),
                    tenantId = tenantId,
                    packageId = uid,
                    data = mockNativeFormClient()
                )
            }
        } catch (e: Exception) {
            log.error { "Can not fetch static data for: Tenant - ${mexDefinition.tenantId}, MexDef: ${mexDefinition.uid}" }
            null
        }
    }

    private suspend fun mockNativeFormClient(): String {
        throw Exception("mockNativeFormClient")
        //return "mockNativeFormClient"
    }
}
