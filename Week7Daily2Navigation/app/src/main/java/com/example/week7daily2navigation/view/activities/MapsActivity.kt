package com.example.week7daily2navigation.view.activities

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.week7daily2navigation.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(38.0,-97.0)))
        map.moveCamera(CameraUpdateFactory.zoomTo(3.0f))
        //set Map properties
        //map.mapType = GoogleMap.MAP_TYPE_HYBRID
    }

    private fun displayLocationOnMap(latLng : LatLng, title : String){
        map.addMarker(MarkerOptions().position(latLng).title(title))
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        map.moveCamera(CameraUpdateFactory.zoomTo(10.0f))
    }

    //Geocoding
    fun getLocationByAddress(address : String) : LatLng {
        val geocoder = Geocoder(this)
        val addressResult = geocoder.getFromLocationName(address, 1)[0] // check for empty result
        return LatLng(addressResult.latitude, addressResult.longitude)
    }

    //Reverse Geocoding
    fun getLocationByLatLng(coordinates : LatLng) : String{
        val geocoder = Geocoder(this)
        val addressResult = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)[0] //check for empty result

        return addressResult.getAddressLine(0)
    }

    fun onClick(view: View) {
        val address : String = etAddress.text.toString()
        val retrievedLatLng : LatLng

        if(address.isNotEmpty()){
            retrievedLatLng = getLocationByAddress(address)
            displayLocationOnMap(retrievedLatLng, getLocationByLatLng(retrievedLatLng))
        }
    }
}
