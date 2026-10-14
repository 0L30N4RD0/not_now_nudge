package dev.contextnudge

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.Settings
import android.view.ViewGroup
import android.widget.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var editor: EditText
    private lateinit var result: TextView
    private lateinit var action: Button
    private lateinit var enabled: Switch
    private lateinit var exclusions: EditText
    private var suggestion: EventSuggestion? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 40, 32, 32) }
        val content = ScrollView(this).apply { addView(root) }
        fun add(view: android.view.View) { root.addView(view, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
        add(TextView(this).apply { text = "Context Nudge · 0.2"; textSize = 25f })
        add(TextView(this).apply { text = "Análise local. Nenhum texto de notificação é salvo ou enviado. Sugestões exigem sua confirmação."; textSize = 15f })
        enabled = Switch(this).apply {
            text = "Analisar notificações elegíveis (desligado por padrão)"
            isChecked = NudgePreferences.enabled(this@MainActivity)
            setOnCheckedChangeListener { _, checked ->
                NudgePreferences.setEnabled(this@MainActivity, checked)
                if (checked) requestNotificationPermission()
            }
        }
        add(enabled)
        add(Button(this).apply { text = "Autorizar acesso às notificações nas Configurações"; setOnClickListener { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) } })
        add(TextView(this).apply { text = "Excluir aplicativos (um nome de pacote por linha, ex.: com.exemplo.app):" })
        exclusions = EditText(this).apply { minLines = 2; hint = "Opcional"; setText(NudgePreferences.excluded(this@MainActivity).sorted().joinToString("\n")) }
        add(exclusions)
        add(Button(this).apply { text = "Salvar exclusões"; setOnClickListener {
            NudgePreferences.setExcluded(this@MainActivity, exclusions.text.toString().lines().map { it.trim() }.filter { it.isNotEmpty() }.toSet())
            Toast.makeText(this@MainActivity, "Exclusões salvas", Toast.LENGTH_SHORT).show()
        } })
        add(TextView(this).apply { text = "Teste manual: compartilhe ou cole um texto abaixo." })
        editor = EditText(this).apply { hint = "Ex.: Amanhã às 15h vamos ao shopping"; minLines = 3; maxLines = 7 }
        add(editor)
        add(Button(this).apply { text = "Analisar texto"; setOnClickListener { analyze() } })
        result = TextView(this).apply { textSize = 17f; text = "Nenhuma análise realizada." }
        add(result)
        action = Button(this).apply { text = "Revisar no calendário"; isEnabled = false; setOnClickListener { suggestion?.let(::openCalendar) } }
        add(action)
        setContentView(content)
        receive(intent)
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); receive(intent) }
    private fun receive(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            editor.setText(intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()); analyze()
        }
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
    }
    private fun analyze() {
        suggestion = NudgeParser.parse(editor.text.toString())
        action.isEnabled = suggestion != null
        result.text = suggestion?.let {
            val date = Instant.ofEpochMilli(it.startMillis).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR")))
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
        try { startActivity(calendarIntent) } catch (_: android.content.ActivityNotFoundException) { result.text = "Nenhum calendário compatível encontrado." }
    }
}
