package com.example.agenda_kotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.agenda_kotlin.databinding.ActivityRegistroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRegistroBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //Hacer conexion con el layou
        binding=ActivityRegistroBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Cargar el spinner
        cargarCarreras()

        //Instancia de Firebase
        firebaseAuth=FirebaseAuth.getInstance()

        //Instancia del progressDialog
        progressDialog=ProgressDialog(this)
        progressDialog.setTitle("Espere por favor...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.botonRegistrar.setOnClickListener {
            validarInformacion()
        }

    }

    //Varibles globales
    private var nombres=""
    private var apellidos=""
    private var correo=""
    private var password=""
    private var carrera=""


    private fun validarInformacion() {
        nombres=binding.inputNombre.text.toString().trim()
        apellidos=binding.inputApellido.text.toString().trim()
        correo=binding.inputMail.text.toString().trim()
        password=binding.inputPass.text.toString().trim()
        carrera=binding.spinnerCarrera.text.toString().trim()

        if(nombres.isEmpty()){
            binding.inputNombre.error="Ingresa un Nombre"
            binding.inputNombre.requestFocus()
        }else if(apellidos.isEmpty()){
            binding.inputApellido.error="Ingresa un apellido"
            binding.inputApellido.requestFocus()
        }else if(correo.isEmpty()){
            binding.inputMail.error="Ingresa un correo"
            binding.inputMail.requestFocus()
        }else if(!Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
            binding.inputMail.error="¡Ingresa un correo valido!"
            binding.inputMail.requestFocus()
        }else if(password.isEmpty() || password.length>6){
            binding.inputPass.error="Contraseña mayor de 6 digitos"
            binding.inputPass.requestFocus()
        }else if(carrera.isEmpty() || carrera=="-- Seleccione una carrera --"){
            Toast.makeText(this, "Selecciona una carrea", Toast.LENGTH_SHORT).show()
        }else{
            //Mandar a llamar la funcion para que cree el usuario si todos los campos cumplen
            registrarUsuarioEmail()
        }
    }

    private fun registrarUsuarioEmail() {
        progressDialog.setMessage("Creando Usuario")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener {
                crearUsuarioEnFireBase()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al crear cuenta, debido a: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun crearUsuarioEnFireBase(){
        val uid=firebaseAuth.uid
        val nombres=nombres
        val correo=firebaseAuth.currentUser!!.email

        val enviarDatos=HashMap<String,Any>()
        enviarDatos["uid"]="${uid}"
        enviarDatos["nombre"]="${nombres}"
        enviarDatos["apellido"]="${apellidos}"
        enviarDatos["correo"]="${correo}"
        enviarDatos["carrera"]="${carrera}"
        enviarDatos["proveedor"]="Email"

        //Guardar la informacion
        val refencia=FirebaseDatabase.getInstance().getReference("users")
        refencia.child(uid!!)
            .setValue(enviarDatos)
            .addOnCompleteListener {
                progressDialog.dismiss()
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun cargarCarreras() {
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
    }
}