package com.itevebasa.fotoslabcer.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itevebasa.fotoslabcer.R
import com.itevebasa.fotoslabcer.modelos.Inspeccion

class ExpedienteAdapter(
    private val inspeccionList: List<Inspeccion>,
    private val onItemClick: (Inspeccion) -> Unit
) : RecyclerView.Adapter<ExpedienteAdapter.ExpedienteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpedienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expedientes_adapter, parent, false)
        return ExpedienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpedienteViewHolder, position: Int) {
        val expediente = inspeccionList[position]
        holder.nombreText.text = "Acta: " + expediente.acta


        holder.itemView.setOnClickListener {
            onItemClick(expediente)
        }
    }

    override fun getItemCount(): Int = inspeccionList.size

    class ExpedienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreText: TextView = itemView.findViewById(R.id.nombreText)
    }
}