package com.example.crud_kotlin.Modelos

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class Recordatorio(
        var titulo: String = "",
        var hora: String = "",
        var fecha: String = "",
        var usuario: String = "",
        var lugar: String = "",
        var id: String = ""
) : Parcelable