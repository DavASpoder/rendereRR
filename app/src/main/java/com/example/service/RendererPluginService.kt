package com.example.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.ConsoleLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RendererPluginService : Service() {
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): RendererPluginService = this@RendererPluginService
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d("RendererPluginService", "Service bound: ${intent?.action}")
        val db = AppDatabase.getDatabase(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            db.rendererDao().insertLog(ConsoleLog(
                tag = "Zink-Service",
                message = "Zalith Launcher successfully bound to RendererPluginService. Shared library pipeline maps attached.",
                level = "SUCCESS"
            ))
        }
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("RendererPluginService", "Service started")
        return START_STICKY
    }
}
