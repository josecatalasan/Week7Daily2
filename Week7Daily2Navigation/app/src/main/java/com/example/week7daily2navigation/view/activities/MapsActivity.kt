package com.example.week7daily2navigation.view.activities

import android.Manifest
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
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMyLocationButtonClickListener{

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION_INDEX_ID = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //ask for location permission
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_INDEX_ID)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fabMyLocation.setOnClickListener {
            fusedLocationClient.lastLocation.addOnSuccessListener{
                map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude,it.longitude)))
                map.animateCamera(CameraUpdateFactory.zoomTo(13.0f))
            }}
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

    private fun displayLocationOnMap(latLng : LatLng, title : String?){
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.cat_from_below)

        var marker = map.addMarker(MarkerOptions().position(latLng).title(title).draggable(true).icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        map.animateCamera(CameraUpdateFactory.zoomTo(13.0f))
    }

    //Geocoding
    fun getLocationByAddress(address : String) : LatLng? {
        val geocoder = Geocoder(this)
        val addressResult = geocoder.getFromLocationName(address, 1)
        // check for empty result
        if(addressResult.size == 0)
            return null
        return LatLng(addressResult[0].latitude, addressResult[0].longitude)
    }

    //Reverse Geocoding
    fun getLocationByLatLng(coordinates : LatLng) : String{
        val geocoder = Geocoder(this)
        val addressResult = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1) //check for empty result

        return addressResult[0].getAddressLine(0)
    }

    fun onClick(view: View) {
        val address : String = etAddress.text.toString()
        val retrievedLatLng : LatLng?

        if(address.isNotEmpty()){
            retrievedLatLng = getLocationByAddress(address) ?: return
            displayLocationOnMap(retrievedLatLng, getLocationByLatLng(retrievedLatLng))
        }
        hideKeyboard()
    }

    fun changeMap(view: View) {
        //set Map properties NORMAL, HYBRID  SATELLITE, TERRAIN, NONE
        when(view.id){
            R.id.radioNormal -> {map.mapType = GoogleMap.MAP_TYPE_NORMAL; changeColors(false)}
            R.id.radioHybrid -> {map.mapType = GoogleMap.MAP_TYPE_HYBRID; changeColors(true)}
            R.id.radioSatellite -> {map.mapType = GoogleMap.MAP_TYPE_SATELLITE; changeColors(true)}
            R.id.radioTerrain -> {map.mapType = GoogleMap.MAP_TYPE_TERRAIN; changeColors(false)}
            R.id.radioNone -> {map.mapType = GoogleMap.MAP_TYPE_NONE; changeColors(false)}
        }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun changeColors(white: Boolean) {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_INDEX_ID) {
            if (permissions.size == 1 &&
                permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                map.isMyLocationEnabled = true
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(this, "My Location Permission Denied.", Toast.LENGTH_SHORT).show()
                fabMyLocation.hide()
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }
}
