package com.itevebasa.fotoslabcer.actividades

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.itevebasa.fotoslabcer.R
import com.itevebasa.fotoslabcer.adaptadores.ExpedienteAdapter
import com.itevebasa.fotoslabcer.auxiliar.Permisos
import com.itevebasa.fotoslabcer.modelos.Expediente
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExpedientesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpedienteAdapter
    private var expedienteList: MutableList<Expediente> = mutableListOf()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpedienteAdapter(expedienteList) { expediente ->
            val intent = Intent(this, FotosActivity::class.java).apply {
                putExtra("expediente", expediente.nombre)
            }
            startActivity(intent)
        }
        fetchData()
        recyclerView.adapter = adapter
        swipeRefreshLayout.setOnRefreshListener {
            fetchData()
        }
    }

    private fun fetchData() {
        swipeRefreshLayout.isRefreshing = true
        expedienteList.clear()
        expedienteList.add(Expediente("8585-qweq54"))
        expedienteList.add(Expediente("9584-asda985"))
        expedienteList.add(Expediente("9999-ert94"))
        adapter.notifyDataSetChanged()
        swipeRefreshLayout.isRefreshing = false
    }
}