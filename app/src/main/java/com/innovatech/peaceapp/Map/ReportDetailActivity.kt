package com.innovatech.peaceapp.Map

import android.app.Dialog
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.Map.Beans.Report
import com.innovatech.peaceapp.Map.Models.RetrofitClient
import com.innovatech.peaceapp.R
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportDetailActivity : AppCompatActivity() {
    lateinit var token: String
    private lateinit var report: Report
    private lateinit var txtTipoReporte: TextView
    private lateinit var imgReporte: ImageView
    private lateinit var txtUbicacionReporte: TextView
    private lateinit var txtFechaReporte: TextView
    private lateinit var txtDescripcionReporte: TextView
    private lateinit var btnSalirReporteDetallado: Button
    private lateinit var txtTituloReporte: TextView
    private lateinit var btnDeleteReport: ImageView
    private var userId: Int = 0
    private lateinit var txtEstadoReporte: TextView
    private lateinit var txtRejectionReason: TextView
    private lateinit var btnAprobar: Button
    private lateinit var btnRechazar: Button
    private lateinit var containerAccionesAdmin: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        token = GlobalToken.token
        val reportTemp = intent.getSerializableExtra("report") as Report

        // IDs
        userId = getSharedPreferences("GlobalPrefs", MODE_PRIVATE).getInt("userId", 0)

        // UI
        txtTipoReporte = findViewById(R.id.txtTipoReporte)
        imgReporte = findViewById(R.id.imgReporte)
        txtUbicacionReporte = findViewById(R.id.txtUbicacionReporte)
        txtFechaReporte = findViewById(R.id.txtFechaReporte)
        txtDescripcionReporte = findViewById(R.id.txtDescripcionReporte)
        txtTituloReporte = findViewById(R.id.txtTituloReporte)
        btnSalirReporteDetallado = findViewById(R.id.btnSalirReporteDetallado)
        btnDeleteReport = findViewById(R.id.btnDeleteReport)
        txtEstadoReporte = findViewById(R.id.txtEstadoReporte)
        txtRejectionReason = findViewById(R.id.txtRejectionReason)
        btnAprobar = findViewById(R.id.btnAprobarReporte)
        btnRechazar = findViewById(R.id.btnRechazarReporte)
        containerAccionesAdmin = findViewById(R.id.containerAccionesAdmin)

        btnAprobar.setOnClickListener { approveReport() }
        btnRechazar.setOnClickListener { showRejectDialog() }

        // Primero: traer versión ACTUAL del reporte
        loadReportFromServer(reportTemp.id)

        btnSalirReporteDetallado.setOnClickListener { finish() }
        btnDeleteReport.setOnClickListener { showDeleteReportDialog() }

        navigationMenu()
    }
    private fun loadReportFromServer(reportId: Int) {
        val service = RetrofitClient.getClient(token)
        val userRole = getSharedPreferences("GlobalPrefs", MODE_PRIVATE).getString("userRole", "")

        service.getReportById(reportId).enqueue(object : Callback<Report> {
            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                val r = response.body() ?: return
                report = r

                // Mostrar botón eliminar si corresponde
                btnDeleteReport.visibility =
                    if (userId == report.userId || userRole == "ROLE_ADMIN") View.VISIBLE
                    else View.GONE

                // Si es admin y reporte está PENDING → pasar a IN_REVIEW
                if (userRole == "ROLE_ADMIN" && report.state == "PENDING") {

                    // No mostrar nada todavía
                    txtEstadoReporte.text = "Cambiando a En revisión..."
                    imgReporte.visibility = View.GONE

                    markReportAsInReview()

                } else {
                    setReportData()
                }
                // ACCIONES SOLO PARA ADMIN Y SOLO SI NO ESTA APROBADO NI RECHAZADO
                if (userRole == "ROLE_ADMIN" && report.state != "APPROVED" && report.state != "REJECTED") {
                    containerAccionesAdmin.visibility = View.VISIBLE
                } else {
                    containerAccionesAdmin.visibility = View.GONE
                }


            }

            override fun onFailure(call: Call<Report>, t: Throwable) {
                Log.e("Error", "No se pudo cargar reporte: ${t.message}")
            }
        })
    }
    private fun approveReport() {
        val service = RetrofitClient.getClient(token)

        service.approve(report.id).enqueue(object : Callback<Report> {
            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {
                    report = response.body()!!
                    setReportData()
                    containerAccionesAdmin.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<Report>, t: Throwable) {
                Log.e("Error", "Error al aprobar: ${t.message}")
            }
        })
    }
    private fun showRejectDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_reject_report)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etReason = dialog.findViewById<TextView>(R.id.etRejectReason)
        val btnReject = dialog.findViewById<Button>(R.id.btnConfirmReject)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelReject)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnReject.setOnClickListener {
            val reason = etReason.text.toString().trim()

            if (reason.isEmpty()) {
                etReason.error = "Escribe un motivo"
                return@setOnClickListener
            }

            dialog.dismiss()
            rejectReport(reason)
        }

        dialog.show()
    }
    private fun rejectReport(reason: String) {
        val service = RetrofitClient.getClient(token)

        val body = mapOf("reason" to reason)

        service.reject(report.id, body).enqueue(object : Callback<Report> {
            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {
                    report = response.body()!!
                    setReportData()
                    containerAccionesAdmin.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<Report>, t: Throwable) {
                Log.e("Error", "Error al rechazar: ${t.message}")
            }
        })
    }

    private fun showDeleteReportDialog(){
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_delete_new_report)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnEliminar = dialog.findViewById<Button>(R.id.btnEliminarPost)
        val tvMensaje = dialog.findViewById<TextView>(R.id.tvDeleteNewReport)

        tvMensaje.text = "Estás a punto de eliminar el reporte " + "\"" + report.title + "\"" + ". ¿Estás seguro?"

        btnCancel.setOnClickListener {
            dialog.hide()
        }
        btnEliminar.setOnClickListener {
            dialog.hide()

            val service = RetrofitClient.getClient(token)
            service.deleteReport(report.id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        finish()
                        val intent = Intent(this@ReportDetailActivity, ListReportsActivity::class.java)
                        intent.putExtra("token", token)
                        startActivity(intent)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("Error", t.message.toString())
                }
            })
        }

        dialog.show()
    }
    private fun markReportAsInReview() {
        val service = RetrofitClient.getClient(token)

        service.markInReview(report.id).enqueue(object : Callback<Report> {
            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {
                    report = response.body()!!
                    setReportData()   // <-- ahora sí actualiza la pantalla aquí
                }
            }


            override fun onFailure(call: Call<Report>, t: Throwable) {
                Log.e("Error", "Error al cambiar a IN_REVIEW: ${t.message}")
            }
        })
    }

    private fun setReportData() {

        txtTituloReporte.text = report.title
        txtTipoReporte.text = translateType(report.type)
        txtUbicacionReporte.text = report.location
        txtFechaReporte.text = report.createdAt.substring(0,10)
        txtDescripcionReporte.text = report.description

        // ESTADO TRADUCIDO
        txtEstadoReporte.text = when(report.state) {
            "APPROVED" -> "Aprobado"
            "IN_REVIEW" -> "En revisión"
            "PENDING" -> "Pendiente"
            "REJECTED" -> "Rechazado"
            else -> report.state
        }

        // SI ES RECHAZADO → OCULTAR IMAGEN Y MOSTRAR MOTIVO
        if (report.state == "REJECTED") {
            imgReporte.visibility = View.GONE
            txtRejectionReason.visibility = View.VISIBLE
            txtRejectionReason.text =
                if (!report.rejectionReason.isNullOrEmpty())
                    "Motivo del rechazo: ${report.rejectionReason}"
                else
                    "Este reporte fue rechazado"
        } else {
            imgReporte.visibility = View.VISIBLE
            txtRejectionReason.visibility = View.GONE
            Picasso.get().load(report.imageUrl)
                .resize(300, 300)
                .centerCrop().into(imgReporte)
        }
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
                    true
                }
                else -> false
            }

        }

        bottomNavigationView.menu.findItem(R.id.nav_report).setChecked(true)
    }
}