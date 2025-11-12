package com.example.crud_kotlin.Fragmentos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.crud_kotlin.DashboardActivity;
import com.example.crud_kotlin.databinding.FragmentUpdatePerfilBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FragmentUpdatePerfil extends Fragment {

    private FragmentUpdatePerfilBinding binding;
    private FirebaseAuth fireAuth;
    private FirebaseDatabase db;

    private String nombre = "";
    private String apellido = "";
    private String correo = "";
    private String carrera = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUpdatePerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();



    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fireAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        // Rellenar el spinner de carreras universitarias
        setupCarreraSpinner();

        // Método para cargar usuarios
        cargarUsuario();

        // Botón actualizar
        binding.botonActualizarCuenta.setOnClickListener(v -> validarYactualizar());
    }

    private void validarYactualizar() {
        if (validarCampos()) {
            FirebaseUser user = fireAuth.getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                DatabaseReference ref = db.getReference("users").child(uid);

                Map<String, Object> userMap = new HashMap<>();
                userMap.put("nombre", nombre);
                userMap.put("apellido", apellido);
                userMap.put("carrera", carrera);

                user.updateEmail(correo)
                        .addOnSuccessListener(aVoid -> {
                            ref.updateChildren(userMap)
                                    .addOnSuccessListener(aVoid1 -> {
                                        Toast.makeText(requireContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();

                                        // Navegar al Dashboard o volver atrás
                                        Intent intent = new Intent(requireContext(), DashboardActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        requireActivity().finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(requireContext(), "Error al actualizar en la base de datos", Toast.LENGTH_SHORT).show()
                                    );
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(requireContext(), "Error al actualizar el correo en FirebaseAuth", Toast.LENGTH_SHORT).show()
                        );
            }
        }
    }

    private boolean validarCampos() {
        nombre = binding.inputNombre.getText() != null ? binding.inputNombre.getText().toString().trim() : "";
        apellido = binding.inputApellido.getText() != null ? binding.inputApellido.getText().toString().trim() : "";
        carrera = binding.spinnerCarrera.getText() != null ? binding.spinnerCarrera.getText().toString().trim() : "";

        boolean valido = true;

        if (nombre.isEmpty()) {
            binding.inputNombre.setError("Ingrese su nombre");
            valido = false;
        }
        if (apellido.isEmpty()) {
            binding.inputApellido.setError("Ingrese su apellido");
            valido = false;
        }
        if (carrera.isEmpty() || carrera.equals("-- Seleccione una carrera --")) {
            binding.spinnerCarrera.setError("Seleccione una carrera");
            valido = false;
        }

        return valido;
    }

    private void cargarUsuario() {
        FirebaseUser user = fireAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference ref = db.getReference("users").child(uid);

            ref.get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            nombre = snapshot.child("nombre").getValue() != null ?
                                    snapshot.child("nombre").getValue().toString() : "";
                            apellido = snapshot.child("apellido").getValue() != null ?
                                    snapshot.child("apellido").getValue().toString() : "";
                            correo = snapshot.child("email").getValue() != null ?
                                    snapshot.child("email").getValue().toString() : "";
                            carrera = snapshot.child("carrera").getValue() != null ?
                                    snapshot.child("carrera").getValue().toString() : "";

                            binding.inputNombre.setText(nombre);
                            binding.inputApellido.setText(apellido);
                            binding.inputMail.setText(correo);
                            binding.inputMail.setEnabled(false);
                            binding.spinnerCarrera.setText(carrera, false);
                        } else {
                            Toast.makeText(requireContext(), "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(requireContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCarreraSpinner() {
        String[] carreras = {
                "-- Seleccione una carrera --",
                "Licenciatura en Psicología",
                "Licenciatura en Nutrición",
                "Licenciatura en Trabajo Social",
                "Licenciatura en Enfermería",
                "Tecnólogo en Enfermería",
                "Técnico en Enfermería",
                "Técnico en Optometría",
                "Técnico en Mercadeo",
                "Técnico en Idioma Inglés",
                "Técnico en Computación",
                "Técnico en Contabilidad",
                "Técnico en Diseño Gráfico",
                "Ingeniería en Sistemas y Computación"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                carreras
        );
        binding.spinnerCarrera.setAdapter(adapter);

        binding.spinnerCarrera.setOnClickListener(v ->
                binding.spinnerCarrera.showDropDown()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}