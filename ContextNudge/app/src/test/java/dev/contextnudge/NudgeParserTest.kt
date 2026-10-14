package dev.contextnudge

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class NudgeParserTest {
    private val now = LocalDateTime.of(2026, 10, 14, 18, 0)
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test fun tomorrowAtThree() {
        val result = NudgeParser.parse("Amanhã às 15h vamos ao shopping", now, zone)
        assertNotNull(result)
        assertEquals(15, java.time.Instant.ofEpochMilli(result!!.startMillis).atZone(zone).hour)
        assertEquals(15, java.time.Instant.ofEpochMilli(result.startMillis).atZone(zone).dayOfMonth)
    }
    @Test fun rejectsPastToday() { assertNull(NudgeParser.parse("hoje às 15h", now, zone)) }
    @Test fun rejectsInvalidTime() { assertNull(NudgeParser.parse("amanhã às 27h", now, zone)) }
    @Test fun rejectsMissingDay() { assertNull(NudgeParser.parse("às 15h", now, zone)) }
}
