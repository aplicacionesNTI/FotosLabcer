package com.itevebasa.fotoslabcer.actividades

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.itevebasa.fotoslabcer.R
import com.itevebasa.fotoslabcer.adaptadores.ExpedienteAdapter
import com.itevebasa.fotoslabcer.conexion.AppDatabase
import com.itevebasa.fotoslabcer.daos.InspeccionDao
import com.itevebasa.fotoslabcer.modelos.Inspeccion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistorialActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpedienteAdapter
    private var inspeccionesTerminadasList: MutableList<Inspeccion> = mutableListOf()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var db: AppDatabase
    private lateinit var dao: InspeccionDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        db = AppDatabase.getDatabase(this)
        dao = db.inspeccionDao()
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpedienteAdapter(inspeccionesTerminadasList) { inspeccion ->
            val intent = Intent(this, FotosActivity::class.java).apply {
                putExtra("guid", inspeccion.guid)
            }
            startActivity(intent)
        }
        fetchTerminadas()
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
        swipeRefreshLayout.setOnRefreshListener {
            fetchTerminadas()
            adapter.notifyDataSetChanged()
        }
    }

    private fun fetchTerminadas() {
        swipeRefreshLayout.isRefreshing = true
        lifecycleScope.launch(Dispatchers.IO) {
            val inspeccionesTerminadas = dao.obtenerInspeccionesConExpediente()
            inspeccionesTerminadasList.clear()
            inspeccionesTerminadasList.addAll(inspeccionesTerminadas)
            swipeRefreshLayout.isRefreshing = false
        }
    }
}