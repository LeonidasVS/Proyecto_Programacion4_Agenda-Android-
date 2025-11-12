package com.example.crud_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crud_kotlin.databinding.ActivityActualizarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ActualizarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityActualizarBinding
    private lateinit var fireAuth: FirebaseAuth
    private lateinit var db: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityActualizarBinding.inflate(layoutInflater)
        fireAuth= FirebaseAuth.getInstance()
        db= FirebaseDatabase.getInstance()

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

        //Rellenar el spiner de carreras universitarias
        setupCarreraSpinner()

        //Metodo para cargar usuarios
        cargarUsuario()

        binding.botonActualizarCuenta.setOnClickListener {
            validarYactualizar()
        }
    }

    private var nombre = ""
    private var apellido = ""
    private var correo = ""
    private var carrera = ""

    private fun validarYactualizar() {
        validarCampos()
        if (validarCampos()) {
            val user = fireAuth.currentUser
            if (user != null) {
                val uid = user.uid
                val ref = db.getReference("users").child(uid)

                val userMap = mapOf(
                    "nombre" to nombre,
                    "apellido" to apellido,
                    "carrera" to carrera
                )

                user.updateEmail(correo)
                    .addOnSuccessListener {
                        ref.updateChildren(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, DashboardActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al actualizar en la base de datos", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar el correo en FirebaseAuth", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun validarCampos(): Boolean {
        nombre = binding.inputNombre.text.toString().trim()
        apellido = binding.inputApellido.text.toString().trim()
        carrera = binding.spinnerCarrera.text.toString().trim()

        var valido = true

        if (nombre.isEmpty()) {
            binding.inputNombre.error = "Ingrese su nombre"
            valido = false
        }
        if (apellido.isEmpty()) {
            binding.inputApellido.error = "Ingrese su apellido"
            valido = false
        }
        if (carrera.isEmpty() || carrera == "-- Seleccione una carrera --") {
            binding.spinnerCarrera.error = "Seleccione una carrera"
            valido = false
        }

        return valido
    }

    private fun cargarUsuario() {
        val user = fireAuth.currentUser
        if (user != null) {
            val uid = user.uid
            val ref = db.getReference("users").child(uid)

            ref.get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        nombre = snapshot.child("nombre").value.toString()
                        apellido = snapshot.child("apellido").value.toString()
                        correo = snapshot.child("email").value.toString()
                        carrera = snapshot.child("carrera").value.toString()

                        binding.inputNombre.setText(nombre)
                        binding.inputApellido.setText(apellido)
                        binding.inputMail.setText(correo)
                        binding.inputMail.isEnabled = false
                        binding.spinnerCarrera.setText(carrera, false)
                    } else {
                        Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupCarreraSpinner() {
        val carreras = arrayOf(
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
        )

        val adapter = ArrayAdapter(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            carreras
        )
        binding.spinnerCarrera.setAdapter(adapter)

        binding.spinnerCarrera.setOnClickListener {
            binding.spinnerCarrera.showDropDown()
        }
    }
}