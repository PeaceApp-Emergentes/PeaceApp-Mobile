package com.innovatech.peaceapp.AI

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.innovatech.peaceapp.AI.Beans.ChatbotRequest
import com.innovatech.peaceapp.AI.Beans.ChatbotResponse
import com.innovatech.peaceapp.AI.Models.RetrofitClient
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.Map.ListReportsActivity
import com.innovatech.peaceapp.Map.MapActivity
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.ShareLocation.ContactsListActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatbotActivity : AppCompatActivity() {

    private val SUPPORT_WHATSAPP = "51987654321"
    private val SUPPORT_EMAIL = "soporte@peaceapp.pe"

    private lateinit var messagesContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var loading: ProgressBar
    private lateinit var chatScrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chatbot)

        messagesContainer = findViewById(R.id.messagesContainer)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        loading = findViewById(R.id.loadingIndicator)
        chatScrollView = findViewById(R.id.chatScrollView)

        addChatMessage(
            message = "Hola. Puedo ayudarte con orientacion de seguridad y reportes ciudadanos.",
            isUser = false
        )

        findViewById<View>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.supportButton).setOnClickListener {
            val msg = "Hola, necesito ayuda con PeaceApp."
            val url = "https://wa.me/$SUPPORT_WHATSAPP?text=" + android.net.Uri.encode(msg)
            try {
                startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
            } catch (e: Exception) {
                Toast.makeText(this, "Soporte PeaceApp: WhatsApp +$SUPPORT_WHATSAPP - $SUPPORT_EMAIL", Toast.LENGTH_LONG).show()
            }
        }

        navigationMenu()

        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(this, "Escribe un mensaje para consultar al asistente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addChatMessage(message = message, isUser = true)
            messageInput.text.clear()
            hideKeyboard()
            sendChatbotMessage(message)
        }
    }

    private fun sendChatbotMessage(message: String) {
        setLoading(true)

        RetrofitClient.placeHolder.chatbot(ChatbotRequest(message = message))
            .enqueue(object : Callback<ChatbotResponse> {
                override fun onResponse(call: Call<ChatbotResponse>, response: Response<ChatbotResponse>) {
                    setLoading(false)

                    if (!response.isSuccessful) {
                        addChatMessage(
                            message = "No pude procesar tu consulta. Intenta nuevamente.",
                            isUser = false
                        )
                        return
                    }

                    val body = response.body()
                    if (body == null) {
                        addChatMessage(
                            message = "No recibi respuesta del asistente.",
                            isUser = false
                        )
                        return
                    }

                    showAiResponse(body)
                }

                override fun onFailure(call: Call<ChatbotResponse>, t: Throwable) {
                    setLoading(false)
                    addChatMessage(
                        message = "No se pudo conectar con el servicio de IA. Verifica que el backend este levantado.",
                        isUser = false
                    )
                }
            })
    }

    private fun showAiResponse(response: ChatbotResponse) {
        val builder = StringBuilder(response.answer)

        if (!response.suggestedActions.isNullOrEmpty()) {
            builder.append("\n\nAcciones sugeridas:")
            response.suggestedActions.forEach { action ->
                builder.append("\n- ").append(action)
            }
        }

        addChatMessage(
            message = builder.toString(),
            isUser = false,
            isMock = response.mock
        )
    }

    private fun addChatMessage(message: String, isUser: Boolean, isMock: Boolean = false) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = if (isUser) Gravity.END else Gravity.START
        }

        val bubble = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = 28f
                setColor(getColor(if (isUser) R.color.peaceapp_blue else R.color.peaceapp_gray))
            }
            setPadding(24, 18, 24, 18)
        }

        val maxBubbleContentWidth = (resources.displayMetrics.widthPixels * 0.68f).toInt()
        val messageView = TextView(this).apply {
            text = message
            textSize = 15f
            maxWidth = maxBubbleContentWidth
            setTextColor(getColor(if (isUser) R.color.white else R.color.black))
            setLineSpacing(0f, 1.08f)
        }
        bubble.addView(messageView)

        if (isMock) {
            val mockView = TextView(this).apply {
                text = "Respuesta simulada"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                maxWidth = maxBubbleContentWidth
                setTextColor(getColor(R.color.peaceapp_blue))
                setPadding(0, 10, 0, 0)
            }
            bubble.addView(mockView)
        }

        row.addView(
            bubble,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        messagesContainer.addView(
            row,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        )

        chatScrollView.post {
            chatScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun navigationMenu() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_ai
        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_ai) return@setOnItemSelectedListener true

            when (item.itemId) {
                R.id.nav_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                R.id.nav_report -> {
                    startActivity(
                        Intent(this, ListReportsActivity::class.java)
                            .putExtra("token", GlobalToken.token)
                    )
                    true
                }
                R.id.nav_shared_location -> {
                    startActivity(Intent(this, ContactsListActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        loading.visibility = if (isLoading) View.VISIBLE else View.GONE
        sendButton.isEnabled = !isLoading
        messageInput.isEnabled = !isLoading
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(messageInput.windowToken, 0)
    }
}
