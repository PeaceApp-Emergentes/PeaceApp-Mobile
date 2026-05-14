package com.innovatech.peaceapp.Map.ViewHolders

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.innovatech.peaceapp.Map.Beans.Report
import com.innovatech.peaceapp.Map.ReportDetailActivity
import com.innovatech.peaceapp.R
import com.squareup.picasso.Picasso

class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val reportImg = view.findViewById<ImageView>(R.id.imgReport)
    val reportTitle = view.findViewById<TextView>(R.id.txtTitle)
    val reportDate = view.findViewById<TextView>(R.id.txtDate)
    val reportLocation = view.findViewById<TextView>(R.id.txtLocation)
    val reportState = view.findViewById<TextView>(R.id.txtState)

    fun bind(reportModel: Report) {
        val intent = Intent(itemView.context, ReportDetailActivity::class.java)
        intent.putExtra("report", reportModel)

        itemView.setOnClickListener {
            itemView.context.startActivity(intent)
        }

        render(reportModel)
    }

    fun render(reportModel: Report) {

        // Datos principales
        reportTitle.text = reportModel.title
        reportDate.text = reportModel.createdAt.substring(0, 10)
        reportLocation.text = reportModel.location

        // STATE
        val state = reportModel.state.uppercase()
        reportState.text = state

        when (state) {
            "APPROVED" -> {
                reportState.text = "Aprobado"
                reportState.setTextColor(itemView.resources.getColor(R.color.green))
            }
            "IN_REVIEW" -> {
                reportState.text = "En revisión"
                reportState.setTextColor(itemView.resources.getColor(R.color.yellow))
            }
            "PENDING" -> {
                reportState.text = "Pendiente"
                reportState.setTextColor(itemView.resources.getColor(R.color.orange))
            }
            "REJECTED" -> {
                reportState.text = "Rechazado"
                reportState.setTextColor(itemView.resources.getColor(R.color.red))
            }
        }

        // Si está REJECTED → ocultar imagen
        if (state == "REJECTED") {
            reportImg.visibility = View.GONE
        } else {
            reportImg.visibility = View.VISIBLE

            Picasso.get()
                .load(reportModel.imageUrl)
                .resize(300, 300)
                .centerCrop()
                .into(reportImg)
        }
    }
}
