package com.itevebasa.fotoslabcer.actividades

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.itevebasa.fotoslabcer.R
import com.itevebasa.fotoslabcer.auxiliar.Imagenes
import com.itevebasa.fotoslabcer.auxiliar.Localizacion
import com.itevebasa.fotoslabcer.auxiliar.Permisos
import com.itevebasa.fotoslabcer.auxiliar.Vistas
import com.itevebasa.fotoslabcer.modelos.DetallesViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FotosActivity: AppCompatActivity()  {

    private var expediente: String = ""
    private var tempFile: File? = null
    private val imageViews = mutableListOf<ImageView>()
    private var location: Location? = null
    private var selectedImageView: ImageView? = null
    private var photoUri: Uri? = null
    private lateinit var viewModel: DetallesViewModel

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fotos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        expediente = intent.getStringExtra("expediente")!!
        val addButton = findViewById<Button>(R.id.addButton)
        val enviarButton = findViewById<Button>(R.id.enviarButton)
        val contenedor: LinearLayout = findViewById(R.id.contenedor)
        viewModel = ViewModelProvider(this)[DetallesViewModel::class.java]
        val progressDialog = Vistas.showWaitDialog(this)
        lifecycleScope.launch {
            location = Localizacion.getCurrentLocation(this@FotosActivity)
            progressDialog.dismiss()
        }
        mostrarImagenesExistentes(this, contenedor, expediente)
        addButton.setOnClickListener {
            addImageCard(this, contenedor)
        }
        enviarButton.setOnClickListener {
            mostrarDialogoActa { actaTexto ->
               Toast.makeText(this, "Enviado", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Definir el ActivityResultLauncher para capturar la imagen
    @SuppressLint("MissingPermission") //Se comprueban los permisos anteriormente
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val takePictureLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val photo: Bitmap? = Imagenes.getBitmapFromUri(this@FotosActivity, photoUri)
                // Verificar si se obtuvo la ubicación
                if (location == null) {
                    Toast.makeText(
                        this@FotosActivity,
                        "No se pudo obtener la ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("DetallesActivity", "Ubicación no disponible")
                } else {
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                    if (photo != null) {
                        val reducedPhoto: Bitmap = if (photo.width>photo.height){
                            photo.scale(1024, 720)
                        }else{
                            photo.scale(720, 960)
                        }

                        // Aplicar marca de agua
                        val imageWithWatermark = Imagenes.mark(
                            reducedPhoto,
                            LocalDateTime.now().format(formatter).toString()
                        )
                        // Guardar la imagen con metadatos EXIF (solo si hay ubicación)
                        photoUri = Imagenes.guardarImagenConMetadatos(
                            this@FotosActivity,
                            imageWithWatermark,
                            location,
                            expediente
                        )
                        selectedImageView?.setImageURI(photoUri)
                        selectedImageView?.tag = photoUri
                        // Borrar el archivo JPEG_ original
                        println(tempFile)
                        if (tempFile?.exists() == true) {
                            tempFile?.delete()
                        }

                    } else {
                        Log.e("DetallesActivity", "No se pudo decodificar la imagen desde la URI.")
                    }
                }
            }else {
                Toast.makeText(this, "Foto no tomada", Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun openImagePreview(cardView: CardView, container: LinearLayout, imgView: ImageView) {
        selectedImageView = imgView
        if (imgView.drawable == null) {
            abrirCamaraConPermisos()
        } else {
            Vistas.showImagePreview(this, container, cardView, imgView, imageViews)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun abrirCamaraConPermisos(){
        // Verificar si tenemos permiso para acceder a la cámara
        if (Permisos.isCameraPermissionGranted(this) && Permisos.isStoragePermissionGranted(this) && Permisos.isLocationPermissionGranted(this)) {
            openCamera()
        } else {
            Permisos.requestPermissions(this)
        }
    }

    // Función para abrir la cámara
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun openCamera() {
        val photoFile = Imagenes.createImageFile(this, expediente)
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        tempFile = photoFile
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri) // Guardar la foto en el archivo
        }
        takePictureLauncher.launch(intent)
    }


    // Función para enviar las fotos al servidor
    private fun uploadPhotos(context: Context, photos: Int) {
        val progressDialog = Vistas.showProgressDialog(context)

    }

    // Metodo que se llama cuando se recibe la respuesta del usuario sobre los permisos
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Permisos.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permiso denegado para usar la cámara o escribir en almacenamiento", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun addImageCard(context: Context, container: LinearLayout) {
        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(context, 8), 0, dpToPx(context, 8))
            }
            gravity = Gravity.CENTER
            weightSum = 3f
        }

        repeat(3) {
            val cardView = CardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(dpToPx(context, 4), 0, dpToPx(context, 4), 0)
                }
                radius = dpToPxF(context, 16)
                cardElevation = dpToPxF(context, 6)
                preventCornerOverlap = true
                useCompatPadding = true
            }

            val imageView = ImageView(context).apply {
                id = View.generateViewId()
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dpToPx(context, 100)
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener {
                    openImagePreview(cardView, container, this)
                }
            }

            imageViews.add(imageView)
            cardView.addView(imageView)
            rowLayout.addView(cardView)
        }

        container.addView(rowLayout)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun mostrarImagenesExistentes(context: Context, container: LinearLayout, expediente: String) {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), expediente)
        if (!dir.exists() || !dir.isDirectory) return

        val imageFiles = dir.listFiles { file ->
            file.name.startsWith("photo_") && file.name.endsWith(".jpg")
        }?.sortedBy { it.lastModified() } ?: return

        val imagesPerRow = 3
        var currentRow: LinearLayout? = null
        var itemsInRow = 0

        imageFiles.forEachIndexed { index, file ->
            if (index % imagesPerRow == 0) {
                // Crea nueva fila
                currentRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, dpToPx(context, 8), 0, dpToPx(context, 8))
                    }
                    gravity = Gravity.CENTER
                    weightSum = 3f
                }
                container.addView(currentRow)
                itemsInRow = 0
            }

            val cardView = crearCardConImagen(context, container, Uri.fromFile(file))
            currentRow?.addView(cardView)
            itemsInRow++
        }

        // Si la última fila tiene menos de 3 imágenes, completar con tarjetas vacías funcionales
        if (itemsInRow in 1..2) {
            repeat(3 - itemsInRow) {
                val emptyCard = crearCardVaciaPulsable(context, container)
                currentRow?.addView(emptyCard)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun crearCardConImagen(context: Context, container: LinearLayout, uri: Uri): CardView {
        val cardView = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dpToPx(context, 4), 0, dpToPx(context, 4), 0)
            }
            radius = dpToPxF(context, 16)
            cardElevation = dpToPxF(context, 6)
            preventCornerOverlap = true
            useCompatPadding = true
        }

        val imageView = ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(context, 100)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageURI(uri)
            tag = uri
            setOnClickListener {
                openImagePreview(cardView, container, this)
            }
        }

        imageViews.add(imageView)
        cardView.addView(imageView)
        return cardView
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun crearCardVaciaPulsable(context: Context, container: LinearLayout): CardView {
        val cardView = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dpToPx(context, 4), 0, dpToPx(context, 4), 0)
            }
            radius = dpToPxF(context, 16)
            cardElevation = dpToPxF(context, 6)
            preventCornerOverlap = true
            useCompatPadding = true
        }

        val imageView = ImageView(context).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(context, 100)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setOnClickListener {
                selectedImageView = this
                openCamera()
            }
        }

        imageViews.add(imageView)
        cardView.addView(imageView)
        return cardView
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private fun dpToPxF(context: Context, dp: Int): Float {
        return dp * context.resources.displayMetrics.density
    }

    private fun mostrarDialogoActa(onConfirmar: (String) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_acta, null)

        val fotoCountText = dialogView.findViewById<TextView>(R.id.fotoCountText)
        val actaInput = dialogView.findViewById<EditText>(R.id.actaInput)

        fotoCountText.text = "Tienes ${contarFotosReales()} fotos actualmente"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Enviar Fotos")
            .setPositiveButton("Confirmar") { _, _ ->
                val acta = actaInput.text.toString()
                onConfirmar(acta)
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    fun contarFotosReales(): Int {
        return imageViews.count { imageView ->
            imageView.drawable != null
        }
    }
}
