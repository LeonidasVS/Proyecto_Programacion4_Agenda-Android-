package com.example.agenda_kotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.agenda_kotlin.R
import com.example.agenda_kotlin.databinding.ActivityRegistroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase


class RegistroActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRegistroBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    // Variables para datos de Google
    private var idTokenGoogle: String? = null
    private var esNuevoUsuarioGoogle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //Hacer conexion con el layout
        binding=ActivityRegistroBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Ocultar Boton De Google
        binding.botonRegistrarConGoogle.visibility= View.GONE

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

        //Datos De Login
        val desdeGoogle=intent.getBooleanExtra("datosDeGoogle", false)

        if(desdeGoogle){
            // Guardar el idToken para usarlo después
            idTokenGoogle = intent.getStringExtra("idToken")
            esNuevoUsuarioGoogle = intent.getBooleanExtra("esNuevoUsuario", false)
            cargarDatosDeGoogle()
        }

        // Evento para el botón de registrar con Google
        binding.botonRegistrarConGoogle.setOnClickListener {
            validarYRegistrarConGoogle()
        }
    }


    //Variables globales
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
        }else if(password.isEmpty() || password.length<6){
            binding.inputPass.error="Contraseña mayor de 6 digitos"
            binding.inputPass.requestFocus()
        }else if(carrera.isEmpty() || carrera=="-- Seleccione una carrera --"){
            Toast.makeText(this, "Selecciona una carrera", Toast.LENGTH_SHORT).show()
        }else{
            //Mandar a llamar la funcion para que cree el usuario si todos los campos cumplen
            validarCorreoExistente()
        }
    }

    //Validar información para registro con Google
    private fun validarYRegistrarConGoogle() {
        nombres=binding.inputNombre.text.toString().trim()
        apellidos=binding.inputApellido.text.toString().trim()
        correo=binding.inputMail.text.toString().trim()
        carrera=binding.spinnerCarrera.text.toString().trim()

        if(nombres.isEmpty()){
            binding.inputNombre.error="Ingresa un Nombre"
            binding.inputNombre.requestFocus()
        }else if(apellidos.isEmpty()){
            binding.inputApellido.error="Ingresa un apellido"
            binding.inputApellido.requestFocus()
        }else if(carrera.isEmpty() || carrera=="-- Seleccione una carrera --"){
            Toast.makeText(this, "Selecciona una carrera", Toast.LENGTH_SHORT).show()
        }else{
            // AHORA SÍ autenticamos con Google y guardamos en la BD
            registrarUsuarioConGoogle()
        }
    }

    //Cargar los datos que vienen de google
    private fun cargarDatosDeGoogle() {
        val nombre = intent.getStringExtra("nombre") ?: ""
        val correo = intent.getStringExtra("correo") ?: ""

        // 🔹 Separar el nombre y apellido
        val partes = nombre.trim().split("\\s+".toRegex())
        val name = if (partes.size >= 2) "${partes[0]} ${partes[1]}" else partes.getOrNull(0) ?: ""
        val apellido = when {
            partes.size >= 4 -> "${partes[2]} ${partes[3]}"
            partes.size == 3 -> partes[2]
            else -> ""
        }

        binding.inputNombre.setText(name)
        binding.inputApellido.setText(apellido)
        binding.inputMail.setText(correo)

        deshabilitarInput()
    }

    private fun validarCorreoExistente(){
        progressDialog.setMessage("Verificando correo...")
        progressDialog.show()


        val referencia = FirebaseDatabase.getInstance().getReference("users")

        referencia.orderByChild("correo").equalTo(correo)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    if (snapshot.exists()) {
                        progressDialog.dismiss()
                        binding.inputMail.error = "¡Este correo ya está en uso!"
                        binding.inputMail.requestFocus()
                    } else
                        crearCuentaFirebaseAuth()
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@RegistroActivity,
                        "Error al verificar correo: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun crearCuentaFirebaseAuth() {
        progressDialog.setMessage("Creando Usuario")

        firebaseAuth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener {
                crearUsuarioEnFireBase("Email")
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Error al crear cuenta, debido a: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun registrarUsuarioConGoogle() {
        if (idTokenGoogle == null) {
            Toast.makeText(this, "Error: No se encontró token de Google", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.setMessage("Registrando con Google...")
        progressDialog.show()

        // AQUÍ autenticamos por primera vez con Firebase
        val credencial = GoogleAuthProvider.getCredential(idTokenGoogle, null)

        firebaseAuth.signInWithCredential(credencial)
            .addOnSuccessListener {
                // Usuario autenticado, ahora guardamos en la BD
                crearUsuarioEnFireBase("Google")
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al autenticar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun crearUsuarioEnFireBase(proveedor: String){
        val uid=firebaseAuth.uid
        val correoActual=firebaseAuth.currentUser!!.email

        //Enviar datos a FB
        val enviarDatos=HashMap<String,Any>()
        enviarDatos["uid"]="${uid}"
        enviarDatos["nombre"]="${nombres}"
        enviarDatos["apellido"]="${apellidos}"
        enviarDatos["correo"]="${correoActual}"
        enviarDatos["carrera"]="${carrera}"
        enviarDatos["proveedor"]=proveedor

        //Guardar la informacion
        val referencia=FirebaseDatabase.getInstance().getReference("users")
        referencia.child(uid!!)
            .setValue(enviarDatos)
            .addOnCompleteListener {
                progressDialog.dismiss()
                Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finishAffinity()
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

    private fun deshabilitarInput() {
        binding.inputNombre.isEnabled=false
        binding.inputApellido.isEnabled=false
        binding.inputMail.isEnabled=false
        binding.layoutPassword.visibility= View.GONE
        binding.botonRegistrar.visibility= View.GONE
        binding.botonRegistrarConGoogle.visibility= View.VISIBLE
    }
}