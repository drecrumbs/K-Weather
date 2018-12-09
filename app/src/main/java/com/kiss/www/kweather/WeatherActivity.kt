package com.kiss.www.kweather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kiss.www.kweather.Common.Common
import com.kiss.www.kweather.Common.Helper
import com.kiss.www.kweather.Model.OpenWeather
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_weather.*

class WeatherActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SwipeRefreshLayout.OnRefreshListener {


    //Constants
    val PERMISSIONS_REQUEST_CODE = 1001
    val PLAY_SERVICE_RESOLUTION_REQUEST = 1000


    //Variables
    var mGoogleApiClient:GoogleApiClient ?= null
    var mLocationRequest:LocationRequest ?= null
    var mLocationCallback: LocationCallback? = null
    internal var openWeather = OpenWeather()
    var refreshLocation = false;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        requestPermissions()
        if (hasGooglePlayServices()) {
            buildGoogleApiClient()
        }
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.i("LocationResult", locationResult.toString())
                super.onLocationResult(locationResult)
                val lat = locationResult?.locations?.get(0)?.latitude.toString()
                val lon = locationResult?.locations?.get(0)?.longitude.toString()
                GetWeather().execute(Common.apiRequest(lat, lon))
            }
        }
        layoutSwipeRefresh.setOnRefreshListener(this)
    }

    override fun onRefresh() {
        createLocationRequest()
    }

    override fun onStart() {
        super.onStart()
        if(mGoogleApiClient != null)
        {
            mGoogleApiClient!!.connect()
        }
    }

    override fun onDestroy() {
        mGoogleApiClient!!.disconnect()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun requestPermissions() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISSIONS_REQUEST_CODE -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (hasGooglePlayServices())
                    {
                        buildGoogleApiClient()
                    }
                }
            }
        }
    }

    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener { this }
                .addApi(LocationServices.API)
                .build()

    }

    private fun hasGooglePlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if(resultCode != ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RESOLUTION_REQUEST).show()
            }
            else
            {
                Toast.makeText(applicationContext,"This device is not supported",Toast.LENGTH_SHORT).show()
            }
            return false
        }
        return true
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("ERROR", "Connection failed: " + p0.errorCode)
    }

    override fun onConnected(p0: Bundle?) {
        createLocationRequest()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 10000 // 10 seconds
        mLocationRequest!!.fastestInterval = 5000 // 5 Seconds
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.setNumUpdates(1)

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return

        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient!!.connect()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private inner class GetWeather: AsyncTask<String, Void, String>()
    {

        override fun onPreExecute() {
            super.onPreExecute()
            layoutSwipeRefresh.isRefreshing = true
            layoutWeatherContainer.visibility = View.GONE
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(result!!.contains("Error: Not found city")) {
                return
            }
            val gson = Gson()
            val mType = object: TypeToken<OpenWeather>(){}.type

            openWeather = gson.fromJson<OpenWeather>(result,mType)
            //Set Information into UI
            txtCity.text = "${openWeather.name}, ${openWeather.sys!!.country}"
            txtLastUpdate.text = "Last Updated: ${Common.dateNow}"
            txtDescription.text = "${openWeather.weather!![0].description}"
            txtTime.text = "Sunrise: ${Common.unixTimeStampToDateTime(openWeather.sys!!.sunrise)} - Sunset: ${Common.unixTimeStampToDateTime(openWeather.sys!!.sunset)}"
            txtHumidity.text = "Humidity: ${openWeather.main!!.humidity}%"
            txtTemperature.text = "${openWeather.main!!.temp.toInt()}${Typography.degree}F"
            Picasso.with(this@WeatherActivity)
                    .load(Common.getImage(openWeather.weather!![0].icon!!))
                    .into(imgWeatherIcon)

            layoutSwipeRefresh.isRefreshing = false
            layoutWeatherContainer.visibility = View.VISIBLE

            //  refreshLocation = false
        }

        override fun doInBackground(vararg params: String?): String {
            var stream:String?=null
            var urlString=params[0]

            val http = Helper()

            stream = http.getHTTPData(urlString)
            return stream

        }

    }
}
