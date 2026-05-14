package com.innovatech.peaceapp.StartingPoint

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.GlobalUserEmail
import com.innovatech.peaceapp.Map.MapActivity
import com.innovatech.peaceapp.Profile.Beans.UserEditSchema
import com.innovatech.peaceapp.Profile.Beans.UserPasswordSchema
import com.innovatech.peaceapp.Profile.Beans.UserProfile
import com.innovatech.peaceapp.Profile.Beans.UserProfileSchema
import com.innovatech.peaceapp.Profile.MainProfileActivity
import com.innovatech.peaceapp.Profile.Models.RetrofitClient
import com.innovatech.peaceapp.StartingPoint.Models.RetrofitClient as RetrofitClient_SP

import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuth
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuthenticated
import com.innovatech.peaceapp.StartingPoint.Beans.UserSchema
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var edtPassword_1: EditText
    private lateinit var ivEye_1: ImageView

    private lateinit var edtPassword_2: EditText
    private lateinit var ivEye_2: ImageView

    private lateinit var btnRecovery: Button

    private var passwordFieldSelected_1 = false
    private var passwordFieldSelected_2 = false

    private lateinit var token: String

    private lateinit var user: UserProfile
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        initComponents()
        highlightInputOnFocus()
        loadUserData()

        ivEye_1.setOnClickListener{
            changePasswordVisibility_1()
        }
        ivEye_2.setOnClickListener{
            changePasswordVisibility_2()
        }

        btnRecovery.setOnClickListener {
            if(validateSignUpFields())
                showConfirmationDialog()
        }

    }

    private fun initComponents() {
        edtPassword_1 = findViewById(R.id.et_password_1)
        ivEye_1 = findViewById(R.id.iv_eye_1)

        edtPassword_2 = findViewById(R.id.et_password_2)
        ivEye_2 = findViewById(R.id.iv_eye_2)

        btnRecovery = findViewById(R.id.btn_recovery)

        token = GlobalToken.token
        email = GlobalUserEmail.email
    }

    private fun showConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_edit)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        btnContinue.setOnClickListener() {
            lifecycleScope.launch {
                updateUser()
                val intent = Intent(this@ChangePasswordActivity, InitialActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
            }
            dialog.dismiss()
        }
        btnCancel.setOnClickListener() {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun loadUserData() {
        val service = RetrofitClient.getClient(token)

        service.getUserByEmail(email)
            .enqueue(
                object: Callback<UserProfile> {
                    override fun onResponse(call: Call<UserProfile>, response:
                    Response<UserProfile>
                    ) {
                        val userProfile = response.body()
                        if (userProfile != null) {

                            user = UserProfile(
                                userProfile.id,
                                userProfile.name,
                                userProfile.lastname,
                                userProfile.phonenumber,
                                userProfile.email,
                                userProfile.userId,
                                userProfile.profileImage
                            )
                        }
                    }

                    override fun onFailure(p0: Call<UserProfile>, p1: Throwable) {
                        p1.printStackTrace()
                    }
                })


    }

    private fun updateUser(){

        val service = RetrofitClient_SP.placeHolder
        service.changePassword(UserAuth(email,edtPassword_1.text.toString())).enqueue(object : Callback<UserAuthenticated> {
            override fun onResponse(p0: Call<UserAuthenticated>, response: Response<UserAuthenticated>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ChangePasswordActivity, "Guardando cambios", Toast.LENGTH_LONG).show()
                        saveUser(user.id, token)
                    }
                }

            override fun onFailure(p0: Call<UserAuthenticated>, p1: Throwable) {
                Toast.makeText(this@ChangePasswordActivity, "Error: ${p1.message}", Toast.LENGTH_LONG).show()
                Log.i("CHANGE_PASSWORD_ERROR", p1.message.toString())
            }
            })

    }

    private fun saveUser(user_id: Int, token: String){
        val serviceUserProfile = RetrofitClient.getClient(token)


        serviceUserProfile.changeUserPassword(user_id,
            UserPasswordSchema(edtPassword_1.text.toString())
        ).enqueue(object : Callback<UserProfile> {
            override fun onResponse(p0: Call<UserProfile>, response: Response<UserProfile>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ChangePasswordActivity, "Contraseña guardada correctamente", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(p0: Call<UserProfile>, p1: Throwable) {
                Toast.makeText(this@ChangePasswordActivity, "Error: ${p1.message}", Toast.LENGTH_LONG).show()
                Log.i("ErrorSavingPasswordIntoUser", p1.message.toString())
            }
        })
    }


    private fun highlightInputOnFocus() {
        edtPassword_1.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordFieldSelected_1 = true
                edtPassword_1.setBackgroundResource(R.drawable.auth_input_focused)
                edtPassword_1.setHintTextColor(resources.getColor(R.color.input_focused_stroke))
                ivEye_1.drawable.setTint(resources.getColor(R.color.input_focused_stroke))
            } else {
                passwordFieldSelected_1 = false
                edtPassword_1.setBackgroundResource(R.drawable.auth_input)
                edtPassword_1.setHintTextColor(resources.getColor(R.color.input_stroke))
                ivEye_1.drawable.setTint(resources.getColor(R.color.input_stroke))
            }
        }

        edtPassword_2.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordFieldSelected_2 = true
                edtPassword_2.setBackgroundResource(R.drawable.auth_input_focused)
                edtPassword_2.setHintTextColor(resources.getColor(R.color.input_focused_stroke))
                ivEye_2.drawable.setTint(resources.getColor(R.color.input_focused_stroke))
            } else {
                passwordFieldSelected_2 = false
                edtPassword_2.setBackgroundResource(R.drawable.auth_input)
                edtPassword_2.setHintTextColor(resources.getColor(R.color.input_stroke))
                ivEye_2.drawable.setTint(resources.getColor(R.color.input_stroke))
            }
        }

    }



    private fun changePasswordVisibility_1(){
        if(edtPassword_1.inputType == 129){
            edtPassword_1.inputType = 1
            // change the icon
            ivEye_1.setImageResource(R.drawable.ic_closed_eye)
        }else{
            edtPassword_1.inputType = 129
            ivEye_1.setImageResource(R.drawable.ic_open_eye)
        }
        if(passwordFieldSelected_1){
            ivEye_1.drawable.setTint(resources.getColor(R.color.input_focused_stroke))
        }else{
            ivEye_1.drawable.setTint(resources.getColor(R.color.input_stroke))
        }

    }

    private fun changePasswordVisibility_2(){
        if(edtPassword_2.inputType == 129){
            edtPassword_2.inputType = 1
            // change the icon
            ivEye_2.setImageResource(R.drawable.ic_closed_eye)
        }else{
            edtPassword_2.inputType = 129
            ivEye_2.setImageResource(R.drawable.ic_open_eye)
        }
        if(passwordFieldSelected_2){
            ivEye_2.drawable.setTint(resources.getColor(R.color.input_focused_stroke))
        }else{
            ivEye_2.drawable.setTint(resources.getColor(R.color.input_stroke))
        }
    }

    private fun validateSignUpFields(): Boolean {
        val password1 = edtPassword_1.text.toString()
        val password2 = edtPassword_2.text.toString()

        // Validación de fuerza de contraseña
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$")
        if (!passwordRegex.matches(password1)) {
            showIncorrectSignUpDialog("La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial.")
            return false
        }

        if (!passwordRegex.matches(password2)) {
            showIncorrectSignUpDialog("La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial.")
            return false
        }

        // Coincidencia
        if (password1 != password2) {
            showIncorrectSignUpDialog("Las contraseñas no coinciden.")
            return false
        }

        return true
    }


    private fun showIncorrectSignUpDialog(texto: String){
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_incorrect_signup)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)


        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val tvMensaje = dialog.findViewById<TextView>(R.id.tvIncorrectSignup)

        tvMensaje.text = texto

        btnContinue.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


}