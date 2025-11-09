package com.example.agenda_kotlin

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.agenda_kotlin.Objeto.Avatar
import com.example.agenda_kotlin.databinding.ActivityPerfilBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPerfilBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding=ActivityPerfilBinding.inflate(layoutInflater)
        firebaseAuth=FirebaseAuth.getInstance()

        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Espere por favor...")
        progressDialog.setCanceledOnTouchOutside(false)

        var uid=firebaseAuth.currentUser?.uid

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if(uid!=null){
            buscarUsuarioLogeado(uid)
        }
        else {
            Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show()
        }

        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }

        //Evento editar perfil
        binding.btnEditarPerfil.setOnClickListener {
            val intent=Intent(this, ActualizarPerfilActivity::class.java)
            startActivity(intent)
        }

        //Eliminar cuenta si es de EMAIL
        binding.btnElminarCuenta.setOnClickListener {
            eliminarUsuarioDeEmail()
        }
    }


    private fun buscarUsuarioLogeado(uid: String) {
        progressDialog.setMessage("¡Cargando Usuario!")
        progressDialog.show()  // 🔹 Mostrar el diálogo

        val database=FirebaseDatabase.getInstance().reference

        database.child("users").child(uid).get()
            .addOnSuccessListener { snapshot ->
                progressDialog.dismiss()
                if (snapshot.exists()) {
                    val nombre=snapshot.child("nombre").getValue(String::class.java)
                    val apellido=snapshot.child("apellido").getValue(String::class.java)
                    val correo=snapshot.child("correo").getValue(String::class.java)
                    val carrera=snapshot.child("carrera").getValue(String::class.java)
                    val proveedor=snapshot.child("proveedor").getValue(String::class.java)

                    if(proveedor=="Google"){
                        binding.btnElminarCuenta.visibility=View.GONE
                    }

                    binding.NombreUsuario.text="${nombre} ${apellido}"
                    binding.correoUsuario.text="${correo}"
                    binding.carreraUsuario.text="${carrera}"

                    //Cagar foto de usuario
                    cargarFotoUsuario()

                } else {
                    Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                    cerrarSesion()
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()  // 🔹 Ocultar si falla
                Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarFotoUsuario() {

        if (Avatar.imagenUri != null) {
            Picasso.get().load(Avatar.imagenUri).into(binding.ImagePerfil)
        } else {
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(Avatar.colorAvatar ?: Color.GRAY)
            binding.AvatarUsuario.background = drawable

            binding.AvatarUsuario.text = Avatar.letra
            binding.ImagePerfil.visibility = View.GONE
        }

    }

    private fun eliminarUsuarioDeEmail(){
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

    private fun cerrarSesion() {
        firebaseAuth.signOut()

        Avatar.colorAvatar = null
        Avatar.letra= null
        Avatar.imagenUri=null

        val intent=Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}