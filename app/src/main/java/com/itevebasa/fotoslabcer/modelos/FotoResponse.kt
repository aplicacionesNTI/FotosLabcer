package com.itevebasa.fotoslabcer.modelos
data class FotoResponse(
    val message: String,
    val data: DocumentoData
)

data class DocumentoData(
    val nombre: String,
    val item: String,
    val codigoDocumento: String,
    val user_id: Int,
    val expediente_id: Int,
    val tipoDocumento_id: Int,
    val id: Int
)