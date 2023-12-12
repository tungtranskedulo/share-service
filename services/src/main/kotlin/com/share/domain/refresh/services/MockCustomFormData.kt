package com.share.domain.refresh.services

import com.share.config.JsonObject
import com.share.config.emptyJsonObject
import com.share.config.fromJson
import com.share.model.ObjectId
import mu.KotlinLogging

private val log = KotlinLogging.logger {}
fun postInstanceData(): JsonObject {
    return """{
          "Job Products": {
            "a0P5f00001IJB8uEAH": {
              "jobProducts": [
                {
                  "UID": "dynamic_1",
                  "__managedSource": 1 
                },
                {
                  "UID": "dynamic_2",
                  "__managedSource": 1
                }, 
                {
                  "UID": "real_3",
                  "__managedSource": 2 
                },
                {
                  "UID": "real_4",
                   "__managedSource": 3 
                }
              ]
            }
          }
        }""".fromJson<JsonObject>()
}

fun saveToCondenserService(mock: Int): JsonObject {
    return when (mock) {
        1 -> """{
              "Job Products": {
                "main": {
                  "a0P5f00001IJB8uEAH": {
                    "jobId": "a0P5f00001IJB8uEAH",
                    "job": {},
                    "jobProducts": [
                       {
                         "UID": "real_1",
                        "__managedSource": 0
                        },
                      {
                        "UID": "real_5",
                        "__managedSource": 0
                      }
                    ]
                  }
                }
              }
            }""".fromJson<JsonObject>()
        2 -> """{
              "Job Products": {
                "main": {
                  "a0P5f00001IJB8uEAH": {
                    "jobId": "a0P5f00001IJB8uEAH",
                    "job": {},
                    "jobProducts": [
                       {
                         "UID": "real_1",
                        "__managedSource": 0
                        },
                         {
                         "UID": "real_2",
                        "__managedSource": 0
                        },
                      {
                        "UID": "real_5",
                        "__managedSource": 0
                      }
                    ]
                  }
                }
              }
            }""".fromJson<JsonObject>()
        3 -> """{
              "Job Products": {
                "main": {
                  "a0P5f00001IJB8uEAH": {
                    "jobId": "a0P5f00001IJB8uEAH",
                    "job": {},
                    "jobProducts": [
                       {
                         "UID": "real_1",
                        "__managedSource": 0
                        },
                         {
                         "UID": "real_2",
                        "__managedSource": 0
                        },
                        {
                         "UID": "real_3",
                        "__managedSource": 0
                        },
                         {
                         "UID": "real_4",
                        "__managedSource": 0
                        },
                      {
                        "UID": "real_5",
                        "__managedSource": 0
                      }
                    ]
                  }
                }
              }
            }""".fromJson<JsonObject>()

        else -> emptyJsonObject()
    }

}

fun findExistObjectIdMapping(tempIds: List<String>) : List<ObjectIdMapping>{
    log.info { "findExistObjectIdMapping: ${tempIds }}" }
    return listOf (ObjectIdMapping(ObjectId("dynamic_1"), ObjectId("real_1")))
}

fun getCustomFormInstanceDataFromCB(): JsonObject {
    return """{
              "Job Products": {
                "main": {
                  "a0P5f00001IJB8uEAH": {
                    "jobId": "a0P5f00001IJB8uEAH",
                    "job": {},
                    "jobProducts": [
                      {
                        "UID": "real_5"
                      }
                    ]
                  }
                }
              }
        }""".fromJson<JsonObject>()

}
