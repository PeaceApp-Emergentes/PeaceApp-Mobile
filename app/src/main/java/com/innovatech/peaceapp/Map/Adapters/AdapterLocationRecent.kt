package com.innovatech.peaceapp.Map.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.innovatech.peaceapp.DB.Entities.LocationModel
import com.innovatech.peaceapp.Map.ViewHolders.LocationRecentViewHolder
import com.innovatech.peaceapp.R

class AdapterLocationRecent(val locations: List<LocationModel>): RecyclerView.Adapter<LocationRecentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationRecentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LocationRecentViewHolder(layoutInflater.inflate(R.layout.card_dao_locations, parent, false))
    }

    override fun getItemCount(): Int = locations.size

    override fun onBindViewHolder(holder: LocationRecentViewHolder, position: Int){
        val item = locations[position]
        holder.render(item)
    }
}