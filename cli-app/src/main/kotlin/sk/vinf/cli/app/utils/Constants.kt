package sk.vinf.cli.app.utils

import com.florianingerl.util.regex.Pattern

const val DATA_PATH = "./data"
val infoBoxRegex: Pattern = Pattern.compile("(?=\\{Infobox)(\\{([^{}]|(?1))*+\\})")
val parenthesesRegex2: Pattern = Pattern.compile("(\\[([^\\[\\]]|(?1))*+\\])")
val parenthesesRegex3: Pattern = Pattern.compile("(\\{([^\\{\\}]|(?1))*+\\})")
val parenthesesRegex4: Pattern = Pattern.compile("(\\(([^\\(\\)]|(?1))*+\\))")
val htmlRegex: Pattern = Pattern.compile("<[^>]*>")
val commentsRegex: Pattern = Pattern.compile("\\<!--(.|\\n)*?-->")
val referencesRegex: Pattern = Pattern.compile("(\\<ref(.|\\n)*?\\/ref>|\\<ref(.|\\n)*?\\/>)")
val spanRegex: Pattern = Pattern.compile("(\\<span(.|\\n)*?\\/span>|\\<span(.|\\n)*?\\/>)")
val dateRegex: Pattern = Pattern.compile("(\\d+)")
val arabic: Pattern = Pattern.compile("\\p{InArabic}+")
val monthsRegex: Pattern =
    Pattern.compile("(January|February|March|April|May|June|July|August|September|October|November|December)")
val circaRegex: Pattern = Pattern.compile("(c\\.|''c''|circa|C.|ca.)")
val firstNumberRegex: Pattern = Pattern.compile("(^|\\\\s)*([0-9]+)(\$|\\\\s)*")
val parenthesesRegex: Pattern = Pattern.compile("\\(.+\\)")
val monthsMap = mapOf(
    "January" to 1,
    "February" to 2,
    "March" to 3,
    "April" to 4,
    "May" to 5,
    "June" to 6,
    "July" to 7,
    "August" to 8,
    "September" to 9,
    "October" to 10,
    "November" to 11,
    "December" to 12
)