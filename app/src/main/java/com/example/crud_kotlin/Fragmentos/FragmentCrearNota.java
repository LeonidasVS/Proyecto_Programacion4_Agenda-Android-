package com.example.crud_kotlin.Fragmentos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.crud_kotlin.Modelos.Nota;
import com.example.crud_kotlin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FragmentCrearNota extends Fragment {

    private EditText etTitulo, etContenido;
    private Button btnGuardar, btnCancelar;
    private TextView tvCharCount;
    private FirebaseFirestore db;
    private String notaId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_nota, container, false);

        // Inicializar vistas
        etTitulo = view.findViewById(R.id.et_note_title);
        etContenido = view.findViewById(R.id.et_note_content);
        btnGuardar = view.findViewById(R.id.btn_save_note);
        btnCancelar = view.findViewById(R.id.btn_cancel_note);
        tvCharCount = view.findViewById(R.id.tv_char_count);

        db = FirebaseFirestore.getInstance();

        // Configurar contador de caracteres
        etContenido.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                tvCharCount.setText(s.length() + "/1000 caracteres");
            }
        });

        // Verificar si es edición
        if (getArguments() != null) {
            notaId = getArguments().getString("nota_id");
            etTitulo.setText(getArguments().getString("titulo"));
            etContenido.setText(getArguments().getString("contenido"));
            tvCharCount.setText(etContenido.getText().length() + "/1000 caracteres");
        }

        // Listeners
        btnGuardar.setOnClickListener(v -> guardarNota());
        btnCancelar.setOnClickListener(v -> volverAtras());

        return view;
    }

    private void guardarNota() {
        String titulo = etTitulo.getText().toString().trim();
        String contenido = etContenido.getText().toString().trim();
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (titulo.isEmpty()) {
            etTitulo.setError("El título es requerido");
            etTitulo.requestFocus();
            return;
        }

        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        if (notaId != null) {
            // EDITAR
            db.collection("notas").document(notaId)
                    .update("titulo", titulo, "contenido", contenido, "fechaCreacion", fecha)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Nota actualizada", Toast.LENGTH_SHORT).show();
                        volverAtras();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // CREAR
            Nota nota = new Nota(titulo, contenido, fecha, usuario.getUid(), "");
            db.collection("notas")
                    .add(nota)
                    .addOnSuccessListener(docRef -> {
                        docRef.update("id", docRef.getId());
                        Toast.makeText(requireContext(), "Nota guardada", Toast.LENGTH_SHORT).show();
                        volverAtras();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void volverAtras() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}