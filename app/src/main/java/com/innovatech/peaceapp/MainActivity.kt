package com.innovatech.peaceapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.innovatech.peaceapp.Map.MapActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnPruebaProfile: Button = findViewById(R.id.btnPrueba)
        val btnMap: Button = findViewById(R.id.btnMap)

//        btnPruebaProfile.setOnClickListener {
//            val profileDialog = ProfileMainDialogFragment()
//            profileDialog.show(supportFragmentManager, "ProfileDialogFragment")
//        }

        btnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

    }
}