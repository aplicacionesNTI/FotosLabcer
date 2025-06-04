package com.itevebasa.fotoslabcer.auxiliar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import androidx.core.net.toFile
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class Imagenes {
    companion object{
        //Devuelve el Bitmap de un URI
        fun getBitmapFromUri(context: Context, uri: Uri?): Bitmap? {
            return try {
                val inputStream = context.contentResolver.openInputStream(uri!!)
                BitmapFactory.decodeStream(inputStream)
            } catch (e: FileNotFoundException) {
                Log.e("DetallesActivity", "Error al obtener la imagen: ${e.message}")
                null
            }
        }

        // Función para aplicar la marca de agua
        fun mark(src: Bitmap, watermark: String): Bitmap {
            val w = src.width
            val h = src.height
            val result = createBitmap(w, h, src.config ?: Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)

            // Dibuja la imagen original sobre el lienzo
            canvas.drawBitmap(src, 0f, 0f, null)

            // Define el tamaño y la posición del texto de la marca de agua
            val paint = Paint()
            paint.color = Color.RED
            paint.textSize = 30f
            paint.isAntiAlias = true
            paint.typeface = Typeface.create("arial", Typeface.BOLD)

            val textWidth = paint.measureText(watermark)
            val textHeight = paint.fontMetrics.descent - paint.fontMetrics.ascent

            // Calcular la posición para colocar la marca de agua en la parte inferior derecha
            val x = 20f
            val y = 10f + textHeight

            // Dibuja la marca de agua en la posición calculada
            canvas.drawText(watermark, x, y, paint)
            return result
        }

        //Crea el archivo de imagen en la carpeta de pictures/MyAppImages de la aplicacion
        fun createImageFile(context: Context, carpeta: String): File {
            // Verificar si el directorio de almacenamiento existe, si no, crearlo
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            val dir = File(storageDir, carpeta)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            // Crear el archivo temporal de imagen en el directorio correcto
            return File.createTempFile(
                "JPEG_${System.currentTimeMillis()}_", ".jpg", dir
            )
        }

        //Borra todos los archivos de un directorio
        fun borrarContenidoCarpeta(carpeta: File) {
            if (carpeta.exists() && carpeta.isDirectory) {
                val archivos = carpeta.listFiles()
                archivos?.forEach { archivo ->
                    archivo.delete()
                }
            }
        }

        //Devuelve la dirección y la fecha desde los metadatos de una imagen
        fun getImageMetadata(context: Context, uri: Uri): String {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val exif = ExifInterface(inputStream!!)
                val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: "Desconocido"
                val latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) ?: "No disponible"
                val latitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) ?: "No disponible"
                val longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) ?: "No disponible"
                val longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF) ?: "No disponible"
                return "📅 Fecha: $dateTime\n📍 Ubicación: $latitude $latitudeRef / $longitude $longitudeRef"
            } catch (e: Exception) {
                return "No se pudieron obtener los metadatos."
            }
        }

        //Devuelve el Uri desde el tag de un ImageView
        fun getImageUriFromImageView(imageView: ImageView): Uri? {
            return imageView.tag as? Uri
        }

        //Devuelve el base64 bde una imagen desde el ImageView
        fun encodeImageViewUriToBase64(imageView: ImageView, context: Context): String? {
            val uri = imageView.tag as? Uri ?: return null
            return try {
                // Abrir el stream desde la URI
                val inputStream = context.contentResolver.openInputStream(uri) ?: return null
                val byteArray = inputStream.readBytes()  // Leer los bytes de la imagen
                inputStream.close()
                Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        //Convierte un ImageView a tipo File
        fun convertirImagenViewAFile(imageView: ImageView): File? {
            val uri = imageView.tag as? Uri ?: return null
            return try {
                uri.toFile()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        //Guarda los metadatos de la imagen, la guarda en el dispositivo y devuelve el uri de la imagen modificada
        fun guardarImagenConMetadatos(context: Context, bitmap: Bitmap, location: Location?, carpeta: String): Uri? {
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val dir = File(storageDir, carpeta)
            val imageFile = File(dir, fileName)

            try {
                var quality = 30
                val outStream = ByteArrayOutputStream()
                //val outStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
                while (outStream.toByteArray().size > 85 * 1024 && quality > 5) {
                    quality -= 5
                    outStream.reset()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
                }
                val finalBytes = outStream.toByteArray()
                val fileOut = FileOutputStream(imageFile)
                fileOut.write(finalBytes)
                fileOut.flush()
                fileOut.close()

                val exif = ExifInterface(imageFile.absolutePath)
                val currentDateTime = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).format(Date())
                exif.setAttribute(ExifInterface.TAG_DATETIME, currentDateTime)
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertToDegreeMinuteSecondFormat(latitude))
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertToDegreeMinuteSecondFormat(longitude))
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, if (latitude >= 0) "N" else "S")
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, if (longitude >= 0) "E" else "W")
                }
                exif.saveAttributes()

                return Uri.fromFile(imageFile)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        // Función auxiliar para convertir las coordenadas GPS en formato de grados y minutos
        private fun convertToDegreeMinuteSecondFormat(coordinate: Double): String {
            val absoluteCoordinate = abs(coordinate)
            val degrees = absoluteCoordinate.toInt()
            val minutes = ((absoluteCoordinate - degrees) * 60).toInt()
            val seconds = ((absoluteCoordinate - degrees - minutes / 60.0) * 3600).toInt()
            return "$degrees/1,$minutes/1,$seconds/1"
        }


    }
}