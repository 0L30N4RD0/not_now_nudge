package dev.contextnudge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.LinkedHashMap

class NudgeNotificationListener : NotificationListenerService() {
    private val recent = object : LinkedHashMap<String, Long>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?) = size > 100
    }
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || !NudgePreferences.enabled(this)) return
        if (sbn.packageName == packageName || sbn.packageName in NudgePreferences.excluded(this)) return
        val n = sbn.notification
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        if (n.category == Notification.CATEGORY_SERVICE || n.category == Notification.CATEGORY_CALL || n.category == Notification.CATEGORY_ALARM || n.category == Notification.CATEGORY_TRANSPORT) return
        val extras = n.extras ?: return
        val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return
        if (!NotificationFilter.eligible(text)) return
        val suggestion = NudgeParser.parse(text) ?: return
        val key = "${sbn.packageName}:${sbn.key}:${suggestion.startMillis}"
        synchronized(recent) {
            val now = System.currentTimeMillis()
            if (recent[key]?.let { now - it < 30 * 60_000L } == true) return
            recent[key] = now
        }
        val manager = getSystemService(NotificationManager::class.java)
        val channelId = "context_nudge_suggestions"
        manager.createNotificationChannel(NotificationChannel(channelId, "Sugestões contextuais", NotificationManager.IMPORTANCE_DEFAULT))
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_SEND; type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pending = PendingIntent.getActivity(this, key.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle("Possível compromisso identificado")
            .setContentText("Toque para revisar uma sugestão de calendário")
            .setContentIntent(pending).setAutoCancel(true).build()
        manager.notify(key.hashCode(), notification)
        NudgeOverlay.show(this, text, suggestion)
    }
}
