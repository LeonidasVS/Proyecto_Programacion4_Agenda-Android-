package com.example.crud_kotlin

import android.app.Application
import android.util.Log

object AppLifecycleTracker {
    private const val TAG = "AppLifecycleTracker"
    var isAppInForeground = false
    private val pendingAlarms = mutableListOf<() -> Unit>()

    fun appWentToForeground() {
        isAppInForeground = true
        Log.d(TAG, "📱 App en primer plano")
        // Ejecutar alarmas pendientes
        pendingAlarms.forEach { it.invoke() }
        pendingAlarms.clear()
    }

    fun appWentToBackground() {
        isAppInForeground = false
        Log.d(TAG, "📱 App en segundo plano")
    }

    fun scheduleAlarmForForeground(alarmAction: () -> Unit) {
        if (isAppInForeground) {
            // Si la app está en primer plano, ejecutar inmediatamente
            alarmAction.invoke()
        } else {
            // Si está en segundo plano, agregar a pendientes
            pendingAlarms.add(alarmAction)
            Log.d(TAG, "⏰ Alarma agregada a pendientes")
        }
    }
}