package dev.contextnudge

import java.text.Normalizer
import java.time.*
import java.util.Locale

data class EventSuggestion(val title: String, val startMillis: Long, val endMillis: Long, val description: String)

/** Local, conservative extraction. Requires a clear date and explicit time. */
object NudgeParser {
    private fun fold(s: String) = Normalizer.normalize(s.lowercase(Locale.forLanguageTag("pt-BR")), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
    private val time = Regex("\\b(?:as|pelas|para as)\\s*(\\d{1,2})(?:[:h](\\d{2}))?\\s*(?:h|hrs|horas)?\\b|\\b(\\d{1,2})[:h](\\d{2})\\b")
    private val numeric = Regex("\\b(?:dia\\s+)?(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{4}))?\\b")
    private val written = Regex("\\b(?:dia\\s+)?(\\d{1,2})\\s+de\\s+(janeiro|fevereiro|marco|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro)(?:\\s+de\\s+(\\d{4}))?\\b")
    private val weekdays = mapOf("segunda" to DayOfWeek.MONDAY,"terca" to DayOfWeek.TUESDAY,"quarta" to DayOfWeek.WEDNESDAY,"quinta" to DayOfWeek.THURSDAY,"sexta" to DayOfWeek.FRIDAY,"sabado" to DayOfWeek.SATURDAY,"domingo" to DayOfWeek.SUNDAY)
    private val monthNames = listOf("janeiro","fevereiro","marco","abril","maio","junho","julho","agosto","setembro","outubro","novembro","dezembro")
    private fun candidate(day: Int, month: Int, year: Int?, now: LocalDate): LocalDate? {
        val first = try { LocalDate.of(year ?: now.year, month, day) } catch (_: DateTimeException) { return null }
        return if (year == null && first.isBefore(now)) try { first.plusYears(1) } catch (_: DateTimeException) { null } else first
    }
    fun parse(text: String, now: LocalDateTime = LocalDateTime.now(), zone: ZoneId = ZoneId.systemDefault()): EventSuggestion? {
        val s = fold(text); val today = now.toLocalDate()
        val date = when {
            Regex("\\bdepois de amanha\\b").containsMatchIn(s) -> today.plusDays(2)
            Regex("\\bamanha\\b").containsMatchIn(s) -> today.plusDays(1)
            Regex("\\bhoje\\b").containsMatchIn(s) -> today
            written.containsMatchIn(s) -> written.find(s)!!.let { candidate(it.groupValues[1].toInt(),monthNames.indexOf(it.groupValues[2])+1,it.groupValues[3].toIntOrNull(),today) }
            numeric.containsMatchIn(s) -> numeric.find(s)!!.let { candidate(it.groupValues[1].toInt(),it.groupValues[2].toInt(),it.groupValues[3].toIntOrNull(),today) }
            else -> weekdays.entries.firstOrNull { Regex("\\b${it.key}(?:-feira)?\\b").containsMatchIn(s) }?.let { entry ->
                val diff = (entry.value.value - today.dayOfWeek.value + 7) % 7
                today.plusDays(if (diff == 0) 7L else diff.toLong())
            }
        } ?: return null
        val m = time.find(s) ?: return null
        val hour = (m.groups[1]?.value ?: m.groups[3]?.value)?.toIntOrNull() ?: return null
        val minute = (m.groups[2]?.value ?: m.groups[4]?.value)?.toIntOrNull() ?: 0
        if (hour !in 0..23 || minute !in 0..59) return null
        val start = LocalDateTime.of(date, LocalTime.of(hour,minute))
        if (!start.isAfter(now)) return null
        val title = when {
            Regex("\\b(prova|exame|avaliacao)\\b").containsMatchIn(s) -> "Prova"
            Regex("\\b(consulta|medico|dentista)\\b").containsMatchIn(s) -> "Consulta"
            Regex("\\b(reuniao|meeting)\\b").containsMatchIn(s) -> "Reunião"
            Regex("\\b(aula|faculdade|curso)\\b").containsMatchIn(s) -> "Aula / faculdade"
            Regex("\\b(entrevista)\\b").containsMatchIn(s) -> "Entrevista"
            else -> "Compromisso sugerido"
        }
        return EventSuggestion(title,start.atZone(zone).toInstant().toEpochMilli(),start.plusHours(1).atZone(zone).toInstant().toEpochMilli(),text.take(1000))
    }
}
