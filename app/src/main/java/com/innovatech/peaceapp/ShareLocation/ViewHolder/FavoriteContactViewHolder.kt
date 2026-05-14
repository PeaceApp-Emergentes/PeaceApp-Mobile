package com.innovatech.peaceapp.ShareLocation.ViewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.innovatech.peaceapp.DB.AppDatabase
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.ShareLocation.Entity.ContactEntity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.notify

class FavoriteContactViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val database = AppDatabase.getDatabase(view.context)

    val contactName = view.findViewById<TextView>(R.id.txtContactName)
    val contactImage = view.findViewById<ImageView>(R.id.contactImage)
    val trashcan = view.findViewById<ImageView>(R.id.trashcan)

    fun render(contactModel: ContactEntity){
        contactName.text = contactModel.name

        if (contactModel.image == null){
            contactImage.setImageResource(R.drawable.image_user_default)
        } else{
            Picasso.get().load(contactModel.image).into(contactImage)
        }

        trashcan.setOnClickListener{
            GlobalScope.launch{
                database.contactDAO().delete(contactModel)

                withContext(Dispatchers.Main){
                    notify()
                }

            }
        }
    }
}