package com.itevebasa.fotoslabcer.daos

import androidx.room.*
import com.itevebasa.fotoslabcer.modelos.Inspeccion

@Dao
interface InspeccionDao {
    @Query("SELECT expediente FROM Inspecciones WHERE guid = :guid")
    fun obtenerExpedientePorGuid(guid: String): String?

    @Query("SELECT guid FROM Inspecciones WHERE expediente = :expediente")
    fun obtenerGuidPorExpediente(expediente: String): String?

    @Query("UPDATE Inspecciones SET expediente = :nuevoExpediente, acta = :nuevaActa WHERE guid = :guid")
    fun actualizarExpedientePorGuid(guid: String, nuevoExpediente: String, nuevaActa: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarInspeccion(inspeccion: Inspeccion): Long

    @Query("SELECT * FROM Inspecciones WHERE expediente IS NOT NULL AND expediente != ''")
    fun obtenerInspeccionesConExpediente(): List<Inspeccion>

    @Query("SELECT * FROM Inspecciones WHERE expediente IS NULL")
    fun obtenerInspeccionesSinExpediente(): List<Inspeccion>
}