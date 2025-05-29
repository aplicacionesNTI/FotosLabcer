package com.itevebasa.fotoslabcer.auxiliar

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permisos {
    companion object {
        // Función para verificar si los permisos de la cámara están concedidos
        fun isCameraPermissionGranted(context: Context): Boolean {
            val cameraPermission =
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            return cameraPermission == PackageManager.PERMISSION_GRANTED
        }

        // Función para verificar si los permisos de almacenamiento están concedidos (Android 11+)
        @RequiresApi(Build.VERSION_CODES.R)
        fun isStoragePermissionGranted(context: Context): Boolean {
            // Verificar si el permiso de almacenamiento está concedido
            val readExternalStoragePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writeExternalStoragePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            return readExternalStoragePermission == PackageManager.PERMISSION_GRANTED ||
                    writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED ||
                    Environment.isExternalStorageManager()  // Verifica si la app tiene acceso al almacenamiento en Android 11+
        }

        // Función para verificar si los permisos de ubicación están concedidos
        fun isLocationPermissionGranted(context: Context): Boolean {
            val fineLocationPermission =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            val coarseLocationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED
        }

        // Función para solicitar permisos en tiempo de ejecución
        fun requestPermissions(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }

        // Código de solicitud de permisos
        const val PERMISSION_REQUEST_CODE = 2001
    }
}