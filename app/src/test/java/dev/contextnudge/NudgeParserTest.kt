package dev.contextnudge
import org.junit.Assert.*
import org.junit.Test
import java.time.*
class NudgeParserTest {
 private val now=LocalDateTime.of(2026,10,14,14,0)
 @Test fun tomorrow(){ val e=NudgeParser.parse("Consulta amanhã às 15h",now); assertNotNull(e); assertEquals("Consulta",e!!.title) }
 @Test fun writtenDate(){ val e=NudgeParser.parse("Tenho prova dia 19 de dezembro às 14h",now); assertNotNull(e); assertEquals("Prova",e!!.title); assertEquals(12,Instant.ofEpochMilli(e.startMillis).atZone(ZoneId.systemDefault()).monthValue) }
 @Test fun numericDate(){ assertNotNull(NudgeParser.parse("Reunião 20/10 às 9h30",now)) }
 @Test fun rejectNoTime(){ assertNull(NudgeParser.parse("Tenho prova dia 19 de dezembro",now)) }
 @Test fun rejectPast(){ assertNull(NudgeParser.parse("Consulta hoje às 9h",now)) }
 @Test fun rejectBadDate(){ assertNull(NudgeParser.parse("Consulta dia 31 de fevereiro às 15h",now)) }
}
