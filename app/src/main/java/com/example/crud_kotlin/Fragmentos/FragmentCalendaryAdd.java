package com.example.crud_kotlin.Fragmentos;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.crud_kotlin.AlarmUtils;
import com.example.crud_kotlin.Modelos.Recordatorio;
import com.example.crud_kotlin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FragmentCalendaryAdd extends Fragment {

    private EditText etTitulo, etLocation;
    private TextView etHora, etFecha;
    private Button btnGuardar;

    private Button cancelarNota;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendary_add, container, false);

        etTitulo = view.findViewById(R.id.et_event_title);
        etHora = view.findViewById(R.id.textViewHora);
        etFecha = view.findViewById(R.id.textViewFecha);
        etLocation = view.findViewById(R.id.et_location);
        btnGuardar = view.findViewById(R.id.btn_save);
        cancelarNota = view.findViewById(R.id.btn_cancel);

        db = FirebaseFirestore.getInstance();

        btnGuardar.setOnClickListener(v -> guardarRecordatorio());
        cancelarNota.setOnClickListener(v -> volverAtras());
        configurarPickers();

        return view;
    }

    private void configurarPickers() {
        // TimePicker
        Calendar cal = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                (view, hour, minute) -> {
                    String amPm = hour >= 12 ? "PM" : "AM";
                    int hourFormatted = hour % 12;
                    if (hourFormatted == 0) hourFormatted = 12;
                    etHora.setText(String.format(Locale.US, "%02d:%02d %s", hourFormatted, minute, amPm));
                },
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);

        etHora.setOnClickListener(v -> timePicker.show());

        // DatePicker
        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                (view, year, month, day) -> {
                    etFecha.setText(String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year));
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        etFecha.setOnClickListener(v -> datePicker.show());
    }

    private void guardarRecordatorio() {
        String titulo = etTitulo.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String lugar = etLocation.getText().toString().trim();

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (titulo.isEmpty() || hora.isEmpty() || fecha.isEmpty()) {
            Toast.makeText(requireContext(), "Llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarFechaHora(fecha, hora)) {
            Toast.makeText(requireContext(), "No puedes crear recordatorios en el pasado", Toast.LENGTH_LONG).show();
            return;
        }

        Recordatorio recordatorio = new Recordatorio(titulo, hora, fecha, usuario.getUid(), lugar, "");

        db.collection("recordatorios")
                .add(recordatorio)
                .addOnSuccessListener(docRef -> {
                    String docId = docRef.getId();

                    // Actualizar con ID
                    docRef.update("id", docId)
                            .addOnSuccessListener(aVoid -> {
                                Recordatorio recordatorioConId = new Recordatorio(titulo, hora, fecha, usuario.getUid(), lugar, docId);

                                // Programar alarma
                                AlarmUtils.INSTANCE.programarAlarma(requireContext(), recordatorioConId);

                                Toast.makeText(requireContext(), "Recordatorio guardado", Toast.LENGTH_LONG).show();
                                limpiarCampos();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validarFechaHora(String fecha, String hora) {
        try {
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US);
            Date fechaHora = formato.parse(fecha + " " + hora);
            return fechaHora != null && fechaHora.getTime() > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    private void limpiarCampos() {
        etTitulo.setText("");
        etHora.setText("");
        etFecha.setText("");
        etLocation.setText("");
    }

    private void volverAtras() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}