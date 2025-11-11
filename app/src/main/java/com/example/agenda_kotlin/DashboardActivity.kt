package com.example.agenda_kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.agenda_kotlin.Fragmentos.FragmentCalendario
import com.example.agenda_kotlin.Fragmentos.FragmentContacto
import com.example.agenda_kotlin.Fragmentos.FragmentRecordatorio
import com.example.agenda_kotlin.Objeto.Avatar
import com.example.agenda_kotlin.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityDashboardBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.bottomNV.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.item_nota->{
                    verFragmetoNotas()
                    true
                }

                R.id.item_recordatorios->{
                    verFragmetoRecordatorios()
                    true
                }

                R.id.item_contactos->{
                    verFragmetoContacto()
                    true
                }

                R.id.item_calendario->{
                    verFragmetoCalendario()
                    true
                }

                else->{
                    false
                }
            }
        }

        avatarUsuario()

        binding.btnPerfil.setOnClickListener{
            val intent=Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        binding.btnGoogleImg.setOnClickListener{
            val intent=Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }
    }

    private fun avatarUsuario() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user?.photoUrl != null) {
            user.photoUrl?.let { uri ->

                // Guardar la imagen en el objeto Avatar
                Avatar.imagenUri = uri

                Picasso.get()
                    .load(uri)
                    .into(binding.btnGoogleImg)

                binding.btnPerfil.visibility = View.GONE
            }
        } else {
            // Obtener la primera letra del email
            val displayName = user?.email
            Avatar.letra = displayName?.firstOrNull()?.toString()?.uppercase() ?: "?"

            binding.btnPerfil.text = Avatar.letra

            if (Avatar.colorAvatar == null) {
                val random = java.util.Random()
                Avatar.colorAvatar = android.graphics.Color.rgb(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                )
            }

            // Crear un drawable circular con el color del avatar
            val drawable = android.graphics.drawable.GradientDrawable()
            drawable.shape = android.graphics.drawable.GradientDrawable.OVAL
            drawable.setColor(Avatar.colorAvatar!!)

            // Asignar el drawable como fondo del TextView
            binding.btnPerfil.background = drawable

            // Ocultar el ImageView del perfil
            binding.btnGoogleImg.visibility = View.GONE
        }
    }


    private fun verFragmetoCalendario() {
        binding.tvTitulo.text = "Calendario"

        val fragment_calendario = FragmentCalendario()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment_calendario, "Frgment PErfil")
        fragmentTransaction.commit()
    }

    private fun verFragmetoContacto() {
        binding.tvTitulo.text = "Contacto"

        val fragment_contacto = FragmentContacto()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment_contacto, "Frgment PErfil")
        fragmentTransaction.commit()
    }

    private fun verFragmetoRecordatorios() {
        binding.tvTitulo.text = "Recordatorios"

        val fragment_recordatorio = FragmentRecordatorio()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentoFL.id, fragment_recordatorio, "Recordatorios")
        fragmentTransaction.commit()
    }

    private fun verFragmetoNotas() {
        binding.tvTitulo.text = "PlanTimer"

        //Volver a la dashboard principal
        val intent = Intent(this, DashboardActivity::class.java).apply {
            // Si ya existe una instancia en la pila, la trae y limpia la de encima
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        true
    }

}