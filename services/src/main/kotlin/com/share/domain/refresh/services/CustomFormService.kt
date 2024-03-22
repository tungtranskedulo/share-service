package com.share.domain.refresh.services

import arrow.core.Either
import arrow.core.fold
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.share.ApiClient
import com.share.config.objectMapper
import com.share.config.readValue
import com.share.config.toJsonObj
import com.share.aspect.CoroutineLogExecutionTime
import com.share.config.AuthIdToken
import com.share.config.JsonArray
import com.share.config.JsonObject
import com.share.config.emptyJsonArrayObject
import com.share.config.emptyJsonObject
import com.share.config.fromJson
import com.share.http.RetryConfiguration
import com.share.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class CustomFormService(
    @Value("\${skedulo.app.config.limit-concurrent-coroutine}")
    private val limitConcurrentCoroutine: Int,
    private val apiCLient: ApiClient,
    private val authIdToken: AuthIdToken,
) {
    companion object {
        const val UID = "UID"
        const val managedSource = "__managedSource"
        const val dynamicUIDPrefix = "dynamic_"
    }

    @CoroutineLogExecutionTime
    suspend fun getCustomForm(forms: List<String>? = emptyList()) {
        apiCLient.fetchCustomFormData(emptyJsonObject()).let {
            log.info { "Fetching form $it" }
        }
    }

    suspend fun updateToken(tokenId: String) : AuthIdToken {
        authIdToken.setIdToken(tokenId)

        log.info { "Updating token $tokenId" }
        return authIdToken
    }

    @CoroutineLogExecutionTime
    //@WithTimeout(timeout = 1, unit = TimeUnit.SECONDS)
    suspend fun getCustomForm2(a: String, b: Int): Map<String, Any> {
        log.info { "Fetching custom form2" }
        return mapOf("a" to "b")
    }

    suspend fun processEventsForDevice() {
        log.info { "Processing events for device " }
        var process = true
        while (process) {
            val lockResult = processOneEvent()
            when (lockResult) {
                ShouldContinue.STOP -> {
                    log.info { "Done processing events" }
                    process = false
                }

                else -> {}
            }
        }
        log.info { "exiting process Events" }
    }


    @Suppress("UNCHECKED_CAST")
    suspend fun saveCustomFormData(formRevId: String, saveObject: JsonObject): Any {
        val postInstanceData = postInstanceData()
        val existUIDs = mutableListOf<Pair<String, String>>()
        val existObjectIdMapping = findAllObjectIdMapping()
        var newDataRequest: List<DataRequest>

        val saveObjectGroupByUID = postInstanceData.extractContextObjectData(
            ObjectId("a0P5f00001IJB8uEAH")
        ).let {
            it.groupByDynamicUID(existObjectIdMapping)
        }

        val extractStoreObject = saveObjectGroupByUID.let { context ->
            val storeObjects = mutableListOf<StoreObject>()
            newDataRequest = context.filter {
                it.uid.startsWith(dynamicUIDPrefix)
            }.toDataRequest()
            newDataRequest.map { request ->
                if (request.data.isNotEmpty()) {
                    storeObjects.add(
                        StoreObject(
                            ManageSource.CREATE,
                            request.tempIds,
                            postInstanceData.replaceValuesByKeys(request.data)
                        )
                    )
                }
            }

            context.filter {
                it.uid == "__updateSource"
            }.associate { node ->
                node.key to node.data
            }.also { updateObject ->
                if (updateObject.isNotEmpty()) {
                    storeObjects.add(
                        StoreObject(
                            manageSource = ManageSource.UPDATE,
                            data = postInstanceData.replaceValuesByKeys(updateObject)
                        )
                    )
                }
            }

            storeObjects
        }


        var mock = 0
        val mappingObjectIdMapping = mutableListOf<ObjectIdMapping>()
        val savedObjects = extractStoreObject.let { storeObj ->
            existUIDs.addAll(
                getCustomFormInstanceDataFromCB()
                    .extractContextObjectData(ObjectId("a0P5f00001IJB8uEAH"))
                    .getUID().toMutableList()
            )
            log.info { "existUIDs1 ${existUIDs.map { p -> "${p.first} - ${p.second}" }}}" }

            storeObj.map { obj ->
                mock++
                val fetchObject = saveToCondenserService(mock)
                log.info { "request - ${obj.data} to condenser" }
                log.info { "response $fetchObject to condenser" }

                if (obj.manageSource == ManageSource.CREATE) {
                    fetchObject.extractContextObjectData(ObjectId("a0P5f00001IJB8uEAH")).forEach { (key, value) ->
                        obj.tempIds.firstOrNull() { it.first == key }?.second?.let { tempId ->
                            value.createObjectIdMapping(
                                key,
                                tempId,
                                existUIDs.filter { it.first == key }.map { it.second }
                            )?.let {
                                existUIDs.add(Pair(it.key, it.objectId.value))
                                mappingObjectIdMapping.add(it)
                            }
                        }
                    }
                }

                fetchObject
            }
        }

        return mapOf(
            "groupByDynamicUID" to saveObjectGroupByUID,
            "newDataRequest" to newDataRequest,
            "extractStoreObject" to extractStoreObject,
            "savedObjects" to savedObjects,
            "mappingObjectIdMapping" to mappingObjectIdMapping
        )
    }

    private suspend fun List<ContextArrayNode>.toDataRequest(): List<DataRequest> {
        val result = mutableListOf<DataRequest>()
        val groupedData = this.groupBy { it.key }
        val maxElementCount = groupedData.values.maxOfOrNull { it.size } ?: 0

        for (i in 0 until maxElementCount) {
            val map = mutableMapOf<String, JsonArray>()
            val tempIds = mutableListOf<Pair<String, String>>()

            for ((key, values) in groupedData) {
                log.info { "key $key" }
                if (i < values.size) {
                    map[key] = values[i].data
                    tempIds.add(key to values[i].uid)
                } else {
                    map[key] = emptyJsonArrayObject()
                }
            }

            result.add(DataRequest(map.toMap(), tempIds))
        }

        return result
    }


    private suspend fun Map<String, JsonArray>.getUID(): List<Pair<String, String>> {
        return this.flatMap { (key, jsonArray) ->
            jsonArray.mapNotNull {
                it[UID]?.asText()?.let { uid ->
                    key to uid
                }
            }
        }
    }

    private suspend fun JsonObject.extractContextObjectData(contextObjectId: ObjectId): Map<String, JsonArray> {
        val result = mutableMapOf<String, JsonArray>()
        for (fieldName in this.fieldNames()) {
            if (this.has(contextObjectId.value)) {
                this.get(contextObjectId.value).fields().forEach { (key, value) ->
                    if (value is JsonArray) {
                        result[key] = value
                    }
                }
            } else {
                val nextNode = this.get(fieldName)
                if (nextNode is JsonObject) {
                    return nextNode.extractContextObjectData(contextObjectId)
                }
            }
        }
        return result
    }

    private suspend fun Map<String, JsonArray>.createObjectIdMapping(
        temporaryIds: List<Pair<String, String>>,
        existKeys: List<Pair<String, String>>
    ): List<ObjectIdMapping> {
        val mapping = mutableListOf<ObjectIdMapping>()
        this.forEach() { (key, value) ->
            value.createObjectIdMapping(
                key,
                temporaryIds.first { it.first == key }.second,
                existKeys.filter { it.first == key }.map { it.second }
            )?.let {
                mapping.add(it)
            }
        }
        return mapping
    }

    private suspend fun JsonArray.createObjectIdMapping(
        key: String,
        temporaryId: String,
        existKeys: List<String>,
    ): ObjectIdMapping? {
        val mapping = this.filterNot {
            it[UID]?.asText() in existKeys
        }
        return mapping.firstOrNull()?.get(UID)?.asText()?.let { uid ->
            ObjectIdMapping(
                key = key,
                temporaryId = ObjectId(temporaryId),
                objectId = ObjectId(uid),
            )
        }
    }


    private suspend fun Map<String, JsonArray>.groupByDynamicUID(existObjectIdMapping: List<ObjectIdMapping>): List<ContextArrayNode> {
        val contextArray = mutableListOf<ContextArrayNode>()

        this.mapValues { (key, value) ->
            val updateObject = objectMapper.createArrayNode()

            value.forEach { node ->
                node[UID]?.asText()?.let { uid ->
                    if (uid.startsWith(dynamicUIDPrefix)) {
                        existObjectIdMapping.find { it.temporaryId.value == uid }?.let {
                            when (node[managedSource]?.asInt()) {
                                ManageSource.DELETE.value -> {
                                    updateObject.add(
                                        (node as ObjectNode).replaceValuesByKeys(
                                            mapOf<String, Any>(
                                                UID to it.objectId.value,
                                            )
                                        )
                                    )
                                }

                                ManageSource.CREATE.value -> {
                                    updateObject.add(
                                        (node as ObjectNode).replaceValuesByKeys(
                                            mapOf<String, Any>(
                                                UID to it.objectId.value,
                                                managedSource to ManageSource.UPDATE.value
                                            )
                                        )
                                    )
                                }

                                else -> {}
                            }
                        } ?: run {
                            contextArray.add(
                                ContextArrayNode(
                                    key,
                                    node[UID].asText(),
                                    objectMapper.createArrayNode().add(node)
                                )
                            )
                        }
                    } else {
                        updateObject.add(node)
                    }
                }
            }
            if (!updateObject.isEmpty){ contextArray.add(
                ContextArrayNode(
                    key,
                    "__updateSource",
                    updateObject
                )
            )}
        }

        return contextArray
    }

    private fun JsonObject.replaceArrayNode(
        replacementArrayNode: ArrayNode
    ): ObjectNode {
        val jsonFactory = objectMapper.factory
        val originalNode = this

        return jsonFactory.toJsonObj().objectNode().apply {
            originalNode.fields().forEach { (key, value) ->
                set<ObjectNode>(
                    key, when (value) {
                        is ObjectNode -> value.replaceArrayNode(replacementArrayNode)
                        is ArrayNode -> {
                            replacementArrayNode
                        }

                        else -> value
                    }
                )
            }
        }
    }


    fun ObjectNode.replaceValuesByKeys(map: Map<String, Any>): ObjectNode {
        return objectMapper.createObjectNode().apply {
            this@replaceValuesByKeys.fields().forEach { (k, v) ->
                set<ObjectNode>(
                    k, when {
                        map.containsKey(k) -> {
                            when (map[k]) {
                                is String -> TextNode(map[k].toString())
                                is Int -> IntNode(map[k] as Int)
                                is ArrayNode -> map[k] as ArrayNode
                                is ObjectNode -> map[k] as ObjectNode
                                else -> v
                            }
                        }

                        v is ObjectNode -> v.replaceValuesByKeys(map)
                        v is ArrayNode -> v.replaceValuesByKeys(map)
                        else -> v
                    }
                )
            }
        }
    }

    fun ArrayNode.replaceValuesByKeys(map: Map<String, Any>): ArrayNode {
        return objectMapper.createArrayNode().apply {
            this@replaceValuesByKeys.forEach { element ->
                add(if (element is ObjectNode) element.replaceValuesByKeys(map) else element)
            }
        }
    }


    private suspend fun JsonArray.getJobProductDynamicUID(): List<Pair<ObjectId, ObjectId>> {
        val mappingRecord = mutableListOf<Pair<ObjectId, ObjectId>>()

        forEach { jobProduct ->
            val uid = jobProduct["UID"]?.asText()
            if (uid != null && uid.startsWith("dynamic_")) {
                mappingRecord.add(
                    Pair(
                        ObjectId(uid),
                        ObjectId(
                            jobProduct[""]?.asText() ?: throw IllegalStateException("ProductId is null")
                        )
                    )
                )
            }
        }
        return mappingRecord
    }

    fun JsonObject.filterAndGroupProductsByDynamicUID(contextObjectId: ObjectId): List<JsonObject> {
        val originalNode = this
        val result = mutableListOf<JsonNode>()

        originalNode.fields().forEach { (key, rootField) ->
            log.info { "originalNode key $key" }
            rootField.fields().forEach { (objectId, objectNode) ->
                log.info { "objectId $objectId" }
                val productsNode = objectNode["jobProducts"]

                val otherProducts = ObjectMapper().createArrayNode()

                productsNode?.forEach { product ->
                    if (product["UID"].textValue().startsWith("dynamic_")) {
                        val dynamicProducts = ObjectMapper().createArrayNode()
                        dynamicProducts.add(product)

                        val resultObject = ObjectMapper().createObjectNode()
                        resultObject.set<ObjectNode>(key, ObjectMapper().createObjectNode().apply {
                            set<ObjectNode>(
                                contextObjectId.value,
                                ObjectMapper().createArrayNode().addAll(dynamicProducts)
                            )
                        })
                        result.add(resultObject)
                    } else {
                        otherProducts.add(product)
                    }
                }

                if (otherProducts.size() > 0) {
                    val resultObject = ObjectMapper().createObjectNode()
                    resultObject.set<ObjectNode>(key, ObjectMapper().createObjectNode().apply {
                        set<ObjectNode>(contextObjectId.value, ObjectMapper().createArrayNode().addAll(otherProducts))
                    })
                    result.add(resultObject)
                }
            }
        }

        return result.map { it as JsonObject }
    }


    suspend fun processOneEvent(): ShouldContinue {
        return try {
            //val sequence = 1
            val nextEvent = getNextEvent().orNull()

            if (nextEvent == null) {
                log.info { "No more events to process." }
                ShouldContinue.STOP
            } else {
                //ShouldContinue.CONTINUE
                ShouldContinue.STOP
            }
        } catch (t: Throwable) {
            log.warn(t) { "Error in processing events" }
            ShouldContinue.STOP
        }
    }


    suspend fun getNextEvent(): Either<Error, ClientEvent?> {
        val nextEventList = getNextEventList()

        return if (nextEventList.isEmpty()) {
            null.right()
        } else {
            val result = getClientEventFromQueryResult(nextEventList[0])
            log.info { "Found event $result" }
            result.right()
        }
    }

    suspend fun getNextEventList(): List<NextEventQueryResult> {

        val nextEventList = listOf(
            NextEventQueryResult(
                "1",
                objectMapper.readValue("""{"clientSequence":24,"data":{"isDraft":false,"objectId":"00141632-52f0-4e9c-a269-36d829155ca6","operation":"Updated","postChangeData":{"customFields":{"custom_boolean":false}},"preChangeData":{"customFields":{"custom_boolean":true}},"schema":"Resources","type":"Write","typeHint":"Resources","writeEventAction":"CustomField"},"deviceIdentifier":"450C72BE-B6FD-46A5-91B6-F620807FAEF4","eventSchemaVersion":1,"isDraft":false,"projectionId":"sk_f68824f0fd34429eab6861d610578779|00141632-52f0-4e9c-a269-36d829155ca6","resourceId":"00053e7f-2057-4959-88a6-8c1378d3c85f","sessionIdentifier":"EA953FC7-B398-41ED-A70F-66C36E490667","tenantId":"sk_f68824f0fd34429eab6861d610578779","timeWritten":"2023-05-05T08:11:33.797Z","type":"ClientEvent","uid":"36633b80-b30b-4293-bee5-2d18b819f880"}""")
            )
        )

        return nextEventList.ifEmpty {
            emptyList()
        }
    }

    suspend fun getClientEventFromQueryResult(
        result: NextEventQueryResult
    ): ClientEvent {
        val evJson = result.eventJson
        val event = if (evJson.get("eventId") == null) {
            evJson.put("eventId", result.id)
        } else {
            evJson
        }
        return try {
            objectMapper.convertValue(event, ClientEvent::class.java)
        } catch (error: Throwable) {
            val fields: Iterator<MutableMap.MutableEntry<String, JsonNode>> = event.fields()
            while (fields.hasNext()) {
                val field = fields.next()
                if (field.key == "data") {
                    val data = ClientEvent.Data.MalFormedData(
                        detailedMessage = error.localizedMessage,
                        causeFields = error.cause?.cause?.stackTrace?.get(0)?.className?.split(".")?.last() ?: "Unknown"
                    )
                    field.setValue(data.toJsonObj())
                }
            }
            objectMapper.convertValue(event, ClientEvent::class.java)
        }
    }

    @CoroutineLogExecutionTime
    suspend fun fetchAllInstanceDataForJob(): List<MexInstanceData> {
        val fetchData = findAll().pmap {
            getMexFetchData(it.id)
        }
        delay(3000)

        return fetchData.filterNotNull().toList()
    }

    suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
        val requestSemaphore = Semaphore(FetchParallel.limitConcurrentCoroutine)
        map {
            async {
                requestSemaphore.withPermit {
                    f(it)
                }
            }
        }.awaitAll()
    }

    suspend fun findAll(): List<MexInstance> {
        val instances = mutableListOf<MexInstance>()
        for (i in 1..10) {
            instances.add(MexInstance(i.toString(), "name$i"))
        }
        return instances
    }

    suspend fun getMexFetchData(id: String): MexInstanceData? {
        return try {
            val instance = MexInstanceData(id, getMexEngine(id, "android"))
            log.info { "fetching data for $id, engine: ${instance.engine}" }
            instance
        } catch (e: Exception) {
            log.error { "Something wrong when processing this FormDef: $id" }
            null
        }
    }

    suspend fun getMexEngine(
        appVersion: String,
        platform: String,
    ): String {
//        val mockoonApi = WebClient.builder()
//            .baseUrl("http://localhost:3000")
//            .defaultCookie("cookie-name", "cookie-value")
//            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//            .build()
//        val request = mockoonApi.get().uri("/form/bundle/engine/$appVersion/$platform")
//
//        return request
//            .retrieve()
//            .awaitBody<ApiSuccessResult<String>>()
//            .result
        return "1"
    }
}

data class ObjectIdMapping(
    val key: String,
    val temporaryId: ObjectId,
    val objectId: ObjectId
)

data class ContextObject(
    val newObject: List<ContextArrayNode>,
    val updateObject: ContextArrayNode
)

data class DataRequest(
    val data: Map<String, JsonArray>,
    val tempIds: List<Pair<String, String>>
)

data class ContextArrayNode(
    val key: String,
    val uid: String,
    val data: JsonArray
)

data class StoreObject(
    val manageSource: ManageSource,
    val tempIds: List<Pair<String, String>> = emptyList(),
    val data: JsonObject
)

enum class ManageSource(val value: Int) {
    CREATE(1),
    UPDATE(2),
    DELETE(3)
}

