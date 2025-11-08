package com.example.agenda_kotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.agenda_kotlin.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    lateinit var referenciBD:DatabaseReference
    lateinit var progressDialog: ProgressDialog

    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth=FirebaseAuth.getInstance()
        binding=ActivityLoginBinding.inflate(layoutInflater)
        referenciBD=FirebaseDatabase.getInstance().getReference("users")

        //Instancia del progressDialog
        progressDialog=ProgressDialog(this)
        progressDialog.setTitle("Espere por favor...")
        progressDialog.setCanceledOnTouchOutside(false)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnLogin.setOnClickListener {
            iniciarSesionConEmail()
        }

        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.id_client_google))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Evento: iniciar sesión con Google
        binding.btnLoginGoogle.setOnClickListener {
            iniciarSesionConGoogle()
        }

    }

    private var correoUsuario="";
    private var passwordUsuario=""

    private fun iniciarSesionConEmail() {

        correoUsuario=binding.inputCorreo.text.toString().trim()
        passwordUsuario=binding.inputPassword.text.toString().trim()

        if(correoUsuario.isEmpty()){
            binding.inputCorreo.error="Ingresa un correo"
            binding.inputCorreo.requestFocus()
        }else if(passwordUsuario.isEmpty() || passwordUsuario.length<6){
            binding.inputPassword.error="Contraseña mayor a 6 digitos"
            binding.inputPassword.requestFocus()
        }else{

            progressDialog.setMessage("Buscando Usuario")
            progressDialog.show()

            auth.signInWithEmailAndPassword(correoUsuario,passwordUsuario)
                .addOnCompleteListener{ task ->
                    if(task.isSuccessful){
                        val uid=auth.currentUser?.uid ?: return@addOnCompleteListener

                        //Obtener la informacion del usuario
                        referenciBD.child(uid).get()
                            .addOnSuccessListener { usuario->
                                if(usuario.exists()){
                                    progressDialog.dismiss()
                                    val intent=Intent(this, DashboardActivity::class.java)
                                    startActivity(intent)
                                    finishAffinity()
                                }else{
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e->
                                progressDialog.dismiss()
                                Toast.makeText(this, "Error al obtener los datos y ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }else{
                        progressDialog.dismiss()
                        Toast.makeText(this, "Credenciales Incorrectas", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    //Inicio de sesion de google
    private fun iniciarSesionConGoogle(){
        // Cerrar sesión de Google para forzar la selección de cuenta
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            val googleSignIntent = mGoogleSignInClient.signInIntent
            googleSignInActivityResultLauncher.launch(googleSignIntent)
        }
    }

    private val googleSignInActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val data = resultado.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta = task.getResult(ApiException::class.java)
                verificarYAutenticarGoogle(cuenta.idToken, cuenta.email, cuenta.displayName)
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verificarYAutenticarGoogle(idToken: String?, email: String?, displayName: String?) {
        progressDialog.setMessage("Verificando usuario...")
        progressDialog.show()

        // Verificar si el correo ya está registrado en Firebase Auth
        email?.let { correo ->
            auth.fetchSignInMethodsForEmail(correo)
                .addOnSuccessListener { resultado ->
                    if (resultado.signInMethods?.isNotEmpty() == true) {
                        // El usuario YA existe en Auth, autenticar y verificar BD
                        autenticarYVerificarBD(idToken)
                    } else {
                        // El usuario NO existe en Auth, enviar a Registro SIN autenticar
                        progressDialog.dismiss()

                        val intent = Intent(this, RegistroActivity::class.java)
                        intent.putExtra("idToken", idToken)
                        intent.putExtra("nombre", displayName ?: "")
                        intent.putExtra("correo", correo)
                        intent.putExtra("datosDeGoogle", true)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al verificar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            progressDialog.dismiss()
            Toast.makeText(this, "No se pudo obtener el correo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun autenticarYVerificarBD(idToken: String?) {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        progressDialog.setMessage("Autenticando...")

        auth.signInWithCredential(credencial)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                // Verificar si el usuario existe en la base de datos
                referenciBD.child(uid).get()
                    .addOnSuccessListener { snapshot ->
                        progressDialog.dismiss()

                        if (snapshot.exists()) {
                            // Usuario tiene datos en BD, ir al Dashboard
                            val intent = Intent(this, DashboardActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        } else {
                            // Caso raro: existe en Auth pero no en BD
                            Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Error al verificar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al autenticar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}