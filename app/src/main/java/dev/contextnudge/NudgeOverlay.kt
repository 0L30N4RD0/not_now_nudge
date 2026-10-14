package dev.contextnudge

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.*

/** Optional temporary overlay, only after explicit opt-in and OS permission. */
object NudgeOverlay {
 private var current: android.view.View? = null
 private var owner: WindowManager? = null
 fun show(context:Context, text:String, suggestion:EventSuggestion) {
  if (!NudgePreferences.overlay(context) || !Settings.canDrawOverlays(context)) return
  val wm=context.getSystemService(WindowManager::class.java)
  synchronized(this) {
   current?.let { try { owner?.removeView(it) } catch (_:Exception) {} }
   current=null
   val layout=LinearLayout(context).apply { orientation=LinearLayout.VERTICAL; setPadding(26,18,26,18); setBackgroundColor(Color.rgb(35,39,48)) }
   layout.addView(TextView(context).apply { setTextColor(Color.WHITE); textSize=16f; setTypeface(null,Typeface.BOLD); text="Context Nudge · ${suggestion.title}" })
   layout.addView(TextView(context).apply { setTextColor(Color.WHITE); text="Possível compromisso. Revise antes de salvar." })
   val actions=LinearLayout(context).apply { orientation=LinearLayout.HORIZONTAL }
   fun dismiss() { synchronized(this@NudgeOverlay) { try { wm.removeView(layout) } catch (_:Exception) {}; if(current===layout) { current=null; owner=null } } }
   actions.addView(Button(context).apply { text="Ignorar"; setOnClickListener { dismiss() } })
   actions.addView(Button(context).apply { text="Revisar"; setOnClickListener {
    dismiss()
    val intent=Intent(context,MainActivity::class.java).apply { action=Intent.ACTION_SEND; type="text/plain"; putExtra(Intent.EXTRA_TEXT,text); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP) }
    context.startActivity(intent)
   } })
   layout.addView(actions)
   val params=WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT).apply { gravity=Gravity.TOP; y=90 }
   try { wm.addView(layout,params); current=layout; owner=wm } catch (_:Exception) { current=null; owner=null }
  }
 }
}
