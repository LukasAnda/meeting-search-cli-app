package sk.vinf.cli.app.utils

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jillesvangurp.eskotlinwrapper.JacksonModelReaderAndWriter
import org.elasticsearch.client.configure
import org.elasticsearch.client.create
import org.elasticsearch.client.indexRepository
import java.io.File

object Inserter {
    fun insert(host: String, port: Int, importFile: String, logGranularity: Int) {
        val client = create(host = host, port = port)
        val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
        objectMapper.findAndRegisterModules().registerModule(KotlinModule())
        objectMapper.propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
        val repo =
            client.indexRepository("meetings_index", JacksonModelReaderAndWriter(PersonEntity::class, objectMapper))
        repo.deleteIndex()
        repo.createIndex {
            configure {
                settings {
                    shards = 3
                    replicas = 0
                }
                mappings {
                    text("name")
                    number<Long>("birthTimestamp")
                    number<Long>("deathTimestamp")
                }
            }
        }

        File(importFile).bufferedReader().useLines { sequence ->
            repo.bulk(bulkSize = 5000) {
                sequence.forEachIndexed { index, s ->
                    val parts = s.split("$|$")
                    val name = parts[0].substringBefore("| ").trim()
                    val birthDay = parts[1].trim().toLong()
                    val deathDay = parts.getOrNull(2)?.trim()?.toLong() ?: birthDay + 60L * 365 * 24 * 60 * 60 * 1000
                    val person = PersonEntity(name, birthDay, deathDay)

                    if (name.isNotEmpty()) {
                        index(index.toString(), person)
                    }

                    if (logGranularity > 0 && index % logGranularity == 0) {
                        println("Imported $index lines")
                    }
                }
            }
        }
        client.close()
    }
}