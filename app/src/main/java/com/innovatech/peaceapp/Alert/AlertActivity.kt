package com.innovatech.peaceapp.Alert

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.innovatech.peaceapp.Alert.Adapters.AlertAdapter
import com.innovatech.peaceapp.Alert.Beans.Alert
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.Alert.Models.RetrofitClient
import com.innovatech.peaceapp.GlobalToken.token
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlertActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtCurrentLocation: TextView
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alert_list)

        // Retrieve userId from SharedPreferences
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)

        // Initialize views
        txtCurrentLocation = findViewById(R.id.tvCurrentLocation)
        recyclerView = findViewById(R.id.recyclerAlert)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get current location from intent and display it immediately
        val currentLocation = intent.getStringExtra("currentLocation") ?: "Location not available"
        Log.i("AlertActivity", "Current Location Received: $currentLocation")
        txtCurrentLocation.text = currentLocation

        // Fetch and display alerts for the current location when the user presses the alert button
        fetchAlertsForUser(userId)
    }

    // Method to fetch alerts for a specific location
// Method to fetch alerts for a specific user
    private fun fetchAlertsForUser(userId: Int) {
        val api = RetrofitClient.getClient(token)

        // Make the API call to fetch alerts for the specific userId
        api.getAlertsByUser(userId).enqueue(object : Callback<List<Alert>> {
            override fun onResponse(call: Call<List<Alert>>, response: Response<List<Alert>>) {
                val alerts = response.body()
                if (alerts != null && alerts.isNotEmpty()) {
                    setupRecyclerView(alerts)
                }
            }

            override fun onFailure(call: Call<List<Alert>>, t: Throwable) {
                Log.e("AlertActivity", "Error fetching alerts for user: ${t.message}")
            }
        })
    }


    private fun setupRecyclerView(alerts: List<Alert>) {
        recyclerView.adapter = AlertAdapter(alerts) {}
    }
}
