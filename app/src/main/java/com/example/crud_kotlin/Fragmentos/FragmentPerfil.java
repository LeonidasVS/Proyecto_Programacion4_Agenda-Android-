package com.example.crud_kotlin.Fragmentos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private FragmentPerfilBinding binding; // ✅ CORREGIDO
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // ✅ CORREGIDO: Usar el binding correcto
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

        fotoUsuario();

        if (uid != null) {
            progressDialog.setMessage("¡Cargando Usuario!");
            progressDialog.show();

            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            binding.NombreUsuario.setVisibility(View.GONE);
            binding.carreraUsuario.setVisibility(View.GONE);
            binding.correoUsuario.setVisibility(View.GONE);

            database.child("users").child(uid).get()
                    .addOnSuccessListener(snapshot -> {
                        progressDialog.dismiss();

                        if (snapshot.exists()) {
                            Registro usuario = snapshot.getValue(Registro.class);
                            if (usuario != null) {
                                binding.NombreUsuario.setText(usuario.getNombre() + " " + usuario.getApellido());
                                binding.correoUsuario.setText(usuario.getEmail());
                                binding.carreraUsuario.setText(usuario.getCarrera());

                                binding.NombreUsuario.setVisibility(View.VISIBLE);
                                binding.carreraUsuario.setVisibility(View.VISIBLE);
                                binding.correoUsuario.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(requireContext(), "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Error al obtener los datos", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "No hay usuario logueado", Toast.LENGTH_SHORT).show();
        }

        // Botón cerrar sesión
        binding.cerrarSesion.setOnClickListener(v -> cerrarSesion());

        // Botón editar perfil - ✅ CORREGIDO: Cambié el ID
        binding.btnEditarPerfil.setOnClickListener(v -> {
            FragmentUpdatePerfil fragmentUpdatePerfil = new FragmentUpdatePerfil();

            // Transacción para mostrar el nuevo fragmento
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentoFL, fragmentUpdatePerfil) // ✅ Asegúrate que este ID existe
                    .addToBackStack(null)
                    .commit();
        });

        // Botón eliminar cuenta - ✅ CORREGIDO: Cambié el ID
        binding.btnElminarCuenta.setOnClickListener(v -> eliminarUsuario());
    }

    private void eliminarUsuario() {
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
                        String password = input.getText() != null ? input.getText().toString().trim() : "";
                        String email = usuario.getEmail();

                        if (email != null && !password.isEmpty()) {
                            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

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

            // Aplicar tipografía al diálogo
            TextView messageView = dialogo.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppins_medium));
            }

            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppins_medium));
            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppins_medium));
        }
    }

    private void fotoUsuario() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Prioridad 1: Verificar si hay imagen en Avatar (usuario subió foto personalizada)
        if (Avatar.INSTANCE.getImagenUri() != null) {
            Picasso.get()
                    .load(Avatar.INSTANCE.getImagenUri())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(binding.ImagePerfil);

            binding.ImagePerfil.setVisibility(View.VISIBLE);
            binding.AvatarUsuario.setVisibility(View.GONE);
        }
        // Prioridad 2: Verificar si hay foto de perfil de Google/Firebase
        else if (currentUser != null && currentUser.getPhotoUrl() != null) {
            Picasso.get()
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(binding.ImagePerfil);

            binding.ImagePerfil.setVisibility(View.VISIBLE);
            binding.AvatarUsuario.setVisibility(View.GONE);
        }
        // Prioridad 3: Mostrar avatar con inicial y color
        else {
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);

            Integer avatarColor = Avatar.INSTANCE.getColor();
            drawable.setColor(avatarColor != null ? avatarColor : android.graphics.Color.GRAY);
            binding.AvatarUsuario.setBackground(drawable);

            String avatarLetra = Avatar.INSTANCE.getLetra();

            // Si no hay letra en Avatar, usar la inicial del nombre de Firebase
            if (avatarLetra == null && currentUser != null && currentUser.getDisplayName() != null) {
                avatarLetra = String.valueOf(currentUser.getDisplayName().charAt(0)).toUpperCase();
            }

            binding.AvatarUsuario.setText(avatarLetra != null ? avatarLetra : "?");
            binding.AvatarUsuario.setVisibility(View.VISIBLE);
            binding.ImagePerfil.setVisibility(View.GONE);
        }
    }

    private void cerrarSesion() {
        if (firebaseAuth.getCurrentUser() != null) {
            // Mostrar diálogo de carga
            progressDialog.setMessage("Cerrando sesión...");
            progressDialog.show();

            firebaseAuth.signOut();

            // Si también usas Google Sign-In, cierra sesión allí
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                progressDialog.dismiss();

                // Limpiar datos del avatar
                Avatar.INSTANCE.setColor(null);
                Avatar.INSTANCE.setLetra(null);
                Avatar.INSTANCE.setImagenUri(null);

                irAlLogin();
            });
        }
    }

    private void irAlLogin() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}