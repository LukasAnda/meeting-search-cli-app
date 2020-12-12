package sk.vinf.cli.app.utils

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import com.jillesvangurp.eskotlinwrapper.JacksonModelReaderAndWriter
import com.jillesvangurp.eskotlinwrapper.dsl.MatchQuery
import org.elasticsearch.action.search.dsl
import org.elasticsearch.client.create
import org.elasticsearch.client.indexRepository

object Finder {
    fun search(host: String, port: Int, firstName: String, secondName: String){
        val client = create(host = host, port = port)
        val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
        objectMapper.findAndRegisterModules().registerModule(KotlinModule())
        objectMapper.propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
        val repo =
            client.indexRepository("meetings_index", JacksonModelReaderAndWriter(PersonEntity::class, objectMapper))

        val group1 = repo.search {
            dsl {
                query = MatchQuery(field = "name", query = firstName)
            }
        }.hits.map {
            it.second
        }.filterNotNull().toList()

        val group2 = repo.search {
            dsl {
                query = MatchQuery(field = "name", query = secondName)
            }
        }.hits.map {
            it.second
        }.filterNotNull().toList()

        println()
        println()
        println(table {
            cellStyle {
                alignment = TextAlignment.MiddleCenter
                paddingLeft = 1
                paddingRight = 1
                border = true
            }
            header {
                cellStyle {
                    border = true
                    alignment = TextAlignment.BottomCenter
                }
                row {
                    cell("") {
                        rowSpan = 2
                    }
                    cell("Have these people met?") {
                        alignment = TextAlignment.BottomCenter
                        columnSpan = group2.size
                    }
                }
                row{
                    group2.forEach {
                        cell(it.name)
                    }
                }
            }
            group1.forEach { person ->
                row {
                    cell(person.name)
                    group2.forEach { person2 ->
                        val sign = if(person.hasMet(person2)) "yes" else "no"
                        cell(sign)
                    }
                }
            }
        })
        println()

        client.close()
    }
}