package com.itevebasa.fotoslabcer.conexion

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.itevebasa.fotoslabcer.daos.InspeccionDao
import com.itevebasa.fotoslabcer.modelos.Inspeccion

@Database(entities = [Inspeccion::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inspeccionDao(): InspeccionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "InspeccionesDB"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}