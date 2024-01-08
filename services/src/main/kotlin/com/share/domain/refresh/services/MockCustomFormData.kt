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
              ],
              "accidentIncidentReports": [
              {
                  "UID": "dynamic_17032084382843",
                  "__managedSource": 1
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
                    ],
                    "accidentIncidentReports": [
                    {
                      "CreatedByID": {
                        "Name": "V3 TestCustomform",
                        "UID": "0055f000009yZCXAA2"
                      },
                      "DateTimeOfIncident": "2023-12-22T01:27:08.000Z",
                      "Details": "",
                      "ExactLocationOfIncident": "",
                      "JobId": "a0P5f00001IJB8uEAH",
                      "UID": "17032084382843",
                      "__managedSource": 1,
                      "tempUID": 1703208428161
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
                    ],
                    "accidentIncidentReports": [
                    {
                      "CreatedByID": {
                        "Name": "V3 TestCustomform",
                        "UID": "0055f000009yZCXAA2"
                      },
                      "DateTimeOfIncident": "2023-12-22T01:27:08.000Z",
                      "Details": "",
                      "ExactLocationOfIncident": "",
                      "JobId": "a0P5f00001IJB8uEAH",
                      "UID": "17032084382843",
                      "__managedSource": 1,
                      "tempUID": 1703208428161
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
                    ],
                    "accidentIncidentReports": [
                    {
                      "CreatedByID": {
                        "Name": "V3 TestCustomform",
                        "UID": "0055f000009yZCXAA2"
                      },
                      "DateTimeOfIncident": "2023-12-22T01:27:08.000Z",
                      "Details": "",
                      "ExactLocationOfIncident": "",
                      "JobId": "a0P5f00001IJB8uEAH",
                      "UID": "17032084382843",
                      "__managedSource": 1,
                      "tempUID": 1703208428161
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
    return emptyList()
   // return listOf (ObjectIdMapping("jobProducts", ObjectId("dynamic_1"), ObjectId("real_1")))
}

fun findAllObjectIdMapping() : List<ObjectIdMapping>{
    return emptyList()
    //return listOf (ObjectIdMapping("jobProducts", ObjectId("dynamic_1"), ObjectId("real_1")))
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
