package com.example.crud_kotlin.Fragmentos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.crud_kotlin.MainActivity;
import com.example.crud_kotlin.Modelos.Registro;
import com.example.crud_kotlin.Objetos.Avatar;
import com.example.crud_kotlin.R;
import com.example.crud_kotlin.databinding.FragmentPerfilBinding; // ✅ CORREGIDO
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FragmentPerfil extends Fragment {

    private FragmentPerfilBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Espere por favor...");
        progressDialog.setCanceledOnTouchOutside(false);

        String uid = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;

        if (uid != null) {
            buscarUsuarioLogeado(uid);
        } else {
            cerrarSesion();
        }

        binding.btnElminarCuenta.setOnClickListener(v -> cerrarSesion());

        // Evento editar perfil
        binding.btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FragmentPerfilBinding.class);
            startActivity(intent);
        });

        binding.btnCerrarSesion.setOnClickListener( c -> {
            cerrarSesion();
        });


        // Eliminar cuenta si es de EMAIL
        binding.btnElminarCuenta.setOnClickListener(v -> eliminarUsuarioDeEmail());
    }

    private void buscarUsuarioLogeado(String uid) {
        progressDialog.setMessage("¡Cargando Usuario!");
        progressDialog.show();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        database.child("users").child(uid).get()
                .addOnSuccessListener(snapshot -> {
                    progressDialog.dismiss();
                    if (snapshot.exists()) {
                        String nombre = snapshot.child("nombre").getValue(String.class);
                        String apellido = snapshot.child("apellido").getValue(String.class);
                        String correo = snapshot.child("correo").getValue(String.class);
                        String carrera = snapshot.child("carrera").getValue(String.class);
                        String proveedor = snapshot.child("proveedor").getValue(String.class);

                        if ("Google".equals(proveedor)) {
                            binding.btnElminarCuenta.setVisibility(View.GONE);
                        }

                        binding.NombreUsuario.setText(nombre + " " + apellido);
                        binding.correoUsuario.setText(correo);
                        binding.carreraUsuario.setText(carrera);

                        // Cargar foto de usuario
                        cargarFotoUsuario();

                    } else {
                        Toast.makeText(requireContext(), "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                        cerrarSesion();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Error al obtener los datos", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarFotoUsuario() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            Uri uri = user.getPhotoUrl();
            // Mostrar la imagen en el ImageView con Picasso
            Picasso.get()
                    .load(uri)
                    .into(binding.ImagePerfil);
        }else {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Avatar.INSTANCE.getColorAvatar() != null ? Avatar.INSTANCE.getColorAvatar() : Color.GRAY);
            binding.AvatarUsuario.setBackground(drawable);

            binding.AvatarUsuario.setText(Avatar.INSTANCE.getLetra());
            binding.ImagePerfil.setVisibility(View.GONE);
        }
    }

    private void eliminarUsuarioDeEmail() {
        FirebaseUser usuario = firebaseAuth.getCurrentUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

        if (usuario != null) {
            // Crear TextInputLayout y TextInputEditText
            TextInputLayout passwordLayout = new TextInputLayout(requireContext());
            passwordLayout.setHint("Contraseña...");
            passwordLayout.setPasswordVisibilityToggleEnabled(true);
            passwordLayout.setPadding(16, 16, 16, 16);
            passwordLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

            TextInputEditText input = new TextInputEditText(passwordLayout.getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppins_light));

            passwordLayout.addView(input);

            // Construir el AlertDialog
            AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                    .setTitle("¿Deseas eliminar tu cuenta?")
                    .setMessage("¡Ingresa tu contraseña para confirmar!")
                    .setView(passwordLayout)
                    .setIcon(R.drawable.ic_advertencia)
                    .setPositiveButton("Si, continuar", (dialog, which) -> {
                        String password = input.getText().toString().trim();
                        String email = usuario.getEmail();

                        if (email != null && !password.isEmpty()) {
                            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                            // Mostrar ProgressDialog indicando eliminación
                            progressDialog.setMessage("Eliminando cuenta, por favor espera...");
                            progressDialog.show();

                            // Reautenticar al usuario
                            usuario.reauthenticate(credential).addOnSuccessListener(aVoid -> {
                                // Eliminar datos del usuario en la base de datos
                                database.child(usuario.getUid()).removeValue().addOnSuccessListener(aVoid1 -> {
                                    // Eliminar usuario de Firebase Auth
                                    usuario.delete().addOnSuccessListener(aVoid2 -> {
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(requireContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        requireActivity().finish();
                                    }).addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(requireContext(), "Error al eliminar cuenta", Toast.LENGTH_SHORT).show();
                                    });
                                }).addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(requireContext(), "Error al eliminar datos", Toast.LENGTH_SHORT).show();
                                });
                            }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(requireContext(), "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            Toast.makeText(requireContext(), "Debes ingresar tu contraseña", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No, cancelar", null)
                    .show();

            TextView messageView = dialogo.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppins_medium));
            }

            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppins_medium));
            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppins_medium));
        }
    }

    private void cerrarSesion() {
        firebaseAuth.signOut();

        Avatar.INSTANCE.setImagenUri(null);
        Avatar.INSTANCE.setColorAvatar(null);
        Avatar.INSTANCE.setLetra(null);

        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
        requireActivity().finishAffinity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}