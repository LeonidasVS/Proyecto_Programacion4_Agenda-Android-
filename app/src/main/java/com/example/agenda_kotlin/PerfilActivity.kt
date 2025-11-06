package com.example.agenda_kotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.agenda_kotlin.databinding.ActivityPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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
            progressDialog.setMessage("¡Cerrando Sesion!")
            progressDialog.show()  // 🔹 Mostrar el diálogo
            cerrarSesion()
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

                } else {
                    Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()  // 🔹 Ocultar si falla
                Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cerrarSesion() {
        progressDialog.dismiss()
        firebaseAuth.signOut()
        val intent=Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}