package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.ConsoleLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RendererPluginReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("RendererPluginReceiver", "Received broadcast: $action")

        val db = AppDatabase.getDatabase(context.applicationContext)
        val dao = db.rendererDao()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            dao.insertLog(ConsoleLog(
                tag = "Zink-Receiver",
                message = "Received broadcast action from Zalith Host: $action",
                level = "INFO"
            ))

            when (action) {
                "com.zalith.launcher.action.QUERY_RENDERERS",
                "com.zalith.launcher.action.GET_RENDERER_INFO",
                "com.zalith.launcher.RENDERER_PLUGIN" -> {
                    val responseIntent = Intent("com.zalith.launcher.action.REGISTER_RENDERER_RESPONSE").apply {
                        putExtra("renderer_id", "zink_mali_g57_fp16")
                        putExtra("renderer_name", "Zink GL 4.6 (Mali-G57 FP16 Optimized)")
                        putExtra("so_name", "libzink_mali.so")
                        putExtra("author", "Zalith Ecosystem Plugin")
                        putExtra("vblank_mode", 0)
                        putExtra("fp16_scaling", true)
                        putExtra("sodium_interop", true)
                        putExtra("iris_compat", true)
                    }
                    context.sendBroadcast(responseIntent)

                    dao.insertLog(ConsoleLog(
                        tag = "Zink-Receiver",
                        message = "Responded with registration configuration packet to 'com.zalith.launcher'",
                        level = "SUCCESS"
                    ))
                }
            }
        }
    }
}
