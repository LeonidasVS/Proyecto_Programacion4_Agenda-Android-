package com.example.agenda_kotlin

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.agenda_kotlin.Objeto.Avatar
import com.example.agenda_kotlin.databinding.ActivityPerfilBinding
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

        binding.btnEditarPerfil.setOnClickListener {
            val intent=Intent(this, ActualizarPerfilActivity::class.java)
            startActivity(intent)
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