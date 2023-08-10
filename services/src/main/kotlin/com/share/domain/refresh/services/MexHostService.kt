package com.share.domain.refresh.services

import com.share.aspect.CoroutineLogExecutionTime
import com.share.cache.MexDefinition
import com.share.cache.MexStaticData
import com.share.cache.MexStaticDataCache
import com.share.common.Util.pmap
import com.share.model.CBID
import com.share.model.ObjectId
import com.share.model.TenantId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class MexHostService(
    private val mexStaticDataCache: MexStaticDataCache
) {

    @CoroutineLogExecutionTime
    suspend fun fetchStaticData(): List<MexStaticData> {
        val mexStatic = mockMexDefRepo().toList().pmap(10) {
            mexStaticDataCache.get(params = arrayOf(it))
        }

        return mexStatic.filterNotNull().also { staticData ->
            log.info { "mexStatic $staticData" }
        }
    }

    private suspend fun mockMexDefRepo(): Flow<MexDefinition> {
        return flowOf(
            MexDefinition(
                CBID.build("tenantId1"),
                TenantId("tenantId1"),
                ObjectId("123"),
                "name",
                "customFunction"
            )
        )
    }
}
