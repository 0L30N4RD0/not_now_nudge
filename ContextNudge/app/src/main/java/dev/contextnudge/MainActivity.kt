package dev.contextnudge

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var editor: EditText
    private lateinit var result: TextView
    private lateinit var action: Button
    private var suggestion: EventSuggestion? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }
        val heading = TextView(this).apply { text = "Context Nudge · 0.1"; textSize = 25f }
        val explanation = TextView(this).apply { text = "Compartilhe um texto com este app ou cole abaixo. Tudo é analisado localmente."; textSize = 15f }
        editor = EditText(this).apply {
            hint = "Ex.: Amanhã às 15h vamos ao shopping"
            minLines = 3
            maxLines = 7
            setText(intent?.takeIf { it.action == Intent.ACTION_SEND && it.type == "text/plain" }?.getStringExtra(Intent.EXTRA_TEXT).orEmpty())
        }
        val analyze = Button(this).apply { text = "Analisar texto"; setOnClickListener { analyze() } }
        result = TextView(this).apply { textSize = 17f; text = "Nenhuma análise realizada." }
        action = Button(this).apply {
            text = "Revisar no calendário"
            isEnabled = false
            setOnClickListener { suggestion?.let(::openCalendar) }
        }
        listOf(heading, explanation, editor, analyze, result, action).forEach {
            root.addView(it, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        setContentView(root)
        if (editor.text.isNotBlank()) analyze()
    }

    private fun analyze() {
        suggestion = NudgeParser.parse(editor.text.toString())
        action.isEnabled = suggestion != null
        result.text = suggestion?.let {
            val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"))
            val date = Instant.ofEpochMilli(it.startMillis).atZone(ZoneId.systemDefault()).format(fmt)
            "Sugestão: criar evento em $date (duração inicial: 1 hora). Revise antes de salvar."
        } ?: "Não identifiquei uma data relativa futura com horário explícito. Experimente 'amanhã às 15h'."
    }

    private fun openCalendar(event: EventSuggestion) {
        val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endMillis)
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
        }
        try { startActivity(calendarIntent) } catch (_: android.content.ActivityNotFoundException) {
            result.text = "Nenhum aplicativo de calendário compatível foi encontrado."
        }
    }
}
