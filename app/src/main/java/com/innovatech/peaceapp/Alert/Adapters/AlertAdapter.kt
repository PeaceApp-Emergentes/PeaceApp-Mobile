package com.innovatech.peaceapp.Alert.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.innovatech.peaceapp.Alert.Beans.Alert
import com.innovatech.peaceapp.R
import com.squareup.picasso.Picasso

class AlertAdapter(private val alerts: List<Alert>, private val onClick: (Alert) -> Unit) :
    RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alert_card, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.bind(alert)

        // Set an onClickListener on the item
        holder.itemView.setOnClickListener {
            onClick(alert)
        }
    }

    override fun getItemCount() = alerts.size

    inner class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtLocation: TextView = view.findViewById(R.id.txtLocation)
        private val txtDescription: TextView = view.findViewById(R.id.txtDescription)
        private val imgReport: ImageView = view.findViewById(R.id.imgReport)

        fun bind(alert: Alert) {
            txtLocation.text = alert.location
            txtDescription.text = alert.description

            // Load image using Picasso
            alert.imageUrl?.let {
                Picasso.get().load(it).into(imgReport)
            } ?: run {
                imgReport.setImageResource(R.drawable.image_report_not_found) // Default image if not available
            }
        }
    }
}
