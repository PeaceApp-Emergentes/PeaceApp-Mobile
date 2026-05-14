package com.innovatech.peaceapp.ShareLocation.ViewHolder

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.ShareLocation.Beans.Contact
import com.squareup.picasso.Picasso

class ContactViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val contactName = view.findViewById<TextView>(R.id.txtContactName)
    val contactImage = view.findViewById<ImageView>(R.id.contactImage)
    val contactCheckBox = view.findViewById<CheckBox>(R.id.chkContact)

    fun render(contactModel: Contact){
        contactName.text = contactModel.name

        if (contactModel.image == null){
            contactImage.setImageResource(R.drawable.image_user_default)
        } else{
            Picasso.get().load(contactModel.image).into(contactImage)
            contactCheckBox.isChecked = contactModel.isSelected
        }
    }
}