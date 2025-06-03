package com.itevebasa.fotoslabcer.actividades

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.itevebasa.fotoslabcer.R
import com.itevebasa.fotoslabcer.adaptadores.ExpedienteAdapter
import com.itevebasa.fotoslabcer.adaptadores.GuidAdapter
import com.itevebasa.fotoslabcer.auxiliar.Permisos
import com.itevebasa.fotoslabcer.conexion.AppDatabase
import com.itevebasa.fotoslabcer.daos.InspeccionDao
import com.itevebasa.fotoslabcer.modelos.Inspeccion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpedientesActivity : AppCompatActivity() {

    private lateinit var abiertasRecyclerView: RecyclerView
    private lateinit var adapterAbiertas: GuidAdapter
    private var inspeccionesAbiertasList: MutableList<Inspeccion> = mutableListOf()
    private lateinit var abiertasSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var nombreEdit: EditText
    private lateinit var db: AppDatabase
    private lateinit var dao: InspeccionDao

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!Permisos.isCameraPermissionGranted(this) || !Permisos.isStoragePermissionGranted(this) || !Permisos.isLocationPermissionGranted(this)) {
            Permisos.requestPermissions(this)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expedientes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        nombreEdit = findViewById(R.id.nombreEdit)
        nombreEdit.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && nombreEdit.text.toString() == getString(R.string.nombre)) {
                nombreEdit.text.clear()
            }
        }
        db = AppDatabase.getDatabase(this)
        dao = db.inspeccionDao()
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val cerrarSesionBtn: Button = findViewById(R.id.cerrarSesionBtn)
        cerrarSesionBtn.setOnClickListener{
            sharedPreferences.edit().clear().apply()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val inspeccionBtn: Button = findViewById(R.id.inspeccionBtn)
        inspeccionBtn.setOnClickListener{
            if (nombreEdit.text.toString() != "" && nombreEdit.text.toString() != "Nombre"){
                val intent = Intent(this, FotosActivity::class.java).apply {
                    putExtra("nombre", nombreEdit.text.toString())
                }
                startActivity(intent)
            }else{
                nombreEdit.error = "Escribe un nombre para la inspección"
            }

        }
        val historialBtn: Button = findViewById(R.id.historialBtn)
        historialBtn.setOnClickListener{
            val intent = Intent(this, HistorialActivity::class.java)
            startActivity(intent)
        }
        abiertasSwipeRefreshLayout = findViewById(R.id.abiertasSwipeRefreshLayout)
        abiertasRecyclerView = findViewById(R.id.abiertasRecyclerview)
        abiertasRecyclerView.layoutManager = LinearLayoutManager(this)
        adapterAbiertas = GuidAdapter(inspeccionesAbiertasList) { inspeccion ->
            val intent = Intent(this, FotosActivity::class.java).apply {
                putExtra("guid", inspeccion.guid)
            }
            startActivity(intent)
        }

        fetchAbiertas()

        abiertasRecyclerView.adapter = adapterAbiertas

        adapterAbiertas.notifyDataSetChanged()
        abiertasSwipeRefreshLayout.setOnRefreshListener {
            fetchAbiertas()
            adapterAbiertas.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAbiertas()
        adapterAbiertas.notifyDataSetChanged()
        nombreEdit.setText("Nombre")
    }


    private fun fetchAbiertas(){
        abiertasSwipeRefreshLayout.isRefreshing = true
        lifecycleScope.launch(Dispatchers.IO) {
            val inspeccionesAbiertas = dao.obtenerInspeccionesSinExpediente()
            inspeccionesAbiertasList.clear()
            inspeccionesAbiertasList.addAll(inspeccionesAbiertas)
            abiertasSwipeRefreshLayout.isRefreshing = false
        }
    }
}