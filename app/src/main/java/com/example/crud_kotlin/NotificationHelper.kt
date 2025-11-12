package com.example.crud_kotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "ALARMA_INMEDIATA"

    fun mostrarAlarmaInmediata(context: Context, titulo: String, mensaje: String = "Es hora de tu recordatorio") {
        try {
            Log.d(TAG, "🔔 MOSTRANDO ALARMA: $titulo")

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            crearCanalNotificacion(notificationManager)

            // Sonido de alarma - FORZAR
            val sonidoAlarma: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            Log.d(TAG, "🔊 Sonido: $sonidoAlarma")

            // Intent para la app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Notificación MÁXIMA prioridad
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🔔 $titulo")
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(sonidoAlarma)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(false)
                .setTimeoutAfter(30000)

            // Mostrar notificación
            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, builder.build())

            Log.d(TAG, "✅✅✅ NOTIFICACIÓN MOSTRADA ✅✅✅")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}")
        }
    }

    private fun crearCanalNotificacion(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarmas Importantes",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Canal para alarmas y recordatorios importantes"
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            channel.setBypassDnd(true)
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

            val sonidoAlarma = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            channel.setSound(sonidoAlarma, android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())

            notificationManager.createNotificationChannel(channel)
        }
    }
}