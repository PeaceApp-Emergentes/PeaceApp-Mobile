package com.innovatech.peaceapp.StartingPoint

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.ShareLocation.ContactsListActivity

class InitialActivity : AppCompatActivity() {

    private lateinit var animationText: TextView
    private var disableBackButton = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_initial)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        animationText = findViewById(R.id.tv_subtitle)

        val fadeOut = ObjectAnimator.ofFloat(animationText, "alpha", 1f, 0f).apply {
            duration = 500
        }
        val fadeIn = ObjectAnimator.ofFloat(animationText, "alpha", 0f, 1f).apply {
            duration = 500
        }

        val textArray = arrayOf("Conecta con tus seres queridos", "Comparte tu ubicación con amigos", "Viaja en paz con PeaceApp")
        var i = 0

        val handler = android.os.Handler()

        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                animationText.text = textArray[i]
                i++
                if (i == textArray.size) {
                    i = 0
                }
                fadeIn.start()
            }
        })

        val runnable = object : Runnable {
            override fun run() {
                fadeOut.start()
                handler.postDelayed(this, 2200)
            }
        }
        handler.post(runnable)



        val btnSignUp = findViewById<Button>(R.id.btn_start_now)
        val btnSignIn = findViewById<MaterialButton>(R.id.tv_login)

        btnSignUp.setOnClickListener {
            //val intent = Intent(this, ContactsListActivity::class.java)
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        btnSignIn.setOnClickListener{
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }



    }
    override fun onBackPressed() {
        if(disableBackButton){

        }else{
            super.onBackPressed()
        }
    }
    private fun textAnimation() {

    }
}