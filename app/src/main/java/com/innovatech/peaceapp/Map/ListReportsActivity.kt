package com.innovatech.peaceapp.Map

import Beans.Location
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.innovatech.peaceapp.Map.Adapters.AdapterReport
import com.innovatech.peaceapp.Map.Beans.Report
import com.innovatech.peaceapp.Map.Models.RetrofitClient
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.ShareLocation.ContactsListActivity
import com.mapbox.geojson.Point
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListReportsActivity : AppCompatActivity() {
    lateinit var token: String
    private var userId: Int = 0
    private lateinit var btnNewReport: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_reports)
        token = intent.getStringExtra("token")!!

        val allReports: LinearLayout = findViewById(R.id.allReports)
        val myReports: LinearLayout = findViewById(R.id.myReports)
        val txtAllReports: TextView = findViewById(R.id.txtAllReports)
        val txtMyReports: TextView = findViewById(R.id.txtMyReports)
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        btnNewReport = findViewById(R.id.btnNewReport)
        userId = sharedPref.getInt("userId", 0)

        tabSelected(myReports, txtMyReports)
        tabUnselected(allReports, txtAllReports)


        // Todos los reportes is selected
        allReports.setOnClickListener {
            btnNewReport.visibility = View.GONE

            tabSelected(myReports, txtMyReports)
            tabUnselected(allReports, txtAllReports)
            obtainAllReports()
        }

        // Mis reportes is selected
        myReports.setOnClickListener {
            btnNewReport.visibility = View.VISIBLE

            tabSelected(allReports, txtAllReports)
            tabUnselected(myReports, txtMyReports)
            obtainMyReports()
        }

        btnNewReport.setOnClickListener {
            val intent = Intent(this, TypeReportsActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        obtainAllReports()
        navigationMenu()
    }

    private fun navigationMenu() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->

            bottomNavigationView.menu.findItem(R.id.nav_map).setIcon(R.drawable.location_icon)
            bottomNavigationView.menu.findItem(R.id.nav_report).setIcon(R.drawable.reports_icon)
            bottomNavigationView.menu.findItem(R.id.nav_shared_location).setIcon(R.drawable.share_location_icon)

            if(item.isChecked) {
                return@setOnNavigationItemSelectedListener false
            }

            when (item.itemId) {
                R.id.nav_map -> {
                    val intent = Intent(this, MapActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                    true
                }
                R.id.nav_report -> {
                    val intent = Intent(this, ListReportsActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                    true
                }
                R.id.nav_shared_location -> {
                    val intent = Intent(this, ContactsListActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                    true
                }
                else -> false
            }

        }

        bottomNavigationView.menu.findItem(R.id.nav_report).setChecked(true)
    }


    private fun obtainAllReports() {
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        val userRole = sharedPref.getString("userRole", "")

        val service = RetrofitClient.getClient(token)

        service.getAllReports().enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                val reports = response.body()
                val listReports = mutableListOf<Report>()

                if (reports != null) {

                    val filteredReports =
                        if (userRole == "ROLE_ADMIN") {

                            reports
                        } else {

                            reports.filter { it.state == "APPROVED" }
                        }

                    for (report in filteredReports) {

                        if (report.imageUrl == null)
                            report.imageUrl = "https://res.cloudinary.com/dqawjz3ih/image/upload/v1728969083/image_default_eqfpgm.png"

                        listReports.add(
                            Report(
                                report.id,
                                report.createdAt,
                                report.updatedAt,
                                report.userId,
                                report.description,
                                report.title,
                                translateType(report.type),
                                report.imageUrl,
                                report.location,
                                report.latitude,
                                report.longitude,
                                report.state,
                                report.rejectionReason
                            )
                        )
                    }

                    val recycler = findViewById<RecyclerView>(R.id.recyclerReports)
                    recycler.layoutManager = LinearLayoutManager(applicationContext)
                    recycler.adapter = AdapterReport(listReports)
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                Log.e("GET REPORTS", "Reports not obtained")
            }
        })
    }
    private fun obtainMyReports() {
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        val userRole = sharedPref.getString("userRole", "")

        val service = RetrofitClient.getClient(token)

        service.getAllReports().enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                val reports = response.body()
                val listReports = mutableListOf<Report>()

                if (reports != null) {

                    // SOLO REPORTES CREADOS POR EL USUARIO
                    val myReports = reports.filter { it.userId == userId }

                    for (report in myReports) {

                        if (report.imageUrl == null)
                            report.imageUrl = "https://res.cloudinary.com/dqawjz3ih/image/upload/v1728969083/image_default_eqfpgm.png"

                        listReports.add(
                            Report(
                                report.id,
                                report.createdAt,
                                report.updatedAt,
                                report.userId,
                                report.description,
                                report.title,
                                translateType(report.type),
                                report.imageUrl,
                                report.location,
                                report.latitude,
                                report.longitude,
                                report.state,
                                report.rejectionReason
                            )
                        )
                    }

                    val recycler = findViewById<RecyclerView>(R.id.recyclerReports)
                    recycler.layoutManager = LinearLayoutManager(applicationContext)
                    recycler.adapter = AdapterReport(listReports)
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                Log.e("GET REPORTS", "Reports not obtained")
            }
        })
    }

    private fun translateType(type: String?): String {
        return when (type?.uppercase()) {
            "ROBBERY" -> "Robo"
            "ACCIDENT" -> "Accidente"
            "DARK_AREA" -> "Zona oscura"
            "HARASSMENT" -> "Acoso"
            "OTHER" -> "Otro"
            else -> type ?: ""
        }
    }

    // Function to change the background of the tab when it is UNSELECTED
    private fun tabSelected(tab: LinearLayout, txt: TextView) {
        tab.setBackgroundResource(R.drawable.tab_selected)
        txt.setTextColor(resources.getColor(R.color.peaceapp_blue_dark_disable))
    }

    // Function to change the background of the tab when it is SELECTED
    private fun tabUnselected(tab: LinearLayout, txt: TextView) {
        tab.setBackgroundResource(R.drawable.rounded_top_corners_white_map)
        txt.setTextColor(resources.getColor(R.color.black))
    }

    private fun cleanReports() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerReports)
        recycler.adapter = null
    }
}