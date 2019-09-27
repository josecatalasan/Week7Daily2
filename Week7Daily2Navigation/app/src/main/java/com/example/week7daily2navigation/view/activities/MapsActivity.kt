package com.example.week7daily2navigation.view.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.week7daily2navigation.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_maps.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener{

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient

    private var geofenceList = ArrayList<Geofence>()

    private val PERMISSION_INDEX_ID = 101
    private val _displayLocationZoomLevel = 15.0f
    private val _myLocationZoomLevel = 16.0f

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //set up functions
        askForPermissions()
        setFabListeners()
        setupNotificationChannel()

    }

    override fun onDestroy() {
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d("Geofence Removing", "Geofences removed.")
            }
            addOnFailureListener {
                Log.d("Geofence Removing", "Geofences failed to be removed.")
            }
        }
        super.onDestroy()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
            override fun onMarkerDragStart(marker: Marker?) { marker?.remove() }
                override fun onMarkerDragEnd(p0: Marker?) {}
                override fun onMarkerDrag(p0: Marker?) {}
        })
        map.setOnMyLocationButtonClickListener(this)
        //Center camera on USA
        map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(38.0,-97.0)))
        map.moveCamera(CameraUpdateFactory.zoomTo(3.0f))

    }

    // Google Map functions
    private fun displayLocationOnMap(latLng : LatLng, title : String?){
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.cat_from_below)

        var marker = map.addMarker(MarkerOptions().position(latLng).title(title).draggable(true).icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
        addGeofence(latLng, title)

        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        map.animateCamera(CameraUpdateFactory.zoomTo(_displayLocationZoomLevel))

    }

    //Geofencing
    private fun addGeofence(latLng : LatLng, title : String?){
        //add geofence to location
        geofenceList.add(
            Geofence.Builder().setRequestId(title)
                .setCircularRegion(latLng.latitude, latLng.longitude, 30.0f)
                .setExpirationDuration(1000*60*60)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        )

        geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences added
                Log.d("Geofence Adding", "Geofences added.")
            }
            addOnFailureListener {
                // Failed to add geofences
                Log.d("Geofence Adding", "Geofences NOT added.")
            }
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    //Geocoding
    private fun getLocationByAddress(address : String) : LatLng? {
        val geocoder = Geocoder(this)
        val addressResult = geocoder.getFromLocationName(address, 1)
        // check for empty result
        if(addressResult.size == 0)
            return null
        return LatLng(addressResult[0].latitude, addressResult[0].longitude)
    }

    //Reverse Geocoding
    private fun getLocationByLatLng(coordinates : LatLng) : String{
        val geocoder = Geocoder(this)
        val addressResult = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1) //check for empty result

        return addressResult[0].getAddressLine(0)
    }

    //button to search for address
    fun onFindAddress(view: View) {
        val address : String = etAddress.text.toString()
        val retrievedLatLng : LatLng?

        if(address.isNotEmpty()){
            retrievedLatLng = getLocationByAddress(address) ?: return
            displayLocationOnMap(retrievedLatLng, getLocationByLatLng(retrievedLatLng))
        }
        hideKeyboard()
    }

    //radio buttons to change map type
    fun onChangeMap(view: View) {
        //set Map properties NORMAL, HYBRID  SATELLITE, TERRAIN, NONE
        when(view.id){
            R.id.radioNormal ->     {map.mapType = GoogleMap.MAP_TYPE_NORMAL; changeRadioTextColors(false)}
            R.id.radioHybrid ->     {map.mapType = GoogleMap.MAP_TYPE_HYBRID; changeRadioTextColors(true)}
            R.id.radioSatellite ->  {map.mapType = GoogleMap.MAP_TYPE_SATELLITE; changeRadioTextColors(true)}
            R.id.radioTerrain ->    {map.mapType = GoogleMap.MAP_TYPE_TERRAIN; changeRadioTextColors(false)}
            R.id.radioNone ->       {map.mapType = GoogleMap.MAP_TYPE_NONE; changeRadioTextColors(false)}
        }
    }

    //helper functions
    private fun hideKeyboard() {
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun changeRadioTextColors(white: Boolean) {
        if (white) {
            radioNormal.setTextColor(ContextCompat.getColor(this,R.color.white))
            radioHybrid.setTextColor(ContextCompat.getColor(this,R.color.white))
            radioSatellite.setTextColor(ContextCompat.getColor(this,R.color.white))
            radioTerrain.setTextColor(ContextCompat.getColor(this,R.color.white))
            radioNone.setTextColor(ContextCompat.getColor(this,R.color.white))
        } else { //black
            radioNormal.setTextColor(ContextCompat.getColor(this,R.color.black))
            radioHybrid.setTextColor(ContextCompat.getColor(this,R.color.black))
            radioSatellite.setTextColor(ContextCompat.getColor(this,R.color.black))
            radioTerrain.setTextColor(ContextCompat.getColor(this,R.color.black))
            radioNone.setTextColor(ContextCompat.getColor(this,R.color.black))
        }
    }

    private fun askForPermissions(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_INDEX_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), PERMISSION_INDEX_ID)
        }
    }

    private fun setFabListeners(){
        // display current location
        fabMyLocation.setOnClickListener {
            fusedLocationClient.lastLocation.addOnSuccessListener{
                if(it != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
                    map.animateCamera(CameraUpdateFactory.zoomTo(_myLocationZoomLevel))
                } else {
                    Toast.makeText(this, "Location is NULL", Toast.LENGTH_SHORT).show()
                }
            }}

        //open Google Navigation app to the current inputted address: google.navigation:q=latitude,longitude
        fabLaunchNavigation.setOnClickListener {
            hideKeyboard()
            val address : String = etAddress.text.toString()
            val retrievedLatLng : LatLng?

            if(address.isNotEmpty()){
                retrievedLatLng = getLocationByAddress(address) //can return null
                if(retrievedLatLng != null) {
                    val gmmIntentUri =
                        Uri.parse("google.navigation:q=" + retrievedLatLng.latitude + "," + retrievedLatLng.longitude) //get current address
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }
            }
        }
    }

    private fun setupNotificationChannel(){
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =  NotificationChannel("geofence_event", "Geofence Event Notification", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
    }

    //interface to respond to permission requests
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_INDEX_ID) {
            if (permissions.size == 1 &&
                permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
                map.isMyLocationEnabled = true
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(this, "My Location Permission Denied.", Toast.LENGTH_SHORT).show()
                fabMyLocation.hide()
            }
        }
    }

    //interface for my location services
    override fun onMyLocationButtonClick(): Boolean {
        return false
    }
}
