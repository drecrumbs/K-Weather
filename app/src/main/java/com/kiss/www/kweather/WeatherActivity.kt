package com.kiss.www.kweather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Typeface
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

class WeatherActivity : AppCompatActivity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SwipeRefreshLayout.OnRefreshListener {


    //Constants
    private val PERMISSIONS_REQUEST_CODE = 1001
    private val PLAY_SERVICE_RESOLUTION_REQUEST = 1000
    private val LOCATION_INTERVAL: Long = 36000000 // 10 * 1000 * 60 * 60 // Every Hour

    //Variables
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest = LocationRequest()
    private var mLocationCallback: LocationCallback? = null
    internal var openWeather = OpenWeather()
    var refreshLocation = false;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        txtWeatherIcon.typeface = Typeface.createFromAsset(assets, "fonts/weather_font_regular.ttf")
        txtTempUnit.typeface = Typeface.createFromAsset(assets, "fonts/weather_font_regular.ttf")

        requestPermissions()

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val locationLogString =
                        "\n\n " + "LAT: ${locationResult?.locations?.get(0)?.latitude.toString()}\n" +
                                "LON: ${locationResult?.locations?.get(0)?.latitude.toString()}\n" +
                                "ALT: ${locationResult?.locations?.get(0)?.altitude.toString()}\n" +
                                "ACCURACY: ${locationResult?.locations?.get(0)?.accuracy.toString()}\n\n"
                Log.i("LocationResult", locationLogString)

                super.onLocationResult(locationResult)
                val lat = locationResult?.locations?.get(0)?.latitude.toString()
                val lon = locationResult?.locations?.get(0)?.longitude.toString()
                GetWeather().execute(Common.apiRequest(lat, lon))
            }
        }
        layoutSwipeRefresh.setOnRefreshListener(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onRefresh() {
        createLocationRequest()
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onDestroy() {
        mGoogleApiClient?.disconnect()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun requestPermissions() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
        } else if (hasGooglePlayServices()) {
            buildGoogleApiClient()
        } else {
            Toast.makeText(this, "Uhh.. No access for you. Accept my permissions!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED } && hasGooglePlayServices())
                {
                    buildGoogleApiClient()
                } else {
                    Toast.makeText(this, "Uhh.. No access for you. Accept my permissions!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun buildGoogleApiClient() {
        mGoogleApiClient = if (mGoogleApiClient == null) GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener { this }
                .addApi(LocationServices.API)
                .build() else mGoogleApiClient
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
                Toast.makeText(applicationContext, "This device is not supported.", Toast.LENGTH_SHORT).show()
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

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return

        }

        mLocationRequest.interval = LOCATION_INTERVAL
        mLocationRequest.fastestInterval = LOCATION_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient!!.connect()
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

            // Null check.
            openWeather = if (result != null) getOpenWeatherObject(result) else openWeather

            // Update UI.
            runOnUiThread { updateUI(openWeather) }
            layoutSwipeRefresh.isRefreshing = false
        }

        override fun doInBackground(vararg params: String?): String {
            var stream: String?
            var urlString=params[0]

            val http = Helper()
            stream = http.getHTTPData(urlString)
            return stream
        }

        fun getOpenWeatherObject(vararg json: String): OpenWeather {
            if (json.contains("Error: Not found city")) {
                Log.e(this.javaClass.simpleName, "City Was Not Found!")
                Toast.makeText(applicationContext, "City Was Not Found", Toast.LENGTH_LONG).show()
                return openWeather
            }

            val gson = Gson()
            val mType = object : TypeToken<OpenWeather>() {}.type
            openWeather = gson.fromJson<OpenWeather>(json[0], mType)
            return openWeather
        }

    }

    private fun updateUI(openWeather: OpenWeather) {
        //Set Information into UI
        txtCity.text = "${openWeather.name}"
        txtLastUpdate.text = "Last Updated: ${Common.dateNow}"
        txtLastUpdate.text = "Last Update: ${Common.unixTimeStampToDateTime(openWeather.dt.toDouble(), Common.EEE_HH_MM_A)}"
        txtDescription.text = "${openWeather.weather!![0].description}"
                .replace(" ", "\n", true)
        txtSunriseTime.text = "Sunrise: ${Common.unixTimeStampToDateTime(openWeather.sys!!.sunrise, Common.HH_MM_A)}"
        txtSunsetTime.text = "Sunset: ${Common.unixTimeStampToDateTime(openWeather.sys!!.sunset, Common.HH_MM_A)}"
        txtHumidity.text = "Humidity: ${openWeather.main!!.humidity}%"
        txtTemperature.text = "${openWeather.main!!.temp.toInt()}"

        Picasso.with(this@WeatherActivity)
                .load(Common.getImage(openWeather.weather!![0].icon!!))
                .into(imgWeatherIcon)


        when (openWeather.weather!![0].icon!!) {
            "50n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_fog))
            "50d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_fog))
            "13n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_snow))
            "13d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_snow))
            "11n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_thunderstorm))
            "11d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_thunderstorm))
            "10n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_rain))
            "10d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_rain))
            "09n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_showers))
            "09d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_showers))
            "04n" -> my_weather_icon.setIconResource(getString(R.string.wi_forecast_io_partly_cloudy_night))
            "04d" -> my_weather_icon.setIconResource(getString(R.string.wi_forecast_io_partly_cloudy_day))
            "03n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_cloudy))
            "03d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_cloudy))
            "02n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_alt_partly_cloudy))
            "02d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_sunny_overcast))
            "01n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_clear))
            "01d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_sunny))

            else -> my_weather_icon.setIconResource(getString(R.string.wi_alien))
        }

        layoutWeatherContainer.visibility = View.VISIBLE
    }

}
