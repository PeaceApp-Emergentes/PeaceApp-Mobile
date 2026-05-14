package com.innovatech.peaceapp.Map

import ReportSchema
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.innovatech.peaceapp.DB.AppDatabase
import com.innovatech.peaceapp.DB.Entities.LocationModel
import com.innovatech.peaceapp.Map.Beans.Report
import com.innovatech.peaceapp.Map.Models.RetrofitClient
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.ShareLocation.ContactsListActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class NewReportActivity : AppCompatActivity() {

    private lateinit var token: String
    private lateinit var txtTypeReport: TextView
    private lateinit var txtLocation: TextView
    private lateinit var edtTitle: EditText
    private lateinit var edtDetail: EditText
    private lateinit var imgMoreEvidence: ImageView
    private lateinit var imgEvidence: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var viewOverlay: View
    private lateinit var cloudinary: Cloudinary
    private lateinit var imgBitmap: Bitmap
    private var userId: Int = 0

    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUEST_CODE_IMAGE_PICKER = 102
    private val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_report)

        txtTypeReport = findViewById(R.id.txtTypeReport)
        txtLocation = findViewById(R.id.txtLocation)
        edtTitle = findViewById(R.id.edtTitle)
        edtDetail = findViewById(R.id.edtDetail)
        imgMoreEvidence = findViewById(R.id.imgMoreEvidence)
        imgEvidence = findViewById(R.id.imgEvidence)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)
        viewOverlay = findViewById(R.id.loadingOverlay)

        // Recuperar datos guardados
        val typeReport = intent.getStringExtra("type") ?: "OTHER"
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        val latitude = sharedPref.getString("latitude", "0.0")!!.toDouble()
        val longitude = sharedPref.getString("longitude", "0.0")!!.toDouble()
        val currentLocation = sharedPref.getString("currentLocation", "No location found")!!
        userId = sharedPref.getInt("userId", 0)
        token = intent.getStringExtra("token")!!

        txtLocation.hint = currentLocation
        txtTypeReport.text = typeReport

        configCloudinary()
        saveImageEvidence()
        navigationMenu()

        btnCancel.setOnClickListener {
            val intent = Intent(this, ListReportsActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        btnSave.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            viewOverlay.visibility = View.VISIBLE

            val title = edtTitle.text.toString()
            val detail = edtDetail.text.toString()

            if (validateFields(title, detail)) {
                lifecycleScope.launch {
                    saveReport(title, detail, latitude, longitude, typeReport, currentLocation)
                    progressBar.visibility = View.GONE
                    viewOverlay.visibility = View.GONE
                }
            } else {
                progressBar.visibility = View.GONE
                viewOverlay.visibility = View.GONE
            }
        }
    }

    private fun configCloudinary() {
        cloudinary = Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", "dqawjz3ih",
                "api_key", getString(R.string.cloudinary_api_key),
                "api_secret", getString(R.string.cloudinary_api_secret)
            )
        )
    }

    // ✅ Traducción a Enum backend
    private fun mapReportType(type: String): String {
        return when (type.lowercase()) {
            "robo" -> "ROBBERY"
            "acoso" -> "HARASSMENT"
            "accidente" -> "ACCIDENT"
            "zona oscura" -> "DARK_AREA"
            else -> "OTHER"
        }
    }
    private suspend fun saveReport(
        title: String,
        detail: String,
        latitude: Double,
        longitude: Double,
        typeReport: String,
        currentLocation: String
    ) {
        val service = RetrofitClient.getClient(token)
        val mappedType = mapReportType(typeReport)

        // 🧩 Validaciones antes de enviar
        if (userId == 0) {
            showIncorrectSignUpDialog("Error: no se encontró el usuario activo. Inicia sesión nuevamente.")
            Log.e("SaveReport", "userId = 0 (no válido)")
            return
        }

        if (currentLocation.isBlank() || currentLocation == "No location found") {
            showIncorrectSignUpDialog("No se pudo obtener la ubicación. Intenta abrir el mapa antes de reportar.")
            Log.e("SaveReport", "Ubicación vacía o inválida: '$currentLocation'")
            return
        }

        val safeLocation = if (currentLocation.isBlank()) "Unknown location" else currentLocation

        // 📸 Subir imagen a Cloudinary
        val urlImage = uploadImage()
        if (urlImage.isNullOrEmpty()) {
            showIncorrectSignUpDialog("Error subiendo la imagen. Intenta nuevamente.")
            return
        }

        Log.i("URL cloudinary", urlImage.toString())

        // 📨 Crear objeto del reporte con datos válidos
        val report = ReportSchema(
            title = title,
            description = detail,
            type = mappedType,
            userId = userId,
            imageUrl = urlImage,
            location = safeLocation,
            latitude = latitude.toString(),
            longitude = longitude.toString()
        )
        Log.i("REPORT_DEBUG", "userId=$userId, type=$mappedType, location='$safeLocation'")
        service.postReport(report).enqueue(object : Callback<Report> {
            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                if (response.isSuccessful) {
                    val createdReport = response.body()
                    if (createdReport != null) {
                        Log.d("Report", "Reporte creado correctamente: ${createdReport.id}")
                        showCorrectReportSaved()
                    } else {
                        showIncorrectSignUpDialog("Error: el servidor no devolvió información del reporte.")
                    }
                } else {
                    Log.e("ReportError", "Error ${response.code()}: ${response.message()}")
                    showIncorrectSignUpDialog("Error al enviar el reporte (${response.code()})")
                }
            }

            override fun onFailure(call: Call<Report>, t: Throwable) {
                Log.e("ReportError", "Fallo en conexión: ${t.message}")
                showIncorrectSignUpDialog("No se pudo conectar al servidor. Revisa tu conexión e inténtalo nuevamente.")
            }
        })
    }

    private fun saveImageEvidence() {
        imgMoreEvidence.setOnClickListener {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            openImageOptions()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                openImageOptions()
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImageOptions() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image From")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            when {
                imageUri != null -> showImgPreviewFromUri(imageUri)
                imageBitmap != null -> showImgPreviewFromBitmap(imageBitmap)
            }
        }
    }

    private fun showImgPreviewFromUri(imageUri: Uri) {
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        imgEvidence.setImageBitmap(bitmap)
        imgBitmap = bitmap
    }

    private fun showImgPreviewFromBitmap(bitmap: Bitmap) {
        imgEvidence.setImageBitmap(bitmap)
        imgBitmap = bitmap
    }

    private suspend fun uploadImage(): String? {
        if (!this::imgBitmap.isInitialized) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return null
        }

        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg")
        FileOutputStream(file).use {
            imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        return withContext(Dispatchers.IO) {
            val result = cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
            result["url"].toString()
        }
    }

    private fun validateFields(title: String, detail: String): Boolean {
        if (title.isEmpty() || detail.isEmpty()) {
            showIncorrectSignUpDialog("Asegúrate de llenar todos los campos")
            return false
        }
        if (imgEvidence.drawable == null || !this::imgBitmap.isInitialized) {
            showIncorrectSignUpDialog("Asegúrate de subir una imagen")
            return false
        }
        return true
    }

    private fun showIncorrectSignUpDialog(texto: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_incorrect_signup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.findViewById<TextView>(R.id.tvIncorrectSignup).text = texto
        dialog.findViewById<Button>(R.id.btnContinue).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showCorrectReportSaved() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_correct_report)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.findViewById<Button>(R.id.btnContinue).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this@NewReportActivity, MapActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
        dialog.show()
    }

    private fun navigationMenu() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    startActivity(Intent(this, MapActivity::class.java).putExtra("token", token))
                    true
                }
                R.id.nav_report -> {
                    startActivity(Intent(this, ListReportsActivity::class.java).putExtra("token", token))
                    true
                }
                R.id.nav_shared_location -> {
                    startActivity(Intent(this, ContactsListActivity::class.java).putExtra("token", token))
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.menu.findItem(R.id.nav_report).isChecked = true
    }
}
