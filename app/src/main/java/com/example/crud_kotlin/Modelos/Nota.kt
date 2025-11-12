package com.example.crud_kotlin.Modelos

data class Nota @JvmOverloads constructor(
    var titulo: String = "",
    var contenido: String = "",
    var fechaCreacion: String = "",
    var usuario: String = "", // ID del usuario
    var id: String = ""
)