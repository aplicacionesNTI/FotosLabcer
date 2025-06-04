package com.itevebasa.fotoslabcer.modelos

import java.io.File

data class FotoRequest(
    val codigoDocumento: String,
    val nombre: String,
    val item: String,
    val user_id: Int,
    val expediente_id: Int,
    val tipoDocumento_id: Int = 9,
    val file: File

)
