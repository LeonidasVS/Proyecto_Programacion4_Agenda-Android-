package com.example.crud_kotlin

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
import com.example.crud_kotlin.Fragmentos.FragmentCalendario
import com.example.crud_kotlin.Fragmentos.FragmentContacto
import com.example.crud_kotlin.Fragmentos.FragmentNotas
import com.example.crud_kotlin.Fragmentos.FragmentRecordatorio
import com.example.crud_kotlin.Modelos.Registro
import com.example.crud_kotlin.Objetos.Avatar
import com.example.crud_kotlin.databinding.ActivityDashboardBinding
import com.example.crud_kotlin.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlin.random.Random

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
                R.id.item_recordatorios -> {
                    verFragmentoRecordatorio()
                    true
                }
                R.id.item_contactos -> {
                    verFragmentoContacto()
                    true
                }
                R.id.item_calendario -> {
                    verFragmentoCalendario()
                    true
                }
                else -> false
            }
        }

        binding.btnConfiguracion.setOnClickListener {
            startActivity(Intent(applicationContext, PerfilActivity::class.java))
        }

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(applicationContext, PerfilActivity::class.java))
        }

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


    private fun verFragmentoNotas(){
        binding.tvTitulo.text = "Notas"

        //Volver a la dashboard principal
        val intent = Intent(this, DashboardActivity::class.java).apply {
            // Si ya existe una instancia en la pila, la trae y limpia la de encima
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        true
    }

    private fun verFragmentoRecordatorio(){
        binding.tvTitulo.text = "Recordatorios"

        val fragment_recordatorio = FragmentRecordatorio()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment_recordatorio, "Recordatorios")
        fragmentTransaction.commit()
    }

    private fun verFragmentonote(){
        binding.tvTitulo.text = "Notas de Usuario"

        val fragment_note = FragmentNotas()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment_note, "Notas de Usuario")
        fragmentTransaction.commit()
    }

    private fun verFragmentoContacto(){
        binding.tvTitulo.text = "Contacto"

        val fragment_contacto = FragmentContacto()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment_contacto, "Frgment PErfil")
        fragmentTransaction.commit()
    }

    private fun verFragmentoCalendario(){
        binding.tvTitulo.text = "Calendario"

        val fragment_calendario = FragmentCalendario()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment_calendario, "Frgment PErfil")
        fragmentTransaction.commit()
    }
}