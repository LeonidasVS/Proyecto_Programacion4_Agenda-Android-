package com.example.crud_kotlin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "🔥 ALARMA RECIBIDA (Segundo plano)");

        String titulo = intent.getStringExtra("titulo");
        String fecha = intent.getStringExtra("fecha");
        String hora = intent.getStringExtra("hora");
        String lugar = intent.getStringExtra("lugar");

        if (titulo == null) {
            titulo = "Recordatorio";
        }

        String mensaje = "Fecha: " + fecha + " - Hora: " + hora;
        if (lugar != null && !lugar.isEmpty()) {
            mensaje += " - Lugar: " + lugar;
        }

        Log.d(TAG, "📢 Mostrando alarma: " + titulo);

        // Usar NotificationHelper
        NotificationHelper.INSTANCE.mostrarAlarmaInmediata(context, titulo, mensaje);
    }
}