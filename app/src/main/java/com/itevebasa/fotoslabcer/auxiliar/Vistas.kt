package com.itevebasa.fotoslabcer.auxiliar

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.itevebasa.fotoslabcer.R
import java.io.File

class Vistas {
    companion object{

        //Versión para fotos obligatorias
        //Crea una preview de la fotografía, que permite borrarla
        @SuppressLint("SetTextI18n")
        fun showImagePreview(context: Context, imgView: ImageView) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_image_preview)

            val previewImageView = dialog.findViewById<ImageView>(R.id.previewImageView)
            val metadataTextView = dialog.findViewById<TextView>(R.id.metadataTextView)
            val deleteButton = dialog.findViewById<Button>(R.id.deleteButton)
            val volverButton = dialog.findViewById<Button>(R.id.volverButton)

            // Obtener la URI de la imagen desde el almacenamiento (ya guardada)
            val uri = Imagenes.getImageUriFromImageView(imgView)
            uri?.let {
                previewImageView.setImageURI(uri)
                val metadata = Imagenes.getImageMetadata(context, it)
                metadataTextView.text = metadata
            } ?: run {
                metadataTextView.text = "No se encontraron metadatos."
            }
            deleteButton.setOnClickListener {
                imgView.setImageDrawable(null)
                dialog.dismiss()
            }
            volverButton.setOnClickListener{
                dialog.dismiss()
            }
            dialog.window?.apply {
                val width = (context.resources.displayMetrics.widthPixels * 0.8).toInt() // 80% del ancho de la pantalla
                setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT) // Ajustamos el tamaño del dialogo
            }
            dialog.show()
        }

        //Versión para fotos extras
        //Crea una preview de la fotografía, que permite borrarla
        @SuppressLint("SetTextI18n")
        fun showImagePreview(context: Context, container: LinearLayout, cardView: CardView, imgView: ImageView, extraImageViews: MutableList<ImageView>) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_image_preview)

            val previewImageView = dialog.findViewById<ImageView>(R.id.previewImageView)
            val metadataTextView = dialog.findViewById<TextView>(R.id.metadataTextView)
            val deleteButton = dialog.findViewById<Button>(R.id.deleteButton)
            val volverButton = dialog.findViewById<Button>(R.id.volverButton)

            // Obtener la URI de la imagen desde el almacenamiento (ya guardada)
            val uri = Imagenes.getImageUriFromImageView(imgView)
            uri?.let {
                previewImageView.setImageURI(uri)
                val metadata = Imagenes.getImageMetadata(context, it)
                metadataTextView.text = metadata
            } ?: run {
                metadataTextView.text = "No se encontraron metadatos."
            }
            deleteButton.setOnClickListener {
                if (uri != null) {
                    val file = File(uri.path!!)
                    if (file.exists()) {
                        file.delete()
                    }
                }

                extraImageViews.remove(imgView)
                imgView.setImageDrawable(null)
                container.removeView(cardView)
                dialog.dismiss()
            }
            volverButton.setOnClickListener{
                dialog.dismiss()
            }
            dialog.window?.apply {
                val width = (context.resources.displayMetrics.widthPixels * 0.8).toInt() // 80% del ancho de la pantalla
                setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT) // Ajustamos el tamaño del diálogo
            }
            dialog.show()
        }

        // Función para crear y mostrar el ProgressDialog con un ProgressBar al enviar las imágenes
        fun showProgressDialog(context: Context): AlertDialog {
            val progressBar = ProgressBar(context)
            progressBar.isIndeterminate = true  // Esto hace que el progreso sea indeterminado
            val dialog = AlertDialog.Builder(context)
                .setMessage("Enviando imágenes...")
                .setView(progressBar)
                .setCancelable(false) // No permitir que se cierre manualmente
                .create()

            dialog.show()
            return dialog
        }

        // Función para crear y mostrar el ProgressDialog con un ProgressBar al obtener la deirección
        fun showWaitDialog(context: Context): AlertDialog {
            val progressBar = ProgressBar(context)
            progressBar.isIndeterminate = true  // Esto hace que el progreso sea indeterminado
            val dialog = AlertDialog.Builder(context)
                .setMessage("Obteniendo dirección...")
                .setView(progressBar)
                .setCancelable(false) // No permitir que se cierre manualmente
                .create()

            dialog.show()
            return dialog
        }

        //Convierte medidas en dp a pixeles
        fun dpToPx(context: Context, dp: Int): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }
    }
}