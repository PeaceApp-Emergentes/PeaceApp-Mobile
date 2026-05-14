package com.innovatech.peaceapp.ShareLocation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.ShareLocation.Entity.ContactEntity
import com.innovatech.peaceapp.ShareLocation.ViewHolder.FavoriteContactViewHolder

class AdapterFavorites(
    private var favoriteList: MutableList<ContactEntity>,
    private val onDeleteSelected: (ContactEntity) -> Unit
): RecyclerView.Adapter<FavoriteContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteContactViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return FavoriteContactViewHolder(layoutInflater.inflate(R.layout.card_favorite_contact, parent, false))
    }

    override fun getItemCount(): Int = favoriteList.size

    override fun onBindViewHolder(holder: FavoriteContactViewHolder, position: Int) {
        val item = favoriteList[position]
        holder.render(item)
        val trashcan = holder.itemView.findViewById<ImageView>(R.id.trashcan)
        trashcan.setOnClickListener {
            onDeleteSelected(item)
            removeContact(item)
        }
    }

    fun updateFavorites(newFavoriteList: List<ContactEntity>) {
        favoriteList.clear()
        favoriteList.addAll(newFavoriteList)
        notifyDataSetChanged()
    }
    fun removeContact(contact: ContactEntity) {
        val pos = favoriteList.indexOf(contact)
        if(pos!=-1){
            favoriteList.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }
}