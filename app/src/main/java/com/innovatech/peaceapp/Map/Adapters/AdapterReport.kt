package com.innovatech.peaceapp.Map.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.innovatech.peaceapp.Map.Beans.Report
import com.innovatech.peaceapp.Map.ViewHolders.ReportViewHolder
import com.innovatech.peaceapp.R

class AdapterReport(val reportsList: List<Report>) : RecyclerView.Adapter<ReportViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ReportViewHolder(layoutInflater.inflate(R.layout.card_report_map, parent, false))
    }

    override fun getItemCount(): Int = reportsList.size

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = reportsList[position]
        holder.bind(item)
    }
}