package com.itevebasa.fotoslabcer.modelos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Inspecciones")
data class Inspeccion(
    @PrimaryKey val guid: String,
    val nombre: String?,
    val expediente: String?,
    val acta: String?
)
