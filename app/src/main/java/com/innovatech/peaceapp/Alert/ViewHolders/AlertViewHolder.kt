package com.innovatech.peaceapp.Alert.ViewHolders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.innovatech.peaceapp.Alert.Beans.Alert
import com.innovatech.peaceapp.R
import com.squareup.picasso.Picasso

class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val alertImage: ImageView = view.findViewById(R.id.imgReport) // Change to imgReport
    private val alertTitle: TextView = view.findViewById(R.id.txtTitle)
    private val alertDescription: TextView = view.findViewById(R.id.txtDescription) // Now this is defined
    private val alertLocation: TextView = view.findViewById(R.id.txtLocation)

    fun bind(alertModel: Alert) {
        // Render the alert information
        render(alertModel)
    }

    private fun render(alertModel: Alert) {
        alertTitle.text = alertModel.type
        alertDescription.text = alertModel.description ?: "No description provided"
        alertLocation.text = alertModel.location

        // Load the image using Picasso
        if (!alertModel.imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(alertModel.imageUrl) // Load the image URL
                .resize(300, 300) // Resize as needed
                .centerCrop() // Center crop the image
                .into(alertImage) // Set the image in the ImageView
        } else {
            // Set the default image if no URL is provided
            alertImage.setImageResource(R.drawable.image_report_not_found)
        }
    }
}
