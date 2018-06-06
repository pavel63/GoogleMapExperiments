package com.example.ios_razrab.googlemapexperiments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : FragmentActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null

    // на смену deprecated
     var mFusedLocationClient: FusedLocationProviderClient ? = null
     var mLocationRequest: LocationRequest = LocationRequest()
     var mLocationCallback: LocationCallback ? = null
     var mLastLocation: Location ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_lat_coarse .text = "0"
        tv_long_coarse .text = "0"
        tv_status_coarse .text = "Определяем приблизительное местоположение.."
        tv_lat_fine .text = "0"
        tv_long_fine .text = "0"
        tv_status_fine .text = "Определяем точное местоположение.."


       mFusedLocationClient  = LocationServices.getFusedLocationProviderClient(this)


                if (checkLocationSwithOn()) {

            if (Build.VERSION.SDK_INT >= 23) {

                if (!checkDynamicPerms()) {
                    callPermissions()
                } else {
                    permsGranted()
                }
            } else {
                permsGranted()
            }
        }
    }


    /**
     * Когда есть разрешения
     */
    internal fun permsGranted() {
        Log.i("Разрешено!!", "")

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        getCoarseLocation()

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {

                val locationList = locationResult!!.locations
                if (locationList.size > 0) {
                    //The last location in the list is the newest
                    val location = locationList[locationList.size - 1]
                    Log.i("MapsActivity", "Location: " + location.latitude + " " + location.longitude)
                    mLastLocation = location

                    val latLng = LatLng(location.latitude, location.longitude)

                    tv_status_fine.text = "Точная локация определена!"
                    tv_lat_fine.text = latLng.latitude.toString()
                    tv_long_fine.text = latLng.longitude.toString()

                    mMap!!.addMarker(MarkerOptions().position(latLng).title("It's Me!"))
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    mMap!!.animateCamera(CameraUpdateFactory.zoomTo(13f), 700, null)

                }
            }
        }
    }


    /**
     * Включена ли физически локация на устройстве
     */
    internal fun checkLocationSwithOn(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Call your Alert message

            Toast.makeText(this, "Для определения точного местоположения необходимо включить геолокацию на устройстве!", Toast.LENGTH_SHORT).show()

            tv_status_fine.text = "Точная локация недоступна!"
            tv_status_coarse.text = "Приблизительное положение недоступно!"

            return false
        } else {

            return true
        }
    }


    /**
     * Коллбек вызывается когда карта готова к использованию
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

           mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())

           mMap!!.isMyLocationEnabled = true

    }

    fun getCoarseLocation() {

        val locationProvider = LocationManager.NETWORK_PROVIDER
        // Or use LocationManager.GPS_PROVIDER

        // Acquire a reference to the system Location Manager
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // получение приблизительной
        // ССЫЛКА НА ОФИЦ ДОКУМЕНТАЦИЮ, ГДЕ ГОВОРЯТ ДЕЛАТЬ ИМЕННО ТАК!:
        // https://developer.android.com/guide/topics/location/strategies#BestPerformance
        val location = locationManager.getLastKnownLocation(locationProvider)

        try {
            tv_lat_coarse.text = "Долгота: " + location.latitude.toString()
            tv_long_coarse.text = "Широта: " + location.longitude.toString()

            tv_status_coarse.text = "Приблизительное местоположение обнаружено"

        } catch (e: NullPointerException) {

            Log.e("Ошибка приблизительн: ", e.toString())

            tv_status_coarse.text = "Приблизительное местоположение обнаружить не удалось. " + "Возможно, Вам следует включить геолокацию на устройстве"
            Log.d("Проблемы с гео: %s", e.toString())
            // Вероятно у пользователя отключена геолокация

        }

    }


    // region permissions -------------------------------------------

    /**
     * Если версия 6 или более,
     * запрашиваем динамические разрешения
     */
    private fun checkDynamicPerms(): Boolean {

        return  (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
    }


    internal fun callPermissions() {
        // если нет разрешения еще то запрашиваем
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_FINE_LOCATION)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    permsGranted()

                } else {

                    Toast.makeText(this, "Пользователь не дал разрешения",
                            Toast.LENGTH_SHORT).show()
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request.
    }

    companion object {

        internal val MY_PERMISSIONS_FINE_LOCATION = 1111
    }

    // endregion permissions ------------------------------------------

}
