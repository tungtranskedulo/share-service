package com.share.domain.refresh.services

import arrow.core.Either
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
import com.share.config.JsonArray
import com.share.config.JsonObject
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
    private val apiCLient: ApiClient
) {
    @CoroutineLogExecutionTime
    suspend fun getCustomForm(forms: List<String>? = emptyList()) {
        delay(500)
        log.info { "Fetching form ${forms?.map { it }}" }
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

        val saveObjectGroupByUID = postInstanceData.extractContextObjectData(
            ObjectId("a0P5f00001IJB8uEAH")
        )?.groupByDynamicUID()

        val extractStoreObject = saveObjectGroupByUID?.let { context ->
            val existObjectIdMapping = findExistObjectIdMapping(context.newObject.map { p -> p.uid })
            log.info { "saveObjectWithUID $context" }
            val storeObjects = mutableListOf<StoreObject>()
            context.newObject.map { node ->
                storeObjects.add(
                    existObjectIdMapping.find { it.temporaryId.value == node.uid }?.let {
                        StoreObject(
                            uid = it.objectId.value,
                            data = postInstanceData.replaceArrayNode(
                                node.data.replaceValueByKey(
                                    "UID",
                                    it.objectId.value
                                )
                            )
                        )
                    } ?: StoreObject(node.uid, postInstanceData.replaceArrayNode(node.data))
                )
            }
            context.updateObject.map {
                storeObjects.add(
                    StoreObject(
                        uid = it.uid,
                        data = postInstanceData.replaceArrayNode(it.data)
                    )
                )
            }
            storeObjects
        }


        var mock = 0
        val mappingObjectIdMapping = mutableListOf<ObjectIdMapping>()
        val savedObjects = extractStoreObject?.let { storeObj ->
            val existUIDs = getCustomFormInstanceDataFromCB()
                .extractContextObjectData(ObjectId("a0P5f00001IJB8uEAH"))
                ?.extractUID()?.toMutableList()
                ?: mutableListOf<String>()
            log.info { "existUIDs $existUIDs" }

            storeObj.map { obj ->
                mock++
                val fetchObject = saveToCondenserService(mock)
                log.info { "request ${obj.uid} - ${obj.data} to condenser" }
                log.info { "response $fetchObject to condenser" }
                if (obj.uid.startsWith("dynamic_")) {
                    val arrayNode = fetchObject.extractContextObjectData(ObjectId("a0P5f00001IJB8uEAH"))
                    arrayNode?.getMappingObject(
                        obj.uid,
                        existUIDs
                    )?.let {
                        log.info { "mappingObjectIdMapping ${it.temporaryId} - ${it.objectId}" }
                        existUIDs.add(it.objectId.toString())
                        log.info { "existUIDs $existUIDs" }
                        mappingObjectIdMapping.add(it)
                    }
                }

                fetchObject
            }
        }

        log.info { "save ObjectIdMapping ${mappingObjectIdMapping.map { "${it.temporaryId} - ${it.objectId}" }}" }

        return savedObjects as Any
    }


    private suspend fun JsonArray.extractUID(): List<String> {
        return this.mapNotNull {
            it["UID"]?.asText()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun JsonObject.extractContextObjectData(contextObjectId: ObjectId): JsonArray? {
        for (fieldName in this.fieldNames()) {
            if (this.has(contextObjectId.value)) {
                this.get(contextObjectId.value).fields().forEach { (_, value) ->
                    if (value is JsonArray) {
                        return value // assume that there is just only one array in the context object
                    }
                }
            } else {
                val nextNode = this.get(fieldName)
                if (nextNode is JsonObject) {
                    return nextNode.extractContextObjectData(contextObjectId)
                }
            }
        }
        return null
    }

    private suspend fun JsonArray.groupByDynamicUID(): ContextObject? {
        val newObject = mutableListOf<ContextArrayNode>()

        val nonDynamicNode = objectMapper.createArrayNode()
        forEach { node ->
            node["UID"]?.asText()?.let { uid ->
                if (uid.startsWith("dynamic_")) {
                    newObject.add(ContextArrayNode(node["UID"].asText(), objectMapper.createArrayNode().add(node)))
                } else {
                    nonDynamicNode.add(node)
                }
            }
        }

        if (newObject.isEmpty()) {
            return null
        }

        return ContextObject(
            newObject = newObject,
            updateObject = if (!nonDynamicNode.isEmpty) {
                listOf(ContextArrayNode("__updateSource", nonDynamicNode))
            } else {
                emptyList()
            }
        )
    }

    fun JsonObject.replaceArrayNode(
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

    private suspend fun JsonArray.getMappingObject(
        temporaryId: String,
        existKeys: List<String>
    ): ObjectIdMapping? {
        val mapping = this.filterNot {
            it["UID"]?.asText() in existKeys
        }
        return mapping.firstOrNull()?.get("UID")?.asText()?.let { uid ->
            ObjectIdMapping(
                temporaryId = ObjectId(temporaryId),
                objectId = ObjectId(uid)
            )
        }
    }

    suspend fun findByTemporaryIds(): Flow<ObjectIdMapping> {
        return listOf(
            ObjectIdMapping(
                temporaryId = ObjectId("dynamic_17016756828861"),
                objectId = ObjectId("a0L5f00000C0elzEAB")
            ),
            ObjectIdMapping(
                temporaryId = ObjectId("dynamic_17016756927662"),
                objectId = ObjectId("a0L5f00000C0f1mEAB")
            ),
        ).asFlow()
    }


//    private suspend fun JsonObject.findAndReplaceExistTempId(temporaryIds: List<ObjectId>): JsonObject {
//        log.info { "findAndReplaceExistTempId $temporaryIds" }
//        val uidMappings = findByTemporaryIds().toList()
//        val updatedJsonObject = replaceUID(this, uidMappings)
//        log.info { "updatedJsonObject $updatedJsonObject" }
//        return updatedJsonObject
//    }


    fun ObjectNode.replaceValueByKey(key: String, value: Any): ObjectNode {
        val jsonFactory = objectMapper.factory
        val jsonObject = this

        return jsonFactory.toJsonObj().objectNode().apply {
            jsonObject.fields().forEach { (k, v) ->
                set<ObjectNode>(
                    k, when {
                        v is ObjectNode -> v.replaceValueByKey(key, value)
                        v is ArrayNode -> v.replaceValueByKey(key, value)
                        k == key -> {
                            when (v) {
                                is TextNode -> {
                                    TextNode(value.toString())
                                }

                                else -> v
                            }
                        }

                        else -> v
                    }
                )
            }
        }
    }

    fun ArrayNode.replaceValueByKey(key: String, value: Any): ArrayNode {
        val jsonFactory = objectMapper.factory
        val jsonArray = this

        return jsonFactory.toJsonObj().arrayNode().apply {
            jsonArray.forEach { element ->
                if (element is ObjectNode) {
                    val updatedElement = element.replaceValueByKey(key, value)
                    add(updatedElement)
                } else {
                    add(element)
                }
            }
        }
    }

    private suspend fun JsonArray.getMappingRecord(mappingKeys: List<Pair<ObjectId, ObjectId>>): List<ObjectIdMapping> {
        return mappingKeys.mapNotNull {
            val jobProduct = this.find { jobProduct -> jobProduct["ProductId"]?.asText() == it.second.value }
            if (jobProduct != null) {
                ObjectIdMapping(
                    temporaryId = it.first,
                    objectId = ObjectId(jobProduct["UID"]?.asText() ?: throw IllegalStateException("UID is null"))
                )
            } else null
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
    val temporaryId: ObjectId,
    val objectId: ObjectId
)

data class ContextObject(
    val newObject: List<ContextArrayNode>,
    val updateObject: List<ContextArrayNode>
)

data class ContextArrayNode(
    val uid: String,
    val data: JsonArray
)

data class StoreObject(
    val uid: String,
    val data: JsonObject
)
