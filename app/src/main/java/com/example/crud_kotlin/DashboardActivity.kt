package com.example.crud_kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crud_kotlin.Fragmentos.FragmentCalendario
import com.example.crud_kotlin.Fragmentos.FragmentNotas
import com.example.crud_kotlin.Fragmentos.FragmentPerfil
import com.example.crud_kotlin.Objetos.Avatar
import com.example.crud_kotlin.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding:ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.bottomNV.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.item_nota -> {
                    verFragmentonote()
                    true
                }
                R.id.item_calendario -> {
                    verFragmentoCalendario()
                    true
                }
                R.id.item_perfil -> {
                    verFragmentoPerfil()
                    true
                }
                else -> false
            }
        }
//
//        binding.btnConfiguracion.setOnClickListener {
//            startActivity(Intent(applicationContext, PerfilActivity::class.java))
//        }
//
//        binding.btnPerfil.setOnClickListener {
//            startActivity(Intent(applicationContext, PerfilActivity::class.java))
//        }

        circulName()

        //  Fragmento por defecto al abrir el Dashboard
        verFragmentonote()
        binding.bottomNV.selectedItemId = R.id.item_nota
    }


    private fun circulName() {

        val user = FirebaseAuth.getInstance().currentUser

        if (user?.photoUrl != null) {
            user.photoUrl?.let { uri ->
                // Guardar la imagen en el objeto Avatar
                Avatar.imagenUri = uri

                // Mostrar la imagen en el ImageView
                Picasso.get()
                    .load(uri)
                    .into(binding.btnConfiguracion)

                // Ocultar el botón de texto (avatar por letra)
                binding.btnPerfil.visibility = View.GONE
            }
        } else {
            // Obtener la primera letra del email
            val displayName = user?.email
            Avatar.letra = displayName?.firstOrNull()?.toString()?.uppercase() ?: "?"

            // Mostrar la letra en el TextView
            binding.btnPerfil.text = Avatar.letra

            // Generar color aleatorio solo si no existe
            if (Avatar.color == null) {
                val random = java.util.Random()
                Avatar.color = android.graphics.Color.rgb(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                )
            }

            // Crear un drawable circular con el color del avatar
            val drawable = android.graphics.drawable.GradientDrawable()
            drawable.shape = android.graphics.drawable.GradientDrawable.OVAL
            drawable.setColor(Avatar.color!!)

            // Asignar el drawable como fondo del TextView
            binding.btnPerfil.background = drawable

            // Ocultar el ImageView del perfil
            binding.btnConfiguracion.visibility = View.GONE
        }

    }






    private fun verFragmentonote(){
        binding.tvTitulo.text = "Notas de Usuario"

        val fragment_note = FragmentNotas()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment_note, "Notas de Usuario")
        fragmentTransaction.commit()
    }

    private fun verFragmentoCalendario(){
        binding.tvTitulo.text = "Calendario"

        val fragment_calendario = FragmentCalendario()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment_calendario, "Frgment PErfil")
        fragmentTransaction.commit()
    }


    private fun verFragmentoPerfil(){
        binding.tvTitulo.text = "Perfil Usuario"

        val fragment_perfil = FragmentPerfil()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment_perfil, "Fragment Perfil")
        fragmentTransaction.commit()
    }
}