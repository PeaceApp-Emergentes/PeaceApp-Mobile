package com.innovatech.peaceapp.ShareLocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.innovatech.peaceapp.DB.AppDatabase
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.Map.ListReportsActivity
import com.innovatech.peaceapp.Map.MapActivity
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.ShareLocation.Beans.Contact
import com.innovatech.peaceapp.ShareLocation.Entity.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsListActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var btnSendLocation: Button
    private lateinit var btnSendLocationFavorites: Button
    private lateinit var btnAddToFavorites: Button
    private lateinit var btnShowFavorites: CardView
    private lateinit var ivHeart: ImageView
    private lateinit var rvContacts: RecyclerView
    private lateinit var contactAdapter: Adapter
    private lateinit var contactAdapterFavorites: AdapterFavorites
    private lateinit var tvSubtitle: TextView
    private lateinit var llButtons: LinearLayout
    private lateinit var btnBack: ImageView
    private var selectedContacts = mutableListOf<Contact>()
    private var listFavorites = mutableListOf<ContactEntity>()
    private var latitude = ""
    private var longitude = ""
    private lateinit var token: String
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts_list)
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        latitude = sharedPref.getString("latitude", "0.0")!!.toString()
        longitude = sharedPref.getString("longitude", "0.0")!!.toString()
        token = GlobalToken.token

        initComponents()
        askForPermissions()
        checkSelectedContacts()

        appDatabase = AppDatabase.getDatabase(this)

        val contactList = loadContacts()
        contactAdapter = Adapter(contactList) { contact, isSelected ->
            if (isSelected) {
                selectedContacts.add(contact)
                checkSelectedContacts()
            } else {
                selectedContacts.remove(contact)
                checkSelectedContacts()
            }
        }
        rvContacts.layoutManager = LinearLayoutManager(applicationContext)
        rvContacts.adapter = contactAdapter

        btnBack.setOnClickListener {
            allContactsSelected()
        }

        btnShowFavorites.setOnClickListener {
            favoritesSelected()
        }
        btnSendLocationFavorites.setOnClickListener{
            sendLocationToSelectedContacts(true)
        }


        btnSendLocation.setOnClickListener {
            if(selectedContacts.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un contacto para continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendLocationToSelectedContacts()
        }

        btnAddToFavorites.setOnClickListener {
            Toast.makeText(this, "Contactos agregados a favoritos", Toast.LENGTH_SHORT).show()
            favoritesSelected()
        }

        editTextListeners(contactList)
        navigationMenu()
    }

    private fun favoritesSelected() {

        selectedContacts.forEach { contact ->
            var contactEntity = ContactEntity(null, contact.name, contact.phone, contact.image)
            GlobalScope.launch(Dispatchers.IO){
                appDatabase.contactDAO().insert(contactEntity)

                val updatedFavorites = appDatabase.contactDAO().getAll()
                withContext(Dispatchers.Main){
                    listFavorites.clear()
                    listFavorites.addAll(updatedFavorites)
                    contactAdapterFavorites.updateFavorites(listFavorites)
                }
            }
        }



        contactAdapterFavorites = AdapterFavorites(listFavorites) { contact ->
            GlobalScope.launch(Dispatchers.IO){
                appDatabase.contactDAO().delete(contact)

                val updatedFavorites = appDatabase.contactDAO().getAll()
                withContext(Dispatchers.Main){
                    listFavorites.clear()
                    listFavorites.addAll(updatedFavorites)
                    contactAdapterFavorites.updateFavorites(listFavorites)
                }
            }
        }


        rvContacts.layoutManager = LinearLayoutManager(applicationContext)
        rvContacts.adapter = contactAdapterFavorites

        GlobalScope.launch(Dispatchers.IO){
            listFavorites.addAll(appDatabase.contactDAO().getAll())
        }

        ivHeart.setImageResource(R.drawable.ic_heart_filled)
        tvSubtitle.text = "Administra tus contactos favoritos para compartir tu ubicación"
        if(listFavorites.isEmpty()){
            btnSendLocationFavorites.visibility = Button.GONE
        }else{
            btnSendLocationFavorites.visibility = Button.VISIBLE
        }
        btnBack.visibility = Button.VISIBLE


        llButtons.visibility = LinearLayout.GONE


    }
    private fun sendWhatsAppMessage(phone: String, message: String) {
        val formattedPhone = phone.replace("[^\\d+]".toRegex(), "") // limpia el número (sin espacios, guiones, +, etc.)
        val uri = Uri.parse("https://wa.me/$formattedPhone?text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.whatsapp")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
        }
    }



    private fun allContactsSelected() {

        val contactList = loadContacts()
        contactAdapter = Adapter(contactList) { contact, isSelected ->
            if (isSelected) {
                selectedContacts.add(contact)
                checkSelectedContacts()
            } else {
                selectedContacts.remove(contact)
                checkSelectedContacts()
            }
        }

        rvContacts.layoutManager = LinearLayoutManager(applicationContext)
        rvContacts.adapter = contactAdapter

        checkSelectedContacts()


        ivHeart.setImageResource(R.drawable.ic_heart_outline)
        tvSubtitle.text = "Elige los contactos con los que compartirás tu ubicación"

        btnSendLocationFavorites.visibility = Button.GONE
        btnBack.visibility = Button.GONE


    }

    private fun checkSelectedContacts() {
        if (selectedContacts.isEmpty()) {
            llButtons.visibility = LinearLayout.GONE
        }else {
            llButtons.visibility = LinearLayout.VISIBLE
        }
    }

    //async function to wait for permissions
    private fun askForPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_CONTACTS)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1002)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), 1001)
        }
    }


    private fun initComponents() {
        etSearch = findViewById(R.id.etSearch)
        btnSendLocation = findViewById(R.id.btnSendLocation)
        btnAddToFavorites = findViewById(R.id.btnAddToFavorites)
        rvContacts = findViewById(R.id.rv_contacts)
        llButtons = findViewById(R.id.ll_buttons)
        btnShowFavorites = findViewById(R.id.cvFavorites)
        ivHeart = findViewById(R.id.ivHeart)
        tvSubtitle = findViewById(R.id.tv_subtitle)
        btnSendLocationFavorites = findViewById(R.id.btnSendLocationFavorites)
        btnBack = findViewById(R.id.btnBack)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario aceptó el permiso → intenta nuevamente enviar SMS
                sendLocationToSelectedContacts()
            } else {
                Toast.makeText(this, "No se otorgó permiso para enviar SMS", Toast.LENGTH_SHORT).show()
            }
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
                    val intent = Intent(this, ContactsListActivity::class.java)
                    intent.putExtra("token", token)
                    startActivity(intent)
                    true
                }
                else -> false
            }

        }

        bottomNavigationView.menu.findItem(R.id.nav_shared_location).setChecked(true)
    }

    private fun sendLocationToSelectedContacts(isFavorites: Boolean = false) {
        val locationMessage = "¡Hola! Esta es mi ubicación en PeaceApp: http://maps.google.com/?q=$latitude,$longitude"

        if (selectedContacts.isEmpty() && !isFavorites) {
            Toast.makeText(this, "Selecciona al menos un contacto para continuar", Toast.LENGTH_SHORT).show()
            return
        }

        val options = arrayOf("Enviar por WhatsApp", "Enviar por SMS")

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("¿Cómo deseas compartir la ubicación?")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> { // WhatsApp
                    val contacts = if (isFavorites) listFavorites.map { Contact(it.name, it.phone ?: "", it.image ?: "") } else selectedContacts
                    contacts.forEach { contact ->
                        sendWhatsAppMessage(contact.phone, locationMessage)
                    }
                }
                1 -> { // SMS
                    // Verifica permiso antes de enviar
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1002)
                    } else {
                        val smsManager = SmsManager.getDefault()

                        if (isFavorites) {
                            listFavorites.forEach { contact ->
                                contact.phone?.let { phone ->
                                    smsManager.sendTextMessage(phone, null, locationMessage, null, null)
                                }
                            }
                        } else {
                            selectedContacts.forEach { contact ->
                                smsManager.sendTextMessage(contact.phone, null, locationMessage, null, null)
                            }
                        }

                        Toast.makeText(this, "Ubicación enviada por SMS", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.show()
    }



    @SuppressLint("Range")
    private fun loadContacts(): List<Contact> {
        val contactList = mutableListOf<Contact>()
        val seenContacts = mutableSetOf<String>()  // Set para rastrear nombres/números únicos

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                // Verifica si ya hemos visto este contacto antes (por nombre o número)
                val uniqueIdentifier = "$name-$phoneNumber"
                if (uniqueIdentifier in seenContacts) continue  // Salta los duplicados

                seenContacts.add(uniqueIdentifier)

                val image = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                    ?: "https://picsum.photos/200/200?random=${contactList.size}"

                val contact = Contact(name, phoneNumber, image)
                contactList.add(contact)
            }
        }

        return contactList
    }




    private fun editTextListeners(contactList: List<Contact>, isFavorites: Boolean = false) {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFavorites) {
                    filterContacts(contactList, s.toString())
                }else{
                filterContacts(contactList, s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) { }

        })
    }

    private fun filterContacts(contactList: List<Contact> = emptyList()  , search: String, isFavorites:
    Boolean = false) {
        if (isFavorites) {
            val filteredList = listFavorites.filter {
                it.name.contains(search, ignoreCase = true) || it.phone?.contains(search) ?: false
            }

            contactAdapterFavorites.updateFavorites(filteredList)
            return
        }
        val filteredList = contactList.filter {
            it.name.contains(search, ignoreCase = true) || it.phone.contains(search)
        }

        contactAdapter.updateContacts(filteredList)
    }




    private fun generateRandomString() : String {
        val length = 6
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    private fun showGeneratedUrlDialog(url:String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_generated_url)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val btnCopy = dialog.findViewById<CardView>(R.id.cvCopyContainer)
        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val tvUrlGenerada = dialog.findViewById<TextView>(R.id.tvGeneratedUrl)

        tvUrlGenerada.text = "http://www.peaceapp.com/$url"

        btnContinue.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


}