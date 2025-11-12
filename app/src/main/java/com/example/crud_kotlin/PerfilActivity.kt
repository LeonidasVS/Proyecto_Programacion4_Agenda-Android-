package com.example.crud_kotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crud_kotlin.Modelos.Registro
import com.example.crud_kotlin.Objetos.Avatar
import com.example.crud_kotlin.databinding.ActivityPerfilBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class PerfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPerfilBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Espere por favor...")
        progressDialog.setCanceledOnTouchOutside(false)

        val uid=firebaseAuth.currentUser?.uid

        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fotoUsuario()

        if (uid != null) {
            progressDialog.setMessage("¡Cargando Usuario!")
            progressDialog.show()  // 🔹 Mostrar el diálogo

            val database = FirebaseDatabase.getInstance().reference
            binding.NombreUsuario.visibility = View.GONE
            binding.carreraUsuario.visibility = View.GONE
            binding.correoUsuario.visibility = View.GONE

            database.child("users").child(uid).get()
                .addOnSuccessListener { snapshot ->
                    progressDialog.dismiss()  // 🔹 Ocultar al terminar

                    if (snapshot.exists()) {
                        val usuario = snapshot.getValue(Registro::class.java)
                        usuario?.let {
                            binding.NombreUsuario.text = "${it.nombre} ${it.apellido}"
                            binding.correoUsuario.text = it.email
                            binding.carreraUsuario.text = it.carrera

                            binding.NombreUsuario.visibility = View.VISIBLE
                            binding.carreraUsuario.visibility = View.VISIBLE
                            binding.correoUsuario.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    progressDialog.dismiss()  // 🔹 Ocultar si falla
                    Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show()
        }


        // Botón cerrar sesión
        binding.cerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        binding.btnEditarPerfil.setOnClickListener {
            startActivity(Intent(this, ActualizarActivity::class.java))
        }

        binding.btnElminarCuenta.setOnClickListener {
            eliminarUsuario()
        }
    }

    private fun eliminarUsuario() {
        val usuario = firebaseAuth.currentUser
        val database = FirebaseDatabase.getInstance().getReference("users")
        val tipografiaInput= ResourcesCompat.getFont(this, R.font.poppins_light)

        if (usuario != null) {
            // Crear TextInputLayout y TextInputEditText
            val passwordLayout = TextInputLayout(this).apply {
                hint = "Contraseña..."
                isPasswordVisibilityToggleEnabled = true
                setPadding(16, 16, 16, 16)
                boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            }

            val input = TextInputEditText(passwordLayout.context).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                typeface=tipografiaInput
            }

            passwordLayout.addView(input)

            // Construir el AlertDialog
            val dialogo= AlertDialog.Builder(this)
                .setTitle("¿Deseas eliminar tu cuenta?")
                .setMessage("¡Ingresa tu contraseña para confirmar!")
                .setView(passwordLayout)
                .setIcon(R.drawable.ic_advertencia)
                .setPositiveButton("Si, continuar") { _, _ ->
                    val password = input.text.toString().trim()
                    val email = usuario.email

                    if (email != null && password.isNotEmpty()) {
                        val credential = EmailAuthProvider.getCredential(email, password)

                        // Mostrar ProgressDialog indicando eliminación
                        progressDialog.setMessage("Eliminando cuenta, por favor espera...")
                        progressDialog.show()

                        // Reautenticar al usuario
                        usuario.reauthenticate(credential).addOnSuccessListener {
                            // Eliminar datos del usuario en la base de datos
                            database.child(usuario.uid).removeValue().addOnSuccessListener {
                                // Eliminar usuario de Firebase Auth
                                usuario.delete().addOnSuccessListener {
                                    progressDialog.dismiss() // 🔹 Ocultar ProgressDialog
                                    startActivity(Intent(this, MainActivity::class.java)
                                        .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
                                    finish()
                                }.addOnFailureListener {
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "Error al eliminar cuenta", Toast.LENGTH_SHORT).show()
                                }
                            }.addOnFailureListener {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Error al eliminar datos", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Debes ingresar tu contraseña", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No, cancelar", null)
                .show()

            val tipografia= ResourcesCompat.getFont(this, R.font.poppins_medium)

            dialogo.findViewById<TextView>(android.R.id.message)?.typeface=tipografia

            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).typeface=tipografia
            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).typeface=tipografia

        }
    }

    private fun fotoUsuario(){

        if (Avatar.imagenUri != null) {
            Picasso.get().load(Avatar.imagenUri).into(binding.ImagePerfil)
        } else {
            val drawable = android.graphics.drawable.GradientDrawable()
            drawable.shape = android.graphics.drawable.GradientDrawable.OVAL
            drawable.setColor(Avatar.color ?: android.graphics.Color.GRAY)
            binding.AvatarUsuario.background = drawable

            binding.AvatarUsuario.text = Avatar.letra
            binding.ImagePerfil.visibility = View.GONE
        }

    }

    private fun cerrarSesion() {
        if (firebaseAuth.currentUser != null) {
            firebaseAuth.signOut()

            // Si también usas Google Sign-In, cierra sesión allí
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut()

            // Redirige al LoginActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            finish()
        }
        Avatar.color = null
        Avatar.letra= null
        Avatar.imagenUri=null
        irAlLogin()
    }

    private fun irAlLogin() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

}