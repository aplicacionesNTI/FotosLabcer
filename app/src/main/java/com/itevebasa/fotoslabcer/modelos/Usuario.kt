package com.itevebasa.fotoslabcer.modelos
data class Usuario(
    val user: User = User(),
    val token: String = "",
    val estado: Boolean = false
)

data class User(
    val id: Int = 0,
    val nombre: String = "",
    val cif: String = "",
    val login: String = "",
    val token: String = "",
    val rol_id: Int = 0,
    val codigoTrabajadorLabcer: Int = 0,
    val certificado: String = "",
    val activo: Int = 0,
    val updatePass: Int = 0,
    val rol: Rol = Rol(),
    val areas: List<Area> = emptyList()
)

data class Rol(
    val id: Int = 0,
    val nombre: String = ""
)

data class Area(
    val id: Int = 0,
    val nombre: String = "",
    val pivot: Pivot = Pivot()
)

data class Pivot(
    val user_id: Int = 0,
    val area_id: Int = 0
)
