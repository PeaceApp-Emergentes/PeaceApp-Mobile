package com.innovatech.peaceapp.StartingPoint

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.GlobalUserEmail
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuth
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuthenticated
import com.innovatech.peaceapp.StartingPoint.Models.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var btnAcceptRecovery: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etEmail = findViewById<EditText>(R.id.et_email)
        btnAcceptRecovery = findViewById(R.id.btn_accept_recovery)
        password = "owo" // valor dummy solo para autenticación

        btnAcceptRecovery.setOnClickListener {
            email = etEmail.text.toString()
            if (email.isNotEmpty()) {
                val service = RetrofitClient.placeHolder
                val user = UserAuth(email, password)

                service.signIn(user).enqueue(object : Callback<UserAuthenticated> {
                    override fun onResponse(call: Call<UserAuthenticated>, response: Response<UserAuthenticated>) {
                        if (response.isSuccessful) {
                            val user = response.body()
                            if (user?.username != null) {
                                val token = user.token

                                val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putInt("userId", user.id)
                                    apply()
                                }

                                GlobalToken.setToken(token)
                                GlobalUserEmail.setEmail(email)

                                navigateToChangePasswordActivity(token)
                            } else {
                                showIncorrectSignInDialog("Correo no registrado")
                            }
                        } else {
                            showIncorrectSignInDialog("Correo no registrado")
                        }
                    }

                    override fun onFailure(call: Call<UserAuthenticated>, t: Throwable) {
                        Toast.makeText(this@ForgotPasswordActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                        Log.e("ERROR", t.message.toString())
                    }
                })
            } else {
                Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToChangePasswordActivity(token: String) {
        val intent = Intent(this, ChangePasswordActivity::class.java)
        intent.putExtra("token", token)
        startActivity(intent)
    }

    private fun showIncorrectSignInDialog(mensaje: String) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_incorrect_signup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val tvMensaje = dialog.findViewById<TextView>(R.id.tvIncorrectSignup)
        tvMensaje.text = mensaje

        btnContinue.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
