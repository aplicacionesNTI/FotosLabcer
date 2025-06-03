package com.itevebasa.fotoslabcer.modelos

data class PaginarResponse(
    val current_page: Int = 0,
    val data: List<Expediente> = emptyList()
)

data class Expediente(
    val id: Int = 0,
    val nombre: String = "",
    val item: String = "",
    val codigoExpediente: String = "",
    val observaciones: String? = null,
    val anulado: Int = 0,
    val motivoAnulado: String? = null,
    val user_id: Int = 0,
    val documentosCompletos: Int = 0,
    val area_id: Int = 0,
    val user: User = User(),
    val documentos: List<Documento> = emptyList(),
    val area: Area = Area()
)

data class Documento(
    val id: Int = 0,
    val nombre: String = "",
    val fileName: String = "",
    val fileSize: String = "",
    val extension: String = "",
    val user_id: Int = 0,
    val codigo_cve: String = "",
    val item: String = "",
    val codigoDocumento: String = "",
    val expediente_id: Int = 0,
    val tipoDocumento_id: Int = 0,
    val tipo_documento: TipoDocumento = TipoDocumento()
)

data class TipoDocumento(
    val id: Int = 0,
    val nombre: String = "",
    val codigo: String = "",
    val area_id: Int = 0
)


