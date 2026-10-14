package dev.contextnudge
import android.content.Context
object NudgePreferences {
 private const val NAME = "context_nudge_options"
 private fun prefs(c:Context)=c.getSharedPreferences(NAME,Context.MODE_PRIVATE)
 fun enabled(c:Context)=prefs(c).getBoolean("enabled",false)
 fun setEnabled(c:Context,v:Boolean)=prefs(c).edit().putBoolean("enabled",v).apply()
 fun overlay(c:Context)=prefs(c).getBoolean("overlay",false)
 fun setOverlay(c:Context,v:Boolean)=prefs(c).edit().putBoolean("overlay",v).apply()
 fun excluded(c:Context)=prefs(c).getStringSet("excluded",emptySet())?:emptySet()
 fun setExcluded(c:Context,v:Set<String>)=prefs(c).edit().putStringSet("excluded",v.toSet()).apply()
}
