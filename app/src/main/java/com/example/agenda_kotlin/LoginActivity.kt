package com.example.agenda_kotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.agenda_kotlin.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    lateinit var referenciBD:DatabaseReference
    lateinit var progressDialog: ProgressDialog

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
}