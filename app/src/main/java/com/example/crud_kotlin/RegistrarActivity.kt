package com.example.crud_kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crud_kotlin.Modelos.Registro
import com.example.crud_kotlin.databinding.ActivityDashboardBinding
import com.example.crud_kotlin.databinding.ActivityRegistrarBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistrarActivity : AppCompatActivity() {

    // Variable para inicializar el binding
    private lateinit var binding: ActivityRegistrarBinding

    //Variable para iniciar FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityRegistrarBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        setupCarreraSpinner()

        loadFieldsForEndingRegister()


        // Apuntnado a cada ID del layout activity_registrar
        val etNombre = findViewById<EditText>(R.id.inputNombre)
        val etApellido = findViewById<EditText>(R.id.inputApellido)
        val etCorreo = findViewById<EditText>(R.id.inputMail)
        val etPassword = findViewById<EditText>(R.id.inputPass)
        val etCarrera = findViewById<AutoCompleteTextView>(R.id.spinnerCarrera)


        // Metodo del boton
        binding.botonRegistrar.setOnClickListener {

            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val carrera = etCarrera.text.toString().trim()
            val contra = etPassword.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || carrera.isEmpty()) {
                Toast.makeText(this, "¡Completa los campos vacíos!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            val isGoogleUser = user?.providerData?.any { it.providerId == "google.com" } ?: false

            if (isGoogleUser) {
                // ✅ Usuario Google → NO usar createUserWithEmailAndPassword
                val uid = user?.uid ?: return@setOnClickListener
                val newUser = Registro(nombre, apellido, correo, carrera)

                FirebaseDatabase.getInstance().getReference("users")
                    .child(uid)
                    .setValue(newUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Registro completado con Google.", Toast.LENGTH_LONG)
                            .show()

                        startActivity(Intent(this, DashboardActivity::class.java))
                        enableInputFields()
                        finish()

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error guardando: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                    }

            } else {
                // ✅ Usuario Email/Password → registrar normalmente
                if (contra.length < 6) {
                    Toast.makeText(
                        this,
                        "¡La contraseña debe tener más de 6 dígitos!",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                auth.createUserWithEmailAndPassword(correo, contra)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                            val userDB = Registro(nombre, apellido, correo, carrera)

                            FirebaseDatabase.getInstance().getReference("users")
                                .child(uid)
                                .setValue(userDB)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registro exitoso.", Toast.LENGTH_LONG)
                                        .show()
                                    startActivity(Intent(this, DashboardActivity::class.java))
                                    enableInputFields()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Error guardando: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                        } else {
                            Toast.makeText(
                                this,
                                task.exception?.message ?: "Error al registrar",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }
    }

        private fun loadFieldsForEndingRegister() {

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val uid = user.uid                 // ID único de usuario
            val email = user.email             // correo del usuario
            val displayName = user.displayName.toString() // nombre completo (solo si lo configuraste)
            val photoUrl = user.photoUrl       // URL de la foto de perfil
            val isEmailVerified = user.isEmailVerified


            val partes = displayName.trim().split("\\s+".toRegex())

            val nombre = if (partes.size >= 2) "${partes[0]} ${partes[1]}" else partes.getOrNull(0) ?: ""
            val apellido = if (partes.size >= 4) "${partes[2]} ${partes[3]}"
            else if (partes.size == 3) partes[2]
            else ""



            binding.inputNombre.setText(nombre)
            binding.inputApellido.setText(apellido)
            binding.inputMail.setText(email)
            disableInputFields()


        }



    }



    private fun disableInputFields() {

        binding.inputNombre.isEnabled = false
        binding.inputApellido.isEnabled = false
        binding.inputMail.isEnabled = false
        binding.inputPass.isEnabled = false
        binding.inputPass.visibility = Button.GONE


    }

    private fun enableInputFields() {

        binding.inputNombre.isEnabled = true
        binding.inputApellido.isEnabled = true
        binding.inputMail.isEnabled = true
        binding.inputPass.isEnabled = true
        binding.spinnerCarrera.isEnabled = true
        binding.inputPass.visibility = Button.VISIBLE
    }



    //metoodo para el llenado del selectitem
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

        val autoComplete = findViewById<AutoCompleteTextView>(R.id.spinnerCarrera)

        // Crear adapter
        val adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, carreras)
        autoComplete.setAdapter(adapter)

        // Configurar comportamiento como spinner
        autoComplete.setOnClickListener {
            autoComplete.showDropDown()
        }

        // Manejar selección
        autoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedCarrera = carreras[position]
            // Procesar selección
            Toast.makeText(this, "Seleccionado: $selectedCarrera", Toast.LENGTH_SHORT).show()
        }
    }


}