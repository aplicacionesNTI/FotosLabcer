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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
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
import com.itevebasa.fotoslabcer.auxiliar.VariablesGlobales
import com.itevebasa.fotoslabcer.auxiliar.Vistas
import com.itevebasa.fotoslabcer.conexion.AppDatabase
import com.itevebasa.fotoslabcer.conexion.RetrofitClient
import com.itevebasa.fotoslabcer.daos.InspeccionDao
import com.itevebasa.fotoslabcer.modelos.DetallesViewModel
import com.itevebasa.fotoslabcer.modelos.Expediente
import com.itevebasa.fotoslabcer.modelos.Inspeccion
import com.itevebasa.fotoslabcer.modelos.PaginarRequest
import com.itevebasa.fotoslabcer.modelos.PaginarResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList
import java.util.UUID

class FotosActivity: AppCompatActivity()  {

    private var guid: String? = null
    private var nombre: String? = null
    private var tempFile: File? = null
    private val imageViews = mutableListOf<ImageView>()
    private var location: Location? = null
    private var selectedImageView: ImageView? = null
    private var photoUri: Uri? = null
    private lateinit var viewModel: DetallesViewModel
    private lateinit var db: AppDatabase
    private lateinit var dao: InspeccionDao

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
        db = AppDatabase.getDatabase(this)
        dao = db.inspeccionDao()
        val addButton = findViewById<Button>(R.id.addButton)
        val enviarButton = findViewById<Button>(R.id.enviarButton)
        val contenedor: LinearLayout = findViewById(R.id.contenedor)
        viewModel = ViewModelProvider(this)[DetallesViewModel::class.java]
        val progressDialog = Vistas.showWaitDialog(this)
        lifecycleScope.launch {
            location = Localizacion.getCurrentLocation(this@FotosActivity)
            progressDialog.dismiss()
        }
        guid = intent.getStringExtra("guid")
        nombre = intent.getStringExtra("nombre")
        if (guid == null){
            guid = UUID.randomUUID().toString()
            lifecycleScope.launch(Dispatchers.IO) {
                dao.insertarInspeccion(Inspeccion(guid = guid!!, nombre, null, null))
            }
        }else{
            lifecycleScope.launch(Dispatchers.IO) {
                mostrarImagenesExistentes(this@FotosActivity, contenedor, guid!!)
            }
        }
        addButton.setOnClickListener {
            addImageCard(this, contenedor)
        }
        enviarButton.setOnClickListener {
            mostrarDialogoActa { expediente ->
                lifecycleScope.launch(Dispatchers.IO) {
                    dao.actualizarExpedientePorGuid(guid!!, expediente.codigoExpediente, expediente.id)
                    Toast.makeText(this@FotosActivity, "Fotos enviadas con éxito", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@FotosActivity, ExpedientesActivity::class.java)
                    startActivity(intent)
                }
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
                            guid!!
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
        val photoFile = Imagenes.createImageFile(this, guid!!)
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        tempFile = photoFile
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri) // Guardar la foto en el archivo
        }
        takePictureLauncher.launch(intent)
    }

    // Metodo que se llama cuando se recibe la respuesta del usuario sobre los permisos
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
                setMargins(0, Vistas.dpToPx(context, 8), 0, Vistas.dpToPx(context, 8))
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
                    setMargins(Vistas.dpToPx(context, 4), 0, Vistas.dpToPx(context, 4), 0)
                }
                radius = Vistas.dpToPxF(context, 16)
                cardElevation = Vistas.dpToPxF(context, 6)
                preventCornerOverlap = true
                useCompatPadding = true
            }

            val imageView = ImageView(context).apply {
                id = View.generateViewId()
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Vistas.dpToPx(context, 100)
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

    //Añade a la vista las imágenes ya creadas en la carpeta de la inspección
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun mostrarImagenesExistentes(context: Context, container: LinearLayout, guid: String) {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), guid)
        if (!dir.exists() || !dir.isDirectory) return

        val imageFiles = dir.listFiles { file ->
            file.name.startsWith("photo_") && file.name.endsWith(".jpg")
        }?.sortedBy { it.lastModified() } ?: return

        val imagesPerRow = 3
        var currentRow: LinearLayout? = null
        var itemsInRow = 0

        imageFiles.forEachIndexed { index, file ->
            // Crea una nueva fila cada 'imagesPerRow' imágenes o si es la primera imagen
            if (index % imagesPerRow == 0) {
                currentRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, Vistas.dpToPx(context, 8), 0, Vistas.dpToPx(context, 8))
                    }
                    gravity = Gravity.CENTER_HORIZONTAL // O Gravity.CENTER dependiendo de lo que necesites
                    // weightSum no es estrictamente necesario si los hijos tienen layout_weight y ancho 0dp,
                    // pero si lo usas, asegúrate de que coincida con la suma de los pesos.
                    // weightSum = imagesPerRow.toFloat() // O 3f si siempre esperas 3 columnas visualmente
                }
                container.addView(currentRow)
            }

            // Asegúrate de que currentRow no sea null (debería haberse creado arriba)
            currentRow?.let { row ->
                val cardView = CardView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0, // Ancho 0dp para que layout_weight funcione
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f  // Cada CardView ocupa 1 parte del peso total
                    ).apply {
                        setMargins(Vistas.dpToPx(context, 4), 0, Vistas.dpToPx(context, 4), 0)
                    }
                    radius = Vistas.dpToPxF(context, 16)
                    cardElevation = Vistas.dpToPxF(context, 6)
                    preventCornerOverlap = true
                    useCompatPadding = true // Esto añade padding para las sombras, considera si lo necesitas con márgenes
                }

                val imageView = ImageView(context).apply {
                    id = View.generateViewId() // Genera un ID único para cada ImageView
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Vistas.dpToPx(context, 100) // Altura fija, ajusta según necesidad
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    // Cargar la imagen desde el archivo
                    val imageUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    setImageURI(imageUri)
                    tag = imageUri // Guardar el URI en el tag para referencia futura

                    setOnClickListener {
                        // Reutiliza tu lógica para mostrar la previsualización o abrir la cámara
                        // Podrías necesitar pasar el CardView y el container (LinearLayout principal)
                        openImagePreview(cardView, container, this)
                    }
                }
                imageViews.add(imageView) // Añade a tu lista global si es necesario
                cardView.addView(imageView)
                row.addView(cardView)
            }
        }

        // (Opcional) Si la última fila no está completa (p.ej. 1 o 2 imágenes en una fila de 3),
        // y quieres que los elementos se distribuyan ocupando el espacio de 3,
        // puedes añadir CardViews vacíos o ajustar los pesos.
        // O, si usas Gravity.START en el LinearLayout de la fila, se alinearán a la izquierda.
        // Si usas Gravity.CENTER_HORIZONTAL, la fila en sí se centrará, pero los elementos dentro de ella
        // se distribuirán según sus pesos.

        // Si quieres que la última fila siempre parezca tener 3 espacios, incluso si algunos están vacíos:
        val remainingCells = imagesPerRow - (imageFiles.size % imagesPerRow)
        if (imageFiles.isNotEmpty() && remainingCells != imagesPerRow && remainingCells > 0) {
            currentRow?.let { row ->
                for (i in 0 until remainingCells) {
                    val emptySpace = View(context).apply { // Puedes usar un FrameLayout o un Space si prefieres
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1f // Ocupa el espacio restante
                        ).apply {
                            setMargins(Vistas.dpToPx(context, 4), 0, Vistas.dpToPx(context, 4), 0)
                        }
                    }
                    row.addView(emptySpace)
                }
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
                setMargins(Vistas.dpToPx(context, 4), 0, Vistas.dpToPx(context, 4), 0)
            }
            radius = Vistas.dpToPxF(context, 16)
            cardElevation = Vistas.dpToPxF(context, 6)
            preventCornerOverlap = true
            useCompatPadding = true
        }

        val imageView = ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Vistas.dpToPx(context, 100)
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
    private fun crearCardVaciaPulsable(context: Context): CardView {
        val cardView = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(Vistas.dpToPx(context, 4), 0, Vistas.dpToPx(context, 4), 0)
            }
            radius = Vistas.dpToPxF(context, 16)
            cardElevation = Vistas.dpToPxF(context, 6)
            preventCornerOverlap = true
            useCompatPadding = true
        }

        val imageView = ImageView(context).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Vistas.dpToPx(context, 100)
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


    //Muestra el Dialogo de subida de fotos, permite seleccioanr el expediente
    private fun mostrarDialogoActa(onConfirmar: (Expediente) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_acta, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner)
        val fotoCountText = dialogView.findViewById<TextView>(R.id.fotoCountText)
        val valores = ArrayList<String>()
        val expedientes = ArrayList<Expediente>()
        var expedienteSeleccionado = Expediente()
        val apiService = RetrofitClient.getApiService()
        //Obtener lista de últimos expedientes (modificable con reg_mostrar)
        apiService.paginarExpedientes(PaginarRequest("", "", 10, VariablesGlobales.user_id)).enqueue(object : Callback<PaginarResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<PaginarResponse>, response: Response<PaginarResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        for (data in it.data){
                            valores.add(data.codigoExpediente)
                            expedientes.add(data)
                        }
                        val adapter = ArrayAdapter(this@FotosActivity, android.R.layout.simple_spinner_item, valores)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                        if (valores.isNotEmpty()) {
                            expedienteSeleccionado = expedientes.first { it.codigoExpediente == valores[0] }
                            spinner.setSelection(0)
                        }
                        spinner.post {
                            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                    for (exp in expedientes){
                                        if (exp.codigoExpediente == valores[position]){
                                            expedienteSeleccionado = exp
                                        }
                                    }
                                }
                                override fun onNothingSelected(parent: AdapterView<*>) {
                                }
                            }
                        }
                        fotoCountText.text = "Tienes ${contarFotosReales()} fotos actualmente"
                        val imagenFiles = mutableListOf<File>()
                        val dialog = AlertDialog.Builder(this@FotosActivity)
                            .setView(dialogView)
                            .setTitle("Enviar Fotos")
                            .setCancelable(false)
                            .setPositiveButton("Confirmar", null)
                            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
                            .create()
                        dialog.setOnShowListener {
                            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            button.setOnClickListener {
                                imageViews.forEach { image ->
                                    val encoded = Imagenes.convertirImagenViewAFile(image)
                                    if (encoded != null){
                                        imagenFiles.add(encoded)
                                    }
                                }
                                val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
                                progressBar.visibility = View.VISIBLE
                                progressBar.progress = 0
                                val envioText = dialogView.findViewById<TextView>(R.id.envioText)
                                envioText.visibility = View.VISIBLE

                                lifecycleScope.launch {
                                    val errores = mutableListOf<String>()
                                    val total = imagenFiles.size

                                    for ((index, image) in imagenFiles.withIndex()) {
                                        val codigo = expedienteSeleccionado.codigoExpediente + "_FO${index + 1}"
                                        val fileBody = image.asRequestBody("image/jpeg".toMediaTypeOrNull())
                                        val filePart = MultipartBody.Part.createFormData("file", image.name, fileBody)
                                        fun toPart(value: Any): RequestBody =
                                            value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                        val success = withContext(Dispatchers.IO) {
                                            try {
                                                val response = RetrofitClient.getApiService().subirFoto(filePart,
                                                    toPart(codigo),
                                                    toPart(codigo),
                                                    toPart(codigo),
                                                    toPart(VariablesGlobales.user_id),
                                                    toPart(expedienteSeleccionado.id),
                                                    toPart(9)).execute()
                                                response.isSuccessful
                                            } catch (e: Exception) {
                                                false
                                            }
                                        }
                                        if (!success) {
                                            errores.add(codigo)
                                        }
                                        envioText.setText("Enviando " + codigo)
                                        progressBar.progress = ((index + 1) * 100) / total
                                        delay(200)
                                    }
                                    progressBar.visibility = View.GONE
                                    if (errores.isNotEmpty()) {
                                        AlertDialog.Builder(this@FotosActivity)
                                            .setTitle("Errores")
                                            .setMessage("Fallaron las siguientes fotos:\n${errores.joinToString("\n")}")
                                            .setPositiveButton("OK", null)
                                            .show()
                                    } else {
                                        Toast.makeText(this@FotosActivity, "Fotos enviadas correctamente", Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                        onConfirmar(expedienteSeleccionado)
                                    }
                                }
                            }
                        }
                        dialog.show()
                    }
                } else {
                    Log.d("API", "ERROR: " + response.code())
                }
            }
            override fun onFailure(call: Call<PaginarResponse>, t: Throwable) {
                Log.d("API", "Fallo llamada a la API: " + t.message)
                Toast.makeText(this@FotosActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    //Cuenta la cantidad de ImageViews que tienen foto
    fun contarFotosReales(): Int {
        return imageViews.count { imageView ->
            imageView.drawable != null
        }
    }
}
