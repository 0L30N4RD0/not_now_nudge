package dev.contextnudge

import android.content.Context

object NudgePreferences {
    private const val NAME = "context_nudge_options"
    fun enabled(context: Context) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean("enabled", false)
    fun setEnabled(context: Context, enabled: Boolean) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putBoolean("enabled", enabled).apply()
    fun excluded(context: Context) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getStringSet("excluded", emptySet()) ?: emptySet()
    fun setExcluded(context: Context, packages: Set<String>) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putStringSet("excluded", packages.toSet()).apply()
}
