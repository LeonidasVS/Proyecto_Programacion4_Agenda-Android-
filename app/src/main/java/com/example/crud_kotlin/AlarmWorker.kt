package com.example.crud_kotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log

class AlarmWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("AlarmWorker", "🔥🔥🔥 WORKER EJECUTADO - ALARMA SONANDO 🔥🔥🔥")

        val titulo = inputData.getString("titulo") ?: "Recordatorio"
        val fecha = inputData.getString("fecha") ?: ""
        val hora = inputData.getString("hora") ?: ""

        // Forzar la notificación aunque el teléfono esté dormido
        mostrarNotificacionForzada(titulo, fecha, hora)

        return Result.success()
    }

    private fun mostrarNotificacionForzada(titulo: String, fecha: String, hora: String) {
        try {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Crear canal de notificación
            crearCanalNotificacion(notificationManager)

            // Sonido de alarma - MÁXIMO volumen
            val sonidoAlarma: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // Notificación MÁS AGRESIVA
            val builder = NotificationCompat.Builder(applicationContext, "ALARMA_CHANNEL")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🔔 $titulo")
                .setContentText("$fecha - $hora")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(sonidoAlarma)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000)) // Vibración larga
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(null, true) // Hacerla full screen en algunos dispositivos
                .setTimeoutAfter(30000) // 30 segundos

            // Mostrar notificación con ID único
            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, builder.build())

            Log.d("AlarmWorker", "✅ NOTIFICACIÓN MOSTRADA - DEBERÍA SONAR")

        } catch (e: Exception) {
            Log.e("AlarmWorker", "❌ Error: ${e.message}")
        }
    }

    private fun crearCanalNotificacion(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ALARMA_CHANNEL",
                "Recordatorios con Alarma",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Canal para recordatorios importantes"
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

            // Configurar sonido
            val sonidoAlarma = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            channel.setSound(sonidoAlarma, null)

            // Configuraciones CRÍTICAS
            channel.setBypassDnd(true) // Ignorar "No molestar"
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

            notificationManager.createNotificationChannel(channel)
        }
    }
}