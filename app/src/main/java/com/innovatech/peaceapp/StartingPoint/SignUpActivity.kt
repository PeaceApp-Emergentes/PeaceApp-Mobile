package com.innovatech.peaceapp.StartingPoint

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.GlobalUserEmail
import com.innovatech.peaceapp.Map.MapActivity
import com.innovatech.peaceapp.Profile.Beans.UserProfile
import com.innovatech.peaceapp.Profile.Beans.UserProfileSchema
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.StartingPoint.Beans.User
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuth
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuthenticated
import com.innovatech.peaceapp.StartingPoint.Beans.UserSchema
import com.innovatech.peaceapp.StartingPoint.Models.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var etName:EditText
    private lateinit var etLastName:EditText
    private lateinit var etPhone:EditText
    private lateinit var btnSignUp:Button
    private lateinit var btnSignIn:TextView
    private lateinit var edtEmail:EditText
    private lateinit var edtPassword:EditText

    private lateinit var ivEye:ImageView
    private lateinit var tvCountryCode:TextView

    private var passwordFieldSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initComponents()
        highlightInputOnFocus()


        btnSignUp.setOnClickListener {
            if(validateSignUpFields())
                saveUserCredentials()
        }

        btnSignIn.setOnClickListener{
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        ivEye.setOnClickListener{
            changePasswordVisibility()
        }

    }

    private fun initComponents() {
        etName = findViewById(R.id.et_name)
        etLastName = findViewById(R.id.et_surname)
        etPhone = findViewById(R.id.et_phone)
        btnSignUp = findViewById(R.id.btn_signup)
        btnSignIn = findViewById(R.id.tv_log_in)
        edtEmail = findViewById(R.id.et_email)
        edtPassword = findViewById(R.id.et_password)
        ivEye = findViewById(R.id.iv_eye)
        tvCountryCode = findViewById(R.id.tv_country_code)
    }

    private fun saveUserCredentials(){
        val service = RetrofitClient.placeHolder


        val user = UserSchema(
            edtEmail.text.toString(),
            edtPassword.text.toString())

        service.signUp(user).enqueue(object : Callback<User> {
            override fun onResponse(p0: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user?.username != null) {
                        authenticateUser()
                    }
                }
            }

            override fun onFailure(p0: Call<User>, p1: Throwable) {
                Toast.makeText(this@SignUpActivity, "Error: ${p1.message}", Toast.LENGTH_LONG).show()
                Log.i("SIGNUPERROR1", p1.message.toString())
            }
        })
    }

    private fun authenticateUser(){
        val service = RetrofitClient.placeHolder

        service.signIn(UserAuth(edtEmail.text.toString(), edtPassword.text.toString())).enqueue(object : Callback<UserAuthenticated> {
            override fun onResponse(p0: Call<UserAuthenticated>, response: Response<UserAuthenticated>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user?.username != null) {
                        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("userId", user.id)
                            putString("userRole",user.role)
                            apply()
                        }


                        GlobalToken.setToken(user.token)
                        GlobalUserEmail.setEmail(user.username)

                        saveUser(user.id, user.token)
                    }
                }
            }

            override fun onFailure(p0: Call<UserAuthenticated>, p1: Throwable) {
                Toast.makeText(this@SignUpActivity, "Error: ${p1.message}", Toast.LENGTH_LONG).show()
                Log.i("SIGNUPERROR3", p1.message.toString())
            }
        })
    }

    private fun saveUser(user_id: Int, token: String){
        val serviceUserProfile = com.innovatech.peaceapp.Profile.Models.RetrofitClient.getClient(token)

        val userProfile = UserProfileSchema(
            etName.text.toString(),
            etLastName.text.toString(),
            etPhone.text.toString(),
            edtEmail.text.toString(),
            user_id.toString(),
            "https://static.vecteezy.com/system/resources/previews/009/292/244/non_2x/default" +
                    "-avatar-icon-of-social-media-user-vector.jpg", // default user image
            )

        serviceUserProfile.createUser(userProfile).enqueue(object : Callback<UserProfile> {
            override fun onResponse(p0: Call<UserProfile>, response: Response<UserProfile>) {
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    if (userProfile?.name != null) {


                        Log.i("UserInSignUp", userProfile.id.toString()+ " "+userProfile.name + " "
                                + userProfile.lastname + " " + userProfile.email + " " + userProfile.phonenumber + " " +
                                 userProfile.userId + " " + userProfile.profileImage)


                        showCorrectSignUpDialog(token)
                    }
                }
            }

            override fun onFailure(p0: Call<UserProfile>, p1: Throwable) {
                Toast.makeText(this@SignUpActivity, "Error: ${p1.message}", Toast.LENGTH_LONG).show()
                Log.i("ErrorSIGNUPPPPPPP2", p1.message.toString())
            }
        })
    }

    private fun navigateToMapActivity(token: String){
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra("token", token)
        startActivity(intent)
    }

    private fun showCorrectSignUpDialog(token: String){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_correct_signup)
        // to set a transparent background
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)

        btnContinue.setOnClickListener {
            dialog.hide()
            navigateToMapActivity(token)
        }

        dialog.show()
    }

    private fun validateSignUpFields(): Boolean {
        val password = edtPassword.text.toString()
        val phone = etPhone.text.toString()
        val email = edtEmail.text.toString()

        if (etName.text.isEmpty() || etLastName.text.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showIncorrectSignUpDialog("Please fill out all fields.")
            return false
        }

        // Email validation
        val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$")
        if (!emailRegex.matches(email)) {
            showIncorrectSignUpDialog("Invalid email format.")
            return false
        }

        // Phone number must be exactly 9 digits
        val phoneRegex = Regex("^\\d{9}\$")
        if (!phoneRegex.matches(phone)) {
            showIncorrectSignUpDialog("Phone number must contain exactly 9 digits.")
            return false
        }

        // Password strength validation
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}\$")
        if (!passwordRegex.matches(password)) {
            showIncorrectSignUpDialog("Password must be at least 8 characters long and include uppercase, lowercase, digit, and special character.")
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

    private fun highlightInputOnFocus() {
        etName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etName.setBackgroundResource(R.drawable.auth_input_focused)
                etName.setHintTextColor(resources.getColor(R.color.input_focused_stroke))
            } else {
                etName.setBackgroundResource(R.drawable.auth_input)
                etName.setHintTextColor(resources.getColor(R.color.input_stroke))
            }
        }
        etLastName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etLastName.setBackgroundResource(R.drawable.auth_input_focused)
                etLastName.setHintTextColor(resources.getColor(R.color.input_focused_stroke))
            } else {
                etLastName.setBackgroundResource(R.drawable.auth_input)
                etLastName.setHintTextColor(resources.getColor(R.color.input_stroke))
            }
        }
        etPhone.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etPhone.setBackgroundResource(R.drawable.auth_input_focused)
                etPhone.setHintTextColor(resources.getColor(R.color.input_focused_stroke))
                tvCountryCode.setTextColor(resources.getColor(R.color.input_focused_stroke))
            } else {
                etPhone.setBackgroundResource(R.drawable.auth_input)
                etPhone.setHintTextColor(resources.getColor(R.color.input_stroke))
                tvCountryCode.setTextColor(resources.getColor(R.color.input_stroke))
            }
        }
        edtEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                edtEmail.setBackgroundResource(R.drawable.auth_input_focused)
                edtEmail.setHintTextColor(resources.getColor(R.color.input_focused_stroke))
            } else {
                edtEmail.setBackgroundResource(R.drawable.auth_input)
                edtEmail.setHintTextColor(resources.getColor(R.color.input_stroke))
            }
        }
        edtPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordFieldSelected = true
                edtPassword.setBackgroundResource(R.drawable.auth_input_focused)
                edtPassword.setHintTextColor(resources.getColor(R.color.input_focused_stroke))
                ivEye.drawable.setTint(resources.getColor(R.color.input_focused_stroke))
            } else {
                passwordFieldSelected = false
                edtPassword.setBackgroundResource(R.drawable.auth_input)
                edtPassword.setHintTextColor(resources.getColor(R.color.input_stroke))
                ivEye.drawable.setTint(resources.getColor(R.color.input_stroke))
            }
        }

    }

    private fun changePasswordVisibility(){
        if(edtPassword.inputType == 129){
            edtPassword.inputType = 1
            // change the icon
            ivEye.setImageResource(R.drawable.ic_closed_eye)
        }else{
            edtPassword.inputType = 129
            ivEye.setImageResource(R.drawable.ic_open_eye)
        }
        if(passwordFieldSelected){
            ivEye.drawable.setTint(resources.getColor(R.color.input_focused_stroke))
        }else{
            ivEye.drawable.setTint(resources.getColor(R.color.input_stroke))
        }
    }

}