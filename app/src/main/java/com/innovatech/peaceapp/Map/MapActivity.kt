package com.innovatech.peaceapp.Map

import Beans.Location
import ReportSchema
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.innovatech.peaceapp.Alert.AlertActivity
import com.innovatech.peaceapp.AI.ChatbotActivity
import com.innovatech.peaceapp.Alert.Beans.Alert
import com.innovatech.peaceapp.Alert.Beans.AlertSchema
import com.innovatech.peaceapp.DB.AppDatabase
import com.innovatech.peaceapp.DB.Entities.LocationModel
import com.innovatech.peaceapp.GlobalToken
import com.innovatech.peaceapp.GlobalUserEmail
import com.innovatech.peaceapp.Map.Adapters.AdapterLocationRecent
import com.innovatech.peaceapp.Map.Beans.MunicipalityProfile
import com.innovatech.peaceapp.Map.Beans.PropertiesPlace
import com.innovatech.peaceapp.Map.Beans.Report
import com.innovatech.peaceapp.Map.Models.RetrofitClient
import com.innovatech.peaceapp.Map.Models.RetrofitClientMapbox
import com.innovatech.peaceapp.Profile.Beans.UserProfile
import com.innovatech.peaceapp.Profile.MainProfileActivity
import com.innovatech.peaceapp.R
import com.innovatech.peaceapp.ShareLocation.ContactsListActivity
import com.innovatech.peaceapp.StartingPoint.GlobalRecentLocation
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.search.autofill.AddressAutofill
import com.mapbox.search.autofill.AddressAutofillResult
import com.mapbox.search.autofill.AddressAutofillSuggestion
import com.mapbox.search.autofill.Query
import com.mapbox.search.ui.adapter.autofill.AddressAutofillUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var token: String
    private lateinit var txtCurrentLocation: TextView
    private lateinit var addressAutofill: AddressAutofill
    private lateinit var searchLocation: EditText
    private lateinit var searchResultsView: SearchResultsView
    private lateinit var addressAutofillUiAdapter: AddressAutofillUiAdapter
    private var ignoreNextQueryTextUpdate: Boolean = false
    private lateinit var mapPin: View
    private var coordinatesCurrentLocation: Point = Point.fromLngLat(0.0, 0.0)
    private var currentLocation: String = ""
    private var ignoreNextMapIdleEvent: Boolean = false
    private var isUserInteracting: Boolean = false
    private lateinit var userPhoto: ImageView
    private var c: Int = 0
    private var expandArrow: ImageView? = null
    private var compressedArrow: ImageView? = null
    private var isKeyboardVisible = false
    private var isExpanded = false
    private lateinit var searchBox: CardView
    private val processedReports = mutableSetOf<Int>() // This will store the ID of the reports that have already triggered an alert.
    private val popupTriggeredReports = mutableSetOf<Int>() // Tracks reports that have triggered a popup
    private var userId: Int = 0
    private lateinit var email: String
    private lateinit var btnNewReport: LinearLayout
    private var lastRecentLongitude: Double = 0.0
    private var lastRecentLatitude: Double = 0.0
    private var realUserLocation: Point? = null
    private var realUserLocationName: String = ""

    private lateinit var handler: Handler
    private val proximityCheckRunnable = object : Runnable {
        override fun run() {
            val currentLat = coordinatesCurrentLocation.latitude()
            val currentLon = coordinatesCurrentLocation.longitude()

            // Llama a checkProximityToReports con las coordenadas actuales
            checkProximityToReports(currentLat, currentLon)

            // Repite el chequeo después de un intervalo de tiempo (por ejemplo, 5000 ms = 5 segundos)
            handler.postDelayed(this, 1000)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        handler = Handler()
        handler.post(proximityCheckRunnable)

        // Permiso de notificaciones (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001
            )
        }
        // Example function that updates the alert counter
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)
        var tk = GlobalToken
        Log.i("Token", tk.token.toString())
// In MapActivity - pass the current location to AlertActivity when the alert button is pressed
        val warningButton = findViewById<ConstraintLayout>(R.id.iconButton)
        warningButton.setOnClickListener {

            // Start the AlertActivity as before
            val intent = Intent(this, AlertActivity::class.java)
            intent.putExtra("currentLocation", currentLocation) // Pass the current location
            startActivity(intent)
        }


        addressAutofill = AddressAutofill.create(locationProvider = null)
        searchLocation = findViewById(R.id.searchLocation)
        searchResultsView = findViewById(R.id.search_results_view)
        mapPin = findViewById(R.id.map_pin)
        txtCurrentLocation = findViewById(R.id.currentLocation)
        token = intent.getStringExtra("token")!!
        mapView = findViewById(R.id.mapView)
        userPhoto = findViewById(R.id.userPhoto)
        searchBox = findViewById(R.id.container_search);
        expandArrow = findViewById(R.id.expand_arrow);
        compressedArrow = findViewById(R.id.compressed_arrow);
        btnNewReport = findViewById(R.id.ll_create_new_report);
        val btnSosEmergency = findViewById<ConstraintLayout>(R.id.btnSosEmergency)
        email = GlobalUserEmail.email
        userPhoto.setOnClickListener {
            val intent = Intent(this, MainProfileActivity::class.java)
            startActivity(intent)

        }


        searchResultsView.initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
            )
        )

        addressAutofillUiAdapter = AddressAutofillUiAdapter(view = searchResultsView, addressAutofill = addressAutofill)
        addressAutofillUiAdapter.addSearchListener(object :
            AddressAutofillUiAdapter.SearchListener {
            override fun onSuggestionSelected(suggestion: AddressAutofillSuggestion) {
                /* when is selected a suggestion, the address is shown in the search bar */

                // ALERTA: Buscador con el autocompletado
                // no descomentar, costo adicional
                selectSuggestion(suggestion)
            }
            override fun onSuggestionsShown(suggestions: List<AddressAutofillSuggestion>) {}
            override fun onError(e: Exception) {}
        })

        /* when is received a click on the search bar, the search results view is shown */
        searchLocation.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (ignoreNextQueryTextUpdate) {
                    ignoreNextQueryTextUpdate = false
                    return
                }

                val query = Query.create(text.toString())
                if (query != null) {
                    lifecycleScope.launchWhenStarted {
                        // ALERTA: AddressAutoFill
                        //Log.i("AddressAutofill SEARCH QUERY", "Searching for: $query")
                        addressAutofillUiAdapter.search(query) // this function is used to search the address
                    }
                }
                searchResultsView.isVisible = query != null
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Nothing to do
            }

            override fun afterTextChanged(s: Editable) {
                // Nothing to do
            }
        })

        // event: when the map is moved, flag for user interaction is set to true
        mapView.mapboxMap.addOnCameraChangeListener {
            isUserInteracting = true
        }
        // event: when the map is idle, the center of the map is obtained
        mapView.mapboxMap.addOnMapIdleListener {
            moveToRecentLocation()
            if (!isUserInteracting) { // if the map is being moved by the user, the center is not obtained
                return@addOnMapIdleListener
            }

            // the map is stopped and obtain the center
            val center = mapView.mapboxMap.cameraState.center
            Log.i("Center", center.toString())

            // ALERTA: solo comentar para pruebas seguras y especificas, ya que consume mucho
            // cada vez que se mueve el mapa, se obtiene la dirección del centro
            // es un costo adicional

            obtainNamePlace(center.longitude(), center.latitude())

            isUserInteracting = false
        }

        btnNewReport.setOnClickListener {
            val intent = Intent(this, TypeReportsActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }
        btnSosEmergency.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Enviar alerta de emergencia")
                .setMessage("¿Seguro que quieres enviar una alerta SOS? Se notificará a la municipalidad de tu zona.")
                .setPositiveButton("Enviar") { _, _ -> sendSosEmergency() }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        loadUserPhoto()
        listenKeyboard()
        locateCurrentPosition()
        obtainAllLocations()
        setupMap()
        navigationMenu()
        deleteAllAlerts()
        moveToRecentLocation()
        GlobalScope.launch {
            recoverRecentLocations()
        }
    }

    private fun moveToRecentLocation() {
        if((GlobalRecentLocation.latitude != 0.0 || GlobalRecentLocation.longitude != 0.0) && lastRecentLongitude != GlobalRecentLocation.longitude && lastRecentLatitude != GlobalRecentLocation.latitude) {
            moveCamera(GlobalRecentLocation.longitude, GlobalRecentLocation.latitude)
            lastRecentLongitude = GlobalRecentLocation.longitude
            lastRecentLatitude = GlobalRecentLocation.latitude
            Log.i("andriushinis", "komovamos")
        }
    }

    private fun loadUserPhoto() {
        val service = com.innovatech.peaceapp.Profile.Models.RetrofitClient.getClient(token)

        service.getUserByEmail(email)
            .enqueue(
                object: Callback<UserProfile> {
                    override fun onResponse(call: Call<UserProfile>, response:
                    Response<UserProfile>) {
                        val userProfile = response.body()
                        if (userProfile != null) {
                            Picasso.get().load(userProfile.profileImage).into(userPhoto)
                        }
                    }

                    override fun onFailure(p0: Call<UserProfile>, p1: Throwable) {
                        p1.printStackTrace()
                    }
                })


    }
    private fun sharedGlobalCoordinates() {
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("currentLocation", txtCurrentLocation.text.toString())
            putString("latitude", coordinatesCurrentLocation.latitude().toString())
            putString("longitude", coordinatesCurrentLocation.longitude().toString())
            apply()
        }
    }

    // function to select the suggestion
    private fun selectSuggestion(suggestion: AddressAutofillSuggestion) {
        lifecycleScope.launchWhenStarted {
            // ALERTA AddressAutoFill
            val response = addressAutofill.select(suggestion)
            response.onValue { result ->
                Log.i("AddressAutofill SELECTSUGGESTION", "Selected suggestion: $result")
                // obtaining the point of the selected location
                coordinatesCurrentLocation = result.suggestion.coordinate!!
                val firstAddress = result.suggestion.name
                val secondAddress = result.suggestion.formattedAddress

                val recentLocation = LocationModel(
                    null,
                    coordinatesCurrentLocation.latitude(),
                    coordinatesCurrentLocation.longitude(),
                    firstAddress,
                    secondAddress
                )

                sharedGlobalCoordinates()
                // showing the result.address
                showAddressAutofillResult(result)

                GlobalScope.launch(Dispatchers.IO) {
                    obtainRecentLocations(recentLocation)
                }


            }.onError {
                Log.e("AddressAutofill", "Error selecting suggestion: $it")
            }
        }
    }

    // function to show the address in the map
    private fun showAddressAutofillResult(result: AddressAutofillResult) {
        val address = result.address

        Log.i("AddressAutofill showAddressAutofillResult", "Address: $address")

        // move the camera to the selected location
        moveCamera(coordinatesCurrentLocation.longitude(), coordinatesCurrentLocation.latitude())
        ignoreNextMapIdleEvent = true
        ignoreNextQueryTextUpdate = true

        // set the name of the current location in the text view
        txtCurrentLocation.text = listOfNotNull(
            address.houseNumber,
            address.street
        ).joinToString()
        searchLocation.clearFocus()

        // clear the edit text
        searchLocation.text.clear()
        searchResultsView.isVisible = false
        collapseSearchBox()
    }

    private suspend fun recoverRecentLocations() {
        val db = AppDatabase.getDatabase(this)
        val recycler = findViewById<RecyclerView>(R.id.locationsRecyclerView)
        GlobalScope.launch {
            var locations = db.reportDAO().listLocations()

            // sort descending
            locations = locations.sortedByDescending { it.id }

            Log.i("andriush", locations.toString())

            withContext(Dispatchers.Main) {
                recycler.layoutManager = LinearLayoutManager(applicationContext)
                recycler.adapter = AdapterLocationRecent(locations)
            }

        }
    }

    private suspend fun saveRecentLocation(location: LocationModel) {
        val db = AppDatabase.getDatabase(this)
        GlobalScope.launch(Dispatchers.IO) {
            db.reportDAO().insert(location)
        }
    }

    private suspend fun recoverLocations(): List<LocationModel> {
        val db = AppDatabase.getDatabase(this)
        var locations = emptyList<LocationModel>()
        GlobalScope.launch {
            locations = db.reportDAO().listLocations()
            locations = locations.sortedByDescending { it.id }
        }
        return locations
    }

    private suspend fun obtainRecentLocations(location: LocationModel) {
        var locationsRecents: List<LocationModel>
        val db = AppDatabase.getDatabase(this)
        // Use withContext to suspend until recoverLocations() is complete
        withContext(Dispatchers.IO) {
            locationsRecents = recoverLocations()
            Log.i("andriush2", locationsRecents.toString())
            if (locationsRecents.size < 2) {
                // Save the recent location in a background coroutine
                withContext(Dispatchers.IO) {
                    saveRecentLocation(location)
                    recoverRecentLocations()

                }
            } else {
                withContext(Dispatchers.IO) {
                    db.reportDAO().delete(locationsRecents[0])
                    saveRecentLocation(location)
                    recoverRecentLocations()
                }
            }
        }


    }

    private fun navigationMenu() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->

            // check the icon for default with the actual activity

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
                R.id.nav_ai -> {
                    val intent = Intent(this, ChatbotActivity::class.java)
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

        bottomNavigationView.menu.findItem(R.id.nav_map).setChecked(true)
    }

    private fun obtainAllLocations() {
        val service = RetrofitClient.getClient(token)
        val reportsLocations = HashMap<Int, String>()
        service.getAllReports().enqueue(object: Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                val reports = response.body()
                if (reports != null) {
                    for(report in reports) {
                        reportsLocations[report.id] = report.type
                    }
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                Log.e("Error MAP", t.message.toString())
            }
        })

        service.getLocations().enqueue(object: Callback<List<Beans.Location>> {
            override fun onResponse(call: Call<List<Beans.Location>>, response: Response<List<Beans.Location>>) {
                val locations = response.body()
                Log.d("MapActivity", "Locations: $locations") // Log the locations
                if (locations != null) {
                    for (location in locations) {
                        if(location.latitude == 0.0 && location.longitude == 0.0) continue

                        var typeReport = R.drawable.alert_marker
                        when (reportsLocations[location.idReport]) {
                            "Robo" -> typeReport = R.drawable.alert_marker
                            "Accidente" -> typeReport = R.drawable.accident_marker
                            "Falta de iluminación" -> typeReport = R.drawable.illumination_marker
                            "Acoso" -> typeReport = R.drawable.acoso_marker
                            "Otro" -> typeReport = R.drawable.other_marker
                        }

                        addMarker(location.latitude, location.longitude, typeReport, location.idReport)
                    }
                }
            }

            override fun onFailure(call: Call<List<Beans.Location>>, t: Throwable) {
                Log.e("Error MAP", t.message.toString())
            }
        })

    }

    private fun addMarker(latitude: Double, longitude: Double, svgResId: Int, reportId: Int) {
        val drawable = AppCompatResources.getDrawable(this, svgResId)
        val bitmap = drawableToBitmap(drawable!!)

        val point = Point.fromLngLat(longitude, latitude)
        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(bitmap)
            .withIconSize(0.5)
        val annotation = pointAnnotationManager.create(pointAnnotationOptions)

        pointAnnotationManager.addClickListener { clickedAnnotation ->
            if (clickedAnnotation == annotation) {
                showReportQuickDetails(reportId)
                true
            } else {
                false
            }
        }
    }

    private fun showReportQuickDetails(reportId: Int) {
        val service = RetrofitClient.getClient(token)

        service.getReportById(reportId).enqueue(object : Callback<Report> {
            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                val report = response.body()
                if (response.isSuccessful && report != null) {
                    openReportDetail(report)
                } else {
                    Toast.makeText(this@MapActivity, "No se pudo cargar el reporte", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Report>, t: Throwable) {
                Log.e("MapActivity", "Error cargando reporte $reportId: ${t.message}")
                Toast.makeText(this@MapActivity, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openReportDetail(report: Report) {
        val intent = Intent(this, ReportDetailActivity::class.java)
        intent.putExtra("report", report)
        intent.putExtra("token", token)
        startActivity(intent)
    }
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun locateCurrentPosition() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        val locationComponentPlugin: LocationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            enabled = true
            locationPuck = LocationPuck2D(
                topImage = null,
                bearingImage = null,
                shadowImage = null
            )
        }

        locationComponentPlugin.addOnIndicatorPositionChangedListener { point ->
            val latitude = point.latitude()
            val longitude = point.longitude()

            // Mantener la ubicación en vivo para la detección de proximidad
            realUserLocation = point
            coordinatesCurrentLocation = point

            if (c == 0) {
                obtainNamePlace(longitude, latitude)
                moveCamera(longitude, latitude)
                sharedGlobalCoordinates()
                c++
            }
        }
    }


    private fun obtainNamePlace(longitude: Double, latitude: Double) {
        // obtaining the name of the current location
        val service = RetrofitClientMapbox.getClient()
        Log.i("Geocoding API obtainNamePlace", "Longitude: $longitude, Latitude: $latitude")
        service.getPlace(longitude, latitude, getString(R.string.mapbox_access_token)).enqueue(object: Callback<PropertiesPlace> {
            override fun onResponse(call: Call<PropertiesPlace>, response: Response<PropertiesPlace>) {
                val place = response.body()
                Log.i("Place", place.toString())

                // Set the name of the current location}
                var features = place?.features
                if (features != null) {
                    if (features.isEmpty()){
                        Toast.makeText(this@MapActivity, "Activar ubicación", Toast.LENGTH_SHORT)
                            .show()

                    }else{
                        currentLocation = place?.features?.get(0)?.properties?.name_preferred.toString()
                        if (isSamePoint(realUserLocation, longitude, latitude)) {
                            realUserLocationName = currentLocation
                        }
                        coordinatesCurrentLocation = Point.fromLngLat(longitude, latitude)
                        txtCurrentLocation.text = currentLocation

                        sharedGlobalCoordinates() // Store coordinates if needed

                    }
                }
            }

            override fun onFailure(call: Call<PropertiesPlace>, t: Throwable) {
                Log.e("Error MAP", t.message.toString())
            }
        })
    }

    private fun sendSosEmergency() {
        val livePoint = realUserLocation
        if (livePoint == null || (livePoint.latitude() == 0.0 && livePoint.longitude() == 0.0)) {
            Toast.makeText(this, "Activa la ubicación para enviar SOS", Toast.LENGTH_LONG).show()
            locateCurrentPosition()
            return
        }

        val sosLocation = if (realUserLocationName.isNotBlank()) {
            realUserLocationName
        } else {
            "SOS: ${livePoint.latitude()}, ${livePoint.longitude()}"
        }

        val sosReport = ReportSchema(
            title = "SOS",
            description = "Alerta SOS enviada desde la app móvil.",
            type = "OTHER",
            userId = userId,
            imageUrl = null,
            videoUrl = null,
            audioUrl = null,
            location = sosLocation,
            district = null,
            latitude = livePoint.latitude().toString(),
            longitude = livePoint.longitude().toString(),
            isEmergency = true
        )

        val service = RetrofitClient.getClient(token)
        service.postReport(sosReport).enqueue(object : Callback<Report> {
            override fun onResponse(call: Call<Report>, response: Response<Report>) {
                val createdReport = response.body()
                if (response.isSuccessful && createdReport != null) {
                    fetchMunicipalityForSos(createdReport)
                } else {
                    val backendMsg = try { response.errorBody()?.string() } catch (e: Exception) { null }
                    Log.e("SOS", "Error ${response.code()}: ${response.message()} - $backendMsg")
                    val msg = if (!backendMsg.isNullOrBlank() && response.code() == 400)
                        backendMsg
                    else
                        "No se pudo enviar la alerta SOS"
                    Toast.makeText(this@MapActivity, msg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Report>, t: Throwable) {
                Log.e("SOS", "Error enviando SOS: ${t.message}")
                Toast.makeText(this@MapActivity, "No se pudo conectar para enviar SOS", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchMunicipalityForSos(report: Report) {
        val district = report.district
        // La municipalidad debe tener cobertura real en tu zona; si no, no se redirige a ninguna.
        if (district.isNullOrBlank() || district.equals("Fuera de cobertura", ignoreCase = true)) {
            showSosConfirmation(null, "Alerta SOS enviada, pero no hay una municipalidad registrada en tu zona.")
            return
        }

        val service = RetrofitClient.getClient(token)
        service.getMunicipalityByDistrict(district).enqueue(object : Callback<MunicipalityProfile> {
            override fun onResponse(call: Call<MunicipalityProfile>, response: Response<MunicipalityProfile>) {
                val municipality = response.body()
                if (response.isSuccessful && municipality != null) {
                    showSosConfirmation(municipality, "Alerta SOS enviada a ${municipality.district ?: district}.")
                } else {
                    showSosConfirmation(null, "Alerta SOS enviada, pero $district no tiene una municipalidad registrada.")
                }
            }

            override fun onFailure(call: Call<MunicipalityProfile>, t: Throwable) {
                Log.e("SOS", "No se pudo obtener municipalidad: ${t.message}")
                showSosConfirmation(null, "Alerta SOS enviada. No se pudo verificar la municipalidad.")
            }
        })
    }

    private fun showSosConfirmation(municipality: MunicipalityProfile?, message: String) {
        val phone = municipality?.phone?.takeIf { it.isNotBlank() }
        val builder = AlertDialog.Builder(this)
            .setTitle("SOS enviado")
            .setMessage(
                if (phone != null) {
                    "$message\nMunicipalidad: ${municipality.municipalityName ?: "Municipalidad"}\nTeléfono: $phone"
                } else {
                    message
                }
            )
            .setPositiveButton("Aceptar", null)

        if (phone != null) {
            builder.setNegativeButton("Llamar ahora") { _, _ ->
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
            }
        }

        builder.show()
    }

    private fun isSamePoint(point: Point?, longitude: Double, latitude: Double): Boolean {
        if (point == null) return false
        return kotlin.math.abs(point.longitude() - longitude) < 0.000001 &&
                kotlin.math.abs(point.latitude() - latitude) < 0.000001
    }

    private fun moveCamera(longitude: Double, latitude: Double) {
        mapView.camera.easeTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(longitude, latitude))
                .zoom(15.0)
                .build()
        )
    }

    private fun setupMap() {
        mapView.logo.updateSettings {
            enabled = false
        }

        mapView.compass.updateSettings {
            enabled = false
        }

        // disable scale bar of mapbox on the top left corner of the screen
        mapView.scalebar.updateSettings {
            enabled = false
        }

        // disable the information icon next of mapbox logo
        mapView.attribution.updateSettings {
            enabled = false
        }
    }

    private fun listenKeyboard() {
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        mainLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            mainLayout.getWindowVisibleDisplayFrame(r)
            val screenHeight = mainLayout.rootView.height
            val keypadHeight = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) {
                // Teclado visible, expandir caja de búsqueda
                if (!isKeyboardVisible) {
                    expandSearchBox()
                    isKeyboardVisible = true
                }
            } else {
                if (isKeyboardVisible) {
                    collapseSearchBox()
                    isKeyboardVisible = false
                }
            }
        }

        expandArrowManually()
    }

    private fun expandArrowManually() {
        expandArrow!!.setOnClickListener { v: View? ->
            if (isExpanded) {
                collapseSearchBox()
            } else {
                expandSearchBox()
            }
            toggleArrow()
            isExpanded = !isExpanded
        }

        compressedArrow!!.setOnClickListener { v: View? ->
            if (isExpanded) {
                collapseSearchBox()
            } else {
                expandSearchBox()
            }
            toggleArrow()
            isExpanded = !isExpanded
        }
    }

    private fun expandSearchBox() {
        expandArrow!!.visibility = View.GONE
        compressedArrow!!.visibility = View.VISIBLE
        val animator = ValueAnimator.ofInt(searchBox.height, 1300)
        animator.addUpdateListener { valueAnimator: ValueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            searchBox.layoutParams.height = `val`
            searchBox.requestLayout()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.setDuration(300)
        animator.start()

    }

    private fun collapseSearchBox() {
        expandArrow!!.visibility = View.VISIBLE
        compressedArrow!!.visibility = View.GONE
        val animator = ValueAnimator.ofInt(searchBox.height, 450)
        animator.addUpdateListener { valueAnimator: ValueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            searchBox.layoutParams.height = `val`
            searchBox.requestLayout()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.setDuration(300)
        animator.start()
    }
    private fun toggleArrow() {
        if (isExpanded) {
            expandArrow!!.visibility = View.VISIBLE
            compressedArrow!!.visibility = View.GONE
        } else {
            expandArrow!!.visibility = View.GONE
            compressedArrow!!.visibility = View.VISIBLE
        }
    }
    // Function to calculate the distance between two coordinates
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radius of the Earth in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
    // Function to check proximity to reports and create alerts if needed
// ======================= checkProximityToReports =======================
    private fun checkProximityToReports(userLat: Double, userLon: Double) {
        val radius = 0.5 // km = 500m
        val service = RetrofitClient.getClient(token)

        Log.i("AlertCheck", "📡 Ejecutando checkProximityToReports()...")

        service.getLocations().enqueue(object : Callback<List<Beans.Location>> {
            override fun onResponse(call: Call<List<Beans.Location>>, response: Response<List<Beans.Location>>) {
                val locations = response.body()
                if (locations != null) {
                    Log.i("AlertCheck", "📍 Total de ubicaciones recibidas: ${locations.size}")

                    var nearbyAlertCount = 0
                    for (location in locations) {
                        if (location.latitude == 0.0 && location.longitude == 0.0) continue
                        Log.i("AlertCheck", "🧭 Analizando ubicación: $location")

                        // Evitar procesar el mismo reporte varias veces
                        if (processedReports.contains(location.idReport)) {
                            Log.i("AlertCheck", "🔁 Reporte ${location.idReport} ya procesado, saltando.")
                            continue
                        }

                        // Calcular distancia
                        val distance = calculateDistance(userLat, userLon, location.latitude, location.longitude)
                        Log.i("AlertCheck", "📏 Distancia a reporte ${location.idReport}: ${(distance * 1000).toInt()}m")

                        if (distance <= radius) {
                            Log.i("AlertCheck", "✅ Reporte ${location.idReport} dentro de rango, creando alerta...")
                            createNewAlert(location)
                            processedReports.add(location.idReport)
                            nearbyAlertCount++
                        }
                    }

                    Log.i("AlertCheck", "📊 Total de alertas creadas o detectadas en esta pasada: $nearbyAlertCount")
                    if (nearbyAlertCount > 1) {
                        showAlertPopupForMultipleAlerts()
                    }
                } else {
                    Log.w("AlertCheck", "⚠️ No se recibieron ubicaciones del backend.")
                }
            }

            override fun onFailure(call: Call<List<Beans.Location>>, t: Throwable) {
                Log.e("AlertCheck", "❌ Error al obtener ubicaciones: ${t.message}")
            }
        })
    }
    // ======================= createNewAlert =======================
    private fun createNewAlert(location: Beans.Location) {
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)

        Log.i("AlertCreate", "👤 Retrieved User ID: $userId")

        if (userId == 0) {
            Log.e("AlertCreate", "⚠️ User ID not found in shared preferences, cancelando creación.")
            return
        }

        val service = RetrofitClient.getClient(token)
        service.getAllReports().enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                val reports = response.body()
                val report = reports?.find { it.id == location.idReport }

                if (report == null) {
                    Log.w("AlertCreate", "⚠️ Reporte no encontrado para loc.idReport=${location.idReport}")
                    return
                }

                Log.i("AlertCreate", "🚀 Creando alerta para reporte ID ${report.id} (${report.type})")

                showProximityNotification(report)

                val alertSchema = AlertSchema(
                    location = report.location,
                    type = report.type,
                    description = report.description,
                    userId = userId,
                    imageUrl = report.imageUrl,
                    reportId = report.id
                )

                Log.i("AlertCreate", "🛰️ Preparando alerta para enviar: $alertSchema")

                checkForDuplicateAlert(alertSchema) { exists ->
                    if (!exists) {
                        Log.i("AlertCreate", "📤 Enviando alerta al backend...")
                        postAlert(alertSchema)
                    } else {
                        Log.i("AlertCreate", "⚠️ Alerta duplicada detectada, no se enviará.")
                    }
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                Log.e("AlertCreate", "❌ Error al obtener reportes: ${t.message}")
            }
        })
    }

    // Function to check for duplicate alerts based on the id or alert schema
    private fun checkForDuplicateAlert(alertSchema: AlertSchema, callback: (exists: Boolean) -> Unit) {
        val service = RetrofitClient.getClient(token)

        // Query to get all alerts for the user
        service.getAllAlerts().enqueue(object : Callback<List<Alert>> {
            override fun onResponse(call: Call<List<Alert>>, response: Response<List<Alert>>) {
                val alerts = response.body()
                if (alerts != null) {
                    // Check if any alert matches the current alert schema or has the same id
                    val duplicateAlert = alerts.find {
                                (it.idUser == alertSchema.userId &&
                                        it.location == alertSchema.location &&
                                        it.type == alertSchema.type &&
                                        it.description == alertSchema.description)
                    }
                    callback(duplicateAlert != null) // Return true if duplicate is found, else false
                } else {
                    callback(false) // No alerts, so no duplicates
                }
            }

            override fun onFailure(call: Call<List<Alert>>, t: Throwable) {
                Log.e("Error", "Failed to fetch alerts: ${t.message}")
                callback(false) // On failure, assume no duplicates
            }
        })
    }

    // Function to show a popup when multiple alerts are detected
    private fun showAlertPopupForMultipleAlerts() {
        val alertDialog = android.app.AlertDialog.Builder(this)
        alertDialog.setTitle("Alerta Detectada")
        alertDialog.setMessage("Se ha detectado una alerta en la zona.")
        alertDialog.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }
    // ======================= Notificación de proximidad =======================
    private fun tipoEnEspanol(type: String): String {
        return when (type.uppercase()) {
            "ROBBERY" -> "Robo"
            "HARASSMENT" -> "Acoso"
            "ACCIDENT" -> "Accidente"
            "DARK_AREA" -> "Zona oscura"
            else -> "Incidente"
        }
    }

    private fun showProximityNotification(report: Report) {
        val channelId = "proximity_alerts"
        val manager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Alertas de proximidad",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Avisos cuando te acercas a una zona de riesgo"
            manager.createNotificationChannel(channel)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("AlertNotif", "Sin permiso POST_NOTIFICATIONS; no se muestra la notificación")
            return
        }

        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra("token", token)
        val pending = android.app.PendingIntent.getActivity(
            this, report.id, intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_warning_24)
            .setContentTitle("Zona de riesgo cercana")
            .setContentText("${tipoEnEspanol(report.type)} cerca de ${report.location}")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        manager.notify(report.id, notification)
        Log.i("AlertNotif", "🔔 Notificación de proximidad mostrada para reporte ${report.id}")
    }

    private fun postAlert(alertSchema: AlertSchema) {
        val service = RetrofitClient.getClient(token)

        // 🔍 Log del objeto antes de enviarlo
        Log.i("AlertDebug", "📦 Enviando JSON: ${com.google.gson.Gson().toJson(alertSchema)}")

        service.postAlert(alertSchema).enqueue(object : Callback<Alert> {
            override fun onResponse(call: Call<Alert>, response: Response<Alert>) {
                Log.i("AlertDebug", "📡 Código de respuesta: ${response.code()}")

                if (response.isSuccessful) {
                    Log.i("AlertDebug", "✅ Alerta creada correctamente: ${response.body()}")
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("AlertDebug", "❌ Error del servidor (${response.code()}): $errorBody")
                    } catch (e: Exception) {
                        Log.e("AlertDebug", "⚠️ No se pudo leer errorBody: ${e.message}")
                    }
                }
            }

            override fun onFailure(call: Call<Alert>, t: Throwable) {
                Log.e("AlertDebug", "💥 Falló conexión al backend: ${t.message}")
                t.printStackTrace()
            }
        })
    }
    private fun deleteAllAlerts() {
        val service = RetrofitClient.getClient(token)
        val sharedPref = getSharedPreferences("GlobalPrefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)

        if (userId == 0) {
            Log.e("MapActivity", "User ID not found, cannot delete alerts.")
            return
        }

        service.deleteAlertsByUserId(userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.i("MapActivity", "✅ All alerts deleted successfully for user $userId.")
                } else {
                    Log.e("MapActivity", "❌ Failed to delete alerts: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("MapActivity", "💥 Error deleting alerts: ${t.message}")
            }
        })
    }

}
