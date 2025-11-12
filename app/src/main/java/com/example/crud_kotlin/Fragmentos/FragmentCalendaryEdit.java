package com.example.crud_kotlin.Fragmentos;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FragmentCalendaryEdit extends Fragment {

    private EditText etTitulo, etLocation;
    private TextView etHora, etFecha;
    private Button btnActualizar;
    private FirebaseFirestore db;
    private Recordatorio recordatorio;

    public static FragmentCalendaryEdit newInstance(Recordatorio recordatorio) {
        FragmentCalendaryEdit fragment = new FragmentCalendaryEdit();
        Bundle args = new Bundle();
        // Usar putParcelable en lugar de putSerializable
        args.putParcelable("recordatorio", recordatorio);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendary_add, container, false);

        etTitulo = view.findViewById(R.id.et_event_title);
        etHora = view.findViewById(R.id.textViewHora);
        etFecha = view.findViewById(R.id.textViewFecha);
        etLocation = view.findViewById(R.id.et_location);
        btnActualizar = view.findViewById(R.id.btn_save);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            // Usar getParcelable en lugar de getSerializable
            recordatorio = getArguments().getParcelable("recordatorio");
            if (recordatorio != null) {
                etTitulo.setText(recordatorio.getTitulo());
                etHora.setText(recordatorio.getHora());
                etFecha.setText(recordatorio.getFecha());
                etLocation.setText(recordatorio.getLugar());
            }
        }

        btnActualizar.setText("Actualizar");
        btnActualizar.setOnClickListener(v -> actualizarRecordatorio());
        configurarPickers();

        return view;
    }

    private void configurarPickers() {
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

        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                (view, year, month, day) -> {
                    etFecha.setText(String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year));
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        etFecha.setOnClickListener(v -> datePicker.show());
    }

    private void actualizarRecordatorio() {
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

        // Crear un Map con los campos a actualizar
        Map<String, Object> updates = new HashMap<>();
        updates.put("titulo", titulo);
        updates.put("hora", hora);
        updates.put("fecha", fecha);
        updates.put("lugar", lugar);
        updates.put("usuario", usuario.getUid());

        db.collection("recordatorios")
                .document(recordatorio.getId())
                .update(updates) // Usar update() en lugar de set()
                .addOnSuccessListener(aVoid -> {
                    // Crear el recordatorio actualizado para la alarma
                    Recordatorio recordatorioActualizado = new Recordatorio(titulo, hora, fecha, usuario.getUid(), lugar, recordatorio.getId());

                    AlarmUtils.INSTANCE.cancelarAlarma(requireContext(), recordatorio);
                    AlarmUtils.INSTANCE.programarAlarma(requireContext(), recordatorioActualizado);

                    Toast.makeText(requireContext(), "Recordatorio actualizado", Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}