package dev.contextnudge

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class EventSuggestion(val title: String, val startMillis: Long, val endMillis: Long, val description: String)

/** Conservative, on-device MVP parser. Never silently writes calendar data. */
object NudgeParser {
    private val timePattern = Regex("(?i)\\b(?:às|as|pelas|para as|para às)?\\s*(\\d{1,2})(?:[:h](\\d{2}))?\\s*(?:horas|hrs|hs|h)?\\b")
    private val explicitTime = Regex("(?i)(?:\\b(?:às|as|pelas|para as|para às)\\s*)(\\d{1,2})(?:[:h](\\d{2}))?(?:\\s*(?:horas|hrs|hs|h))?\\b|\\b(\\d{1,2})[:h](\\d{2})\\b")
    fun parse(text: String, now: LocalDateTime = LocalDateTime.now(), zone: ZoneId = ZoneId.systemDefault()): EventSuggestion? {
        val normalized = text.lowercase(Locale.forLanguageTag("pt-BR"))
        val date: LocalDate = when {
            Regex("\\bdepois de amanhã\\b").containsMatchIn(normalized) -> now.toLocalDate().plusDays(2)
            Regex("\\bamanhã\\b").containsMatchIn(normalized) -> now.toLocalDate().plusDays(1)
            Regex("\\bhoje\\b").containsMatchIn(normalized) -> now.toLocalDate()
            else -> return null
        }
        val match = explicitTime.find(normalized) ?: return null
        val hour = (match.groups[1]?.value ?: match.groups[3]?.value)?.toIntOrNull() ?: return null
        val minute = (match.groups[2]?.value ?: match.groups[4]?.value)?.toIntOrNull() ?: 0
        if (hour !in 0..23 || minute !in 0..59) return null
        val start = LocalDateTime.of(date, LocalTime.of(hour, minute))
        if (!start.isAfter(now)) return null
        val startMillis = start.atZone(zone).toInstant().toEpochMilli()
        val endMillis = start.plusHours(1).atZone(zone).toInstant().toEpochMilli()
        return EventSuggestion("Compromisso sugerido", startMillis, endMillis, text.take(1000))
    }
}
