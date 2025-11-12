package com.example.crud_kotlin

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.crud_kotlin.Modelos.Recordatorio
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object AlarmUtils {
    private const val TAG = "AlarmUtils"
    private val executor = Executors.newScheduledThreadPool(1)
    private val activeTimers = mutableMapOf<String, Timer>()

    fun programarAlarma(context: Context, recordatorio: Recordatorio) {
        try {
            Log.d(TAG, "🚀 PROGRAMANDO ALARMA HÍBRIDA: ${recordatorio.titulo}")

            // Parsear fecha y hora
            val formato = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US)
            val fechaHoraString = "${recordatorio.fecha} ${recordatorio.hora}"
            val fechaHora = formato.parse(fechaHoraString)

            if (fechaHora == null) {
                Log.e(TAG, "❌ Error parseando fecha/hora")
                return
            }

            val tiempoRecordatorio = fechaHora.time
            val tiempoActual = System.currentTimeMillis()
            val diferencia = tiempoRecordatorio - tiempoActual

            Log.d(TAG, "⏰ Alarma en: ${diferencia / 1000 / 60} minutos")

            if (diferencia <= 0) {
                Log.e(TAG, "❌ Tiempo en el pasado")
                mostrarAlarmaInmediata(context, recordatorio)
                return
            }

            // ✅ SOLUCIÓN HÍBRIDA: Timer + AlarmManager
            programarTimerPrimerPlano(context, recordatorio, diferencia)
            programarAlarmManager(context, recordatorio, tiempoRecordatorio)

            Log.d(TAG, "✅ ALARMA HÍBRIDA PROGRAMADA: ${recordatorio.titulo}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}")
        }
    }

    private fun programarTimerPrimerPlano(context: Context, recordatorio: Recordatorio, delay: Long) {
        try {
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    Log.d(TAG, "⏰ TIMER EJECUTADO: ${recordatorio.titulo}")
                    mostrarAlarmaInmediata(context, recordatorio)
                }
            }, delay)

            activeTimers[recordatorio.id] = timer
            Log.d(TAG, "✅ Timer programado para primer plano")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error con timer: ${e.message}")
        }
    }

    private fun programarAlarmManager(context: Context, recordatorio: Recordatorio, tiempoRecordatorio: Long) {
        try {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("titulo", recordatorio.titulo)
                putExtra("fecha", recordatorio.fecha)
                putExtra("hora", recordatorio.hora)
                putExtra("lugar", recordatorio.lugar)
                putExtra("id", recordatorio.id)
            }

            val requestCode = recordatorio.id.hashCode() and 0x7FFFFFFF

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // ✅ USAR setExactAndAllowWhileIdle para mejor funcionamiento en segundo plano
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    tiempoRecordatorio,
                    pendingIntent
                )
                Log.d(TAG, "✅ AlarmManager programado con setExactAndAllowWhileIdle")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    tiempoRecordatorio,
                    pendingIntent
                )
                Log.d(TAG, "✅ AlarmManager programado con setExact")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error con AlarmManager: ${e.message}")
        }
    }

    fun cancelarAlarma(context: Context, recordatorio: Recordatorio) {
        try {
            Log.d(TAG, "🗑️ CANCELANDO ALARMA: ${recordatorio.titulo}")

            // 1. Cancelar timer
            activeTimers[recordatorio.id]?.cancel()
            activeTimers.remove(recordatorio.id)

            // 2. Cancelar AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = recordatorio.id.hashCode() and 0x7FFFFFFF

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            if (pendingIntent != null) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "✅ AlarmManager cancelado")
            }

            Log.d(TAG, "✅ Alarma completamente cancelada")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cancelando: ${e.message}")
        }
    }

    private fun mostrarAlarmaInmediata(context: Context, recordatorio: Recordatorio) {
        val mensaje = "Fecha: ${recordatorio.fecha} - Hora: ${recordatorio.hora}" +
                if (recordatorio.lugar.isNotEmpty()) " - Lugar: ${recordatorio.lugar}" else ""

        NotificationHelper.mostrarAlarmaInmediata(context, recordatorio.titulo, mensaje)
    }

    // ✅ MÉTODO DE PRUEBA INMEDIATA
    fun programarPrueba(context: Context) {
        try {
            Log.d(TAG, "🧪 PROGRAMANDO PRUEBA INMEDIATA")

            executor.schedule({
                Log.d(TAG, "🔥 PRUEBA EJECUTADA")
                NotificationHelper.mostrarAlarmaInmediata(
                    context,
                    "🔥 PRUEBA EXITOSA",
                    "Las alarmas funcionan perfectamente"
                )
            }, 10, TimeUnit.SECONDS)

            Log.d(TAG, "🧪 Prueba programada para 10 segundos")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en prueba: ${e.message}")
        }
    }
}