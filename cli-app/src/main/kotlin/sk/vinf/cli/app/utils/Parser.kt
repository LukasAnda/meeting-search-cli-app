package sk.vinf.cli.app.utils

import sk.vinf.cli.app.utils.*
import kotlinx.coroutines.*
import org.apache.commons.compress.compressors.CompressorException
import org.apache.commons.compress.compressors.CompressorInputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.apache.commons.text.StringEscapeUtils
import org.joda.time.DateTime
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

object Parser{
    fun parse(source: File, dest: File){
        var linesRead = 0
        parseFile(source, object : ParserProgress {
            override fun onLineRead() {
                linesRead++
            }

            override fun onPersonAdded(text: String) {
                dest.appendText(text)
            }

            override fun onFinishedParsing(peopleAdded: Int) {
                println()
                println("Finished parsing, added people: $peopleAdded")
            }
        })
    }
}

interface ParserProgress {
    fun onLineRead()
    fun onPersonAdded(text: String)
    fun onFinishedParsing(numberOfAdded: Int)
}

@Throws(FileNotFoundException::class, CompressorException::class)
fun getInputStream(fileIn: String?): CompressorInputStream? {
    val fin = FileInputStream(fileIn)
    val bis = BufferedInputStream(fin)
    return CompressorStreamFactory().createCompressorInputStream(bis)
}

fun parseFile(entryFile: File, progress: ParserProgress) {
    var processedText = ""
    var isProcessingText = false
    var peopleAdded = 0
    getInputStream(entryFile.absolutePath)?.reader()?.useLines { sequence ->
        sequence.forEach {
            progress.onLineRead()
            when {
                it.contains("<text") -> {
                    processedText = ""
                    isProcessingText = true

                    processedText += it
                    processedText += "\n"
                }
                it.contains("</text") -> {
                    progress.onLineRead()

                    processedText += it
                    processedText += "\n"
                    isProcessingText = false

                    parseText(processedText)?.let { it1 ->
                        progress.onPersonAdded(it1)
                        peopleAdded++
                    }
                }
                else -> {
                    progress.onLineRead()
                    if (isProcessingText) {
                        processedText += it
                        processedText += "\n"
                    }
                }
            }
        }
    }
    progress.onFinishedParsing(peopleAdded)
}

fun parseText(text: String): String? {
    val matcher = infoBoxRegex.matcher(text)
    return if (matcher.find()) {
        parseInfoBox(matcher.group())
    } else null
}

fun parseInfoBox(infoBox: String): String? {
    val person = Person()

    infoBox.split("\n").forEach {
        when {
            it.contains(Regex("^\\|\\s*name")) && person.name.isEmpty() -> person.name = it
            it.contains(Regex("^\\|\\s*birth_date")) && person.birthDate.isEmpty() -> person.birthDate = it
            it.contains(Regex("^\\|\\s*death_date")) && person.deathDate.isEmpty() -> person.deathDate = it
        }
    }
    if (person.isValid()) {
        return person.toString() + "\n"
    }
    return null
}

class Person {
    var name: String = ""
    var birthDate: String = ""
    var deathDate: String = ""

    private fun getPersonName(): String {
        var parsedName = name
        parsedName = parsedName.substringAfter("=")
        return parsedName.unescapeHtml()
            .removeComments()
            .removeNonEnglishCharacters()
            .removeReferences()
            .removeSpans()
            .removePattern(parenthesesRegex2)
            .removePattern(parenthesesRegex3)
            .removePattern(parenthesesRegex4)
            .removePattern(htmlRegex)
            .replace("  ", " ")
            .replace(":", "")
            .trim(',', ' ', '.')
    }

    private fun getBirthDay(): String {
        var parsedBirthDay = birthDate
        parsedBirthDay = parsedBirthDay.substringAfter("=")
        return parsedBirthDay.removeComments().removeReferences().trim().getDate()
    }

    private fun getDeathDay(): String {
        var parsedDeathDate = deathDate
        parsedDeathDate = parsedDeathDate.substringAfter("=")
        return parsedDeathDate.removeComments().removeReferences().trim().getDate()
    }

    fun isValid() = getPersonName().isNotEmpty() && getBirthDay().isNotEmpty()

    override fun toString() = StringBuilder().apply {
        append(getPersonName())

        val birthDay = getBirthDay()
        append(" $|$ ")
        append(birthDay)


        val deathDay = getDeathDay()

        if (deathDay.isNotEmpty()) {
            append(" $|$ ")
            append(deathDay)
        }
    }.toString()
}

fun String.getDate(): String {
    return runCatching {
        when {
            this.contains("date") && (this.contains("birth", true) || this.contains(
                "death",
                true
            )) -> getDate(dateRegex).getTimestamp()
            this.containsMonth() -> removePattern(parenthesesRegex).getDateWithMonthString()
            this.containsCirca() -> extractDateFromCirca()
            this.containsNumber() -> extractDateFromCirca()
            else -> ""
        }
    }.getOrDefault("")
}

fun String.containsMonth() = monthsRegex.matcher(this).find()

fun String.containsCirca() = circaRegex.matcher(this).find()

fun String.containsNumber() = firstNumberRegex.matcher(this).find()

fun String.extractDateFromCirca(): String {
    val matcher = firstNumberRegex.matcher(this)
    if (!matcher.find()) return ""
    var year = matcher.group().toInt()
    if (this.contains("BC", ignoreCase = true)) {
        year -= year * 2
    }
    return DateTime.now().withYear(year).millis.toString()
}

fun String.getDateWithMonthString(): String {
    return runCatching {
        val matcher = monthsRegex.matcher(this)
        matcher.find()
        val month = matcher.group()
        val dateParts = getDate(dateRegex).split("-").take(2).map { it.toInt() }
        val date = DateTime()
            .withMonthOfYear(monthsMap.getOrElse(month) { 1 })
            .withDayOfMonth(dateParts.getOrElse(0) { 0 })
            .withYear(dateParts.getOrElse(1) { 0 })
            .millis
        return@runCatching date.toString()
    }.getOrDefault("")
}

fun String.getTimestamp(): String {
    return runCatching {
        val dateParts = this.split("-").dropLast(1).map { it.toInt() }
        val date = DateTime()
            .withYear(dateParts.getOrElse(0) { 0 })
            .withMonthOfYear(dateParts.getOrElse(1) { 1 })
            .withDayOfMonth(dateParts.getOrElse(2) { 1 })
        return@runCatching date?.millis?.toString() ?: ""
    }.getOrDefault("")
}

fun String.unescapeHtml(): String {
    return StringEscapeUtils.unescapeHtml4(this)
}

fun String.getDate(pattern: com.florianingerl.util.regex.Pattern): String {
    val matcher = pattern.matcher(this)
    if (matcher.find()) {
        return matcher.group() + "-" + this.replaceFirst(matcher.group(), "").getDate(pattern)
    }
    return ""
}

fun String.removePattern(pattern: com.florianingerl.util.regex.Pattern): String {
    val matcher = pattern.matcher(this)
    if (matcher.find()) {
        return this.replace(matcher.group(), " ").removePattern(pattern)
    }
    return this
}

fun String.removeNonEnglishCharacters(): String {
    val matcher = arabic.matcher(this)
    if (matcher.find()) {
        return this.replace(matcher.group(), "").removeNonEnglishCharacters()
    }
    return this
}

fun String.removeReferences(): String {
    return removePattern(referencesRegex)
}

fun String.removeSpans(): String {
    return removePattern(spanRegex)
}

fun String.removeComments(): String {
    return removePattern(commentsRegex)
}