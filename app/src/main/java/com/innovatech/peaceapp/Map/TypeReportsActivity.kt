package com.innovatech.peaceapp.Map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.innovatech.peaceapp.R

class TypeReportsActivity : AppCompatActivity() {
    lateinit var token : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_type_reports)
        token = intent.getStringExtra("token")!!

        val btnRobo = findViewById<ImageView>(R.id.btnRobo)
        val btnAccidente = findViewById<ImageView>(R.id.btnAccidente)
        val btnFaltaIluminacion = findViewById<ImageView>(R.id.btnFaltaIluminacion)
        val btnAcoso = findViewById<ImageView>(R.id.btnAcoso)
        val btnOtro = findViewById<ImageView>(R.id.btnOtro)

        btnRobo.setOnClickListener {
            val intent = Intent(this, NewReportActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("type", "Robo")
            startActivity(intent)
        }

        btnAccidente.setOnClickListener {
            val intent = Intent(this, NewReportActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("type", "Accidente")
            startActivity(intent)
        }

        btnFaltaIluminacion.setOnClickListener {
            val intent = Intent(this, NewReportActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("type", "Falta de iluminación")
            startActivity(intent)
        }

        btnAcoso.setOnClickListener {
            val intent = Intent(this, NewReportActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("type", "Acoso")
            startActivity(intent)
        }

        btnOtro.setOnClickListener {
            val intent = Intent(this, NewReportActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("type", "Otro")
            startActivity(intent)
        }

    }
}