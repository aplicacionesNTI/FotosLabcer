package com.itevebasa.fotoslabcer.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itevebasa.fotoslabcer.R
import com.itevebasa.fotoslabcer.modelos.Expediente

class ExpedienteAdapter(
    private val expedienteList: List<Expediente>,
    private val onItemClick: (Expediente) -> Unit
) : RecyclerView.Adapter<ExpedienteAdapter.ExpedienteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpedienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expedientes_adapter, parent, false)
        return ExpedienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpedienteViewHolder, position: Int) {
        val expediente = expedienteList[position]
        holder.nombreText.text = "Nombre: " + expediente.nombre


        holder.itemView.setOnClickListener {
            onItemClick(expediente)
        }
    }

    override fun getItemCount(): Int = expedienteList.size

    class ExpedienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreText: TextView = itemView.findViewById(R.id.nombreText)
    }
}