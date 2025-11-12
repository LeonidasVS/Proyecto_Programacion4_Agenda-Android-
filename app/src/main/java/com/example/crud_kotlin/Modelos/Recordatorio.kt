package com.example.crud_kotlin.Modelos

data class Recordatorio @JvmOverloads constructor(
        var titulo: String = "",
        var hora: String = "",
        var fecha: String = "",
        var usuario: String = "",
        var lugar: String = "",
        var id: String = ""
)
