package sk.vinf.cli.app.utils

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import com.jillesvangurp.eskotlinwrapper.JacksonModelReaderAndWriter
import com.jillesvangurp.eskotlinwrapper.dsl.*
import org.elasticsearch.action.search.dsl
import org.elasticsearch.client.create
import org.elasticsearch.client.indexRepository
import org.elasticsearch.index.query.MatchPhraseQueryBuilder

object Finder {
    @SearchDSLMarker
    class MatchPhraseQuery(
        field: String,
        query: String,
        matchQueryConfig: MatchQueryConfig = MatchQueryConfig(),
        block: (MatchQueryConfig.() -> Unit)? = null
    ) : ESQuery(name = "match_phrase") {
        // The map is empty until we assign something
        init {
            putNoSnakeCase(field, matchQueryConfig)
            matchQueryConfig.query = query
            block?.invoke(matchQueryConfig)
        }
    }

    fun search(host: String, port: Int, firstName: String, secondName: String){
        val client = create(host = host, port = port)
        val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
        objectMapper.findAndRegisterModules().registerModule(KotlinModule())
        objectMapper.propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
        val repo =
            client.indexRepository("meetings_index", JacksonModelReaderAndWriter(PersonEntity::class, objectMapper))

        val group1 = repo.search {
            dsl {
                query = if(firstName.contains("\"")){
                    MatchPhraseQuery("name", firstName.replace("\"", ""))
                } else {
                    MatchQuery("name", firstName)
                }
            }
        }.hits.map {
            it.second
        }.filterNotNull().toList()

        val group2 = repo.search {
            dsl {
                query = if(secondName.contains("\"")){
                    MatchPhraseQuery("name", secondName.replace("\"", ""))
                } else {
                    MatchQuery("name", secondName)
                }
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
//                row {
//                    cell("") {
//                        rowSpan = 2
//                    }
//                    cell("Have these people met?") {
//                        alignment = TextAlignment.BottomCenter
//                        columnSpan = group2.size
//                    }
//                }
                row{
                    cell("")
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