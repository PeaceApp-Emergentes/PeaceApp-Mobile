package com.innovatech.peaceapp.StartingPoint

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.GlobalUserEmail
import com.innovatech.peaceapp.Map.MapActivity
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.StartingPoint.Beans.User
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuth
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuthenticated
import com.innovatech.peaceapp.StartingPoint.Models.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    private lateinit var btnSignIn: Button
    private lateinit var btnSignUp: TextView

    private lateinit var edtEmail: TextView
    private lateinit var edtPassword: TextView

    private lateinit var tvForgotPassword: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initComponents()
        initListeners()
        highlightInputOnFocus()

        // para subrayar el texto
        val texto = tvForgotPassword.text.toString()
        val spanableText = SpannableString(texto)
        spanableText.setSpan(UnderlineSpan(), 0, texto.length, 0)

        tvForgotPassword.text = spanableText
    }

    private fun highlightInputOnFocus() {
        edtEmail.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                edtEmail.setBackgroundResource(R.drawable.auth_input_focused)
                edtEmail.setHintTextColor(resources.getColor(R.color.input_focused_stroke))
            }else{
                edtEmail.setBackgroundResource(R.drawable.auth_input)
                edtEmail.setHintTextColor(resources.getColor(R.color.input_stroke))
            }
        }

        edtPassword.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                edtPassword.setBackgroundResource(R.drawable.auth_input_focused)
                edtPassword.setHintTextColor(resources.getColor(R.color.input_focused_stroke))
            }else{
                edtPassword.setBackgroundResource(R.drawable.auth_input)
                edtPassword.setHintTextColor(resources.getColor(R.color.input_stroke))
            }
        }
    }

    private fun initComponents() {
        btnSignIn = findViewById<Button>(R.id.btn_signin)
        btnSignUp = findViewById<TextView>(R.id.tv_create_account)
        edtEmail = findViewById<TextView>(R.id.et_email)
        edtPassword = findViewById<TextView>(R.id.et_password)
        tvForgotPassword = findViewById<TextView>(R.id.tv_forgot_password)
    }

    private fun initListeners() {
        btnSignIn.setOnClickListener {
            if(validateSignUpFields())
                signIn(edtEmail.text.toString(), edtPassword.text.toString())
        }

        btnSignUp.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
    private fun signIn(email:String, password:String){
        val service = RetrofitClient.placeHolder

        val user = UserAuth(email, password)
        service.signIn(user).enqueue(object : Callback<UserAuthenticated> {
            override fun onResponse(p0: Call<UserAuthenticated>, response: Response<UserAuthenticated>) {
                if(response.isSuccessful){
                    val user = response.body()
                    if(user?.username != null){
                        // La app móvil es exclusiva para ciudadanos: bloquear cuentas municipales.
                        if (user.role?.uppercase()?.contains("MUNICIPALITY") == true) {
                            showIncorrectSignInDialog("Esta cuenta es de una municipalidad. " +
                                    "Ingresa desde la aplicación web de PeaceApp.")
                            return
                        }
                        val intent = Intent(this@SignInActivity, MapActivity::class.java)
                        val token = user.token

                        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("userId", user.id)
                            putString("userRole", user.role)
                            putString("authToken", token)
                            putString("userEmail", email)
                            apply()
                        }

                        GlobalToken.setToken(token)
                        GlobalUserEmail.setEmail(email)

                        intent.putExtra("token", token)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }else {
                        Log.e("Mensaje", user!!.message)
                        // mostrar el dialog de error
                        when(user.message){
                            "User not found" -> showIncorrectSignInDialog("No existe un usuario " +
                                    "con este correo")
                            "Invalid password" -> showIncorrectSignInDialog("La contraseña es " +
                                    "incorrecta")
                            else -> showIncorrectSignInDialog("Error desconocido")
                        }

                    }
                } else {
                    showIncorrectSignInDialog("Correo o contraseña incorrectos.")
                }
            }
            override fun onFailure(p0: Call<UserAuthenticated>, p1: Throwable) {
                Toast.makeText(this@SignInActivity, "Error: ${p1.message}", Toast.LENGTH_LONG).show()
                Log.e("ERROR", p1.message.toString())
            }
        })
    }
    private fun showIncorrectSignInDialog(mensaje: String){
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_incorrect_signup)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val tvMensaje = dialog.findViewById<TextView>(R.id.tvIncorrectSignup)

        tvMensaje.text = mensaje

        btnContinue.setOnClickListener {
            dialog.hide()
        }

        dialog.show()
    }

    private fun validateSignUpFields():Boolean{
        if(edtEmail.text.isEmpty() || edtPassword.text.isEmpty()){
            showIncorrectSignInDialog("Asegúrate de llenar todos los campos")
            return false
        }
        return true
    }

}
