package com.innovatech.peaceapp.Map.ViewHolders

import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.innovatech.peaceapp.DB.AppDatabase
import com.innovatech.peaceapp.DB.Entities.LocationModel
import com.innovatech.peaceapp.Map.Beans.PropertiesPlace
import com.innovatech.peaceapp.Map.MapActivity
import com.innovatech.peaceapp.Map.Models.RetrofitClientMapbox
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.StartingPoint.GlobalRecentLocation
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.camera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationRecentViewHolder(view: View) : RecyclerView.ViewHolder(view){
    val addressLocation = view.findViewById<TextView>(R.id.txtAddressRecentLocation)
    val secondAddressLocation = view.findViewById<TextView>(R.id.txtSecAddressRecentLocation)

    fun render(location: LocationModel) {
        addressLocation.text = location.firstAddress
        secondAddressLocation.text = location.secondAddress

        itemView.setOnClickListener {
            GlobalRecentLocation.latitude = location.latitude!!
            GlobalRecentLocation.longitude = location.longitude!!
            Log.i("andriushinis", GlobalRecentLocation.longitude.toString() + " " + GlobalRecentLocation.latitude.toString())
        }
    }

}