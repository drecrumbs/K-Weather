package com.kiss.www.kweather

import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kiss.www.kweather.Common.Common
import com.kiss.www.kweather.Common.Helper
import com.kiss.www.kweather.Model.OpenWeather
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_weather.*

class WeatherActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    //Constants
    val PERMISSIONS_REQUEST_CODE = 1001
    val PLAY_SERVICE_RESOLUTION_REQUEST = 1000


    //Variables
    var mGoogleApiClient:GoogleApiClient ?= null
    var mLocationRequest:LocationRequest ?= null
    internal var openWeather = OpenWeather()
    var locationCaptured = false;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        requestPermission()
        if(checkPlayService())
            buildGoogleApiClient()
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

        checkPlayService()
    }

    private fun requestPermission() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISSIONS_REQUEST_CODE -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayService())
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

    private fun checkPlayService(): Boolean {
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

    override fun onLocationChanged(location: Location?) {

      //  txtID.text = "${location!!.latitude} - ${location!!.longitude}"
        if(!locationCaptured) {
            Log.d("Debug", "Location Changed!")
            GetWeather().execute(Common.apiRequest(location!!.latitude.toString(), location!!.longitude.toString()))
        }
    }

    override fun onConnected(p0: Bundle?) {
        createLocationRequest()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 10000 // 10 seconds
        mLocationRequest!!.fastestInterval = 5000 // 5 Seconds
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return

        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this)
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient!!.connect()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private inner class GetWeather: AsyncTask<String, Void, String>()
    {
        internal var pd = ProgressDialog(this@WeatherActivity)

        override fun onPreExecute() {
            super.onPreExecute()
            pd.setTitle("Please wait...")
            pd.show()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(result!!.contains("Error: Not found city")) {
                pd.dismiss()
                return
            }
            val gson = Gson()
            val mType = object: TypeToken<OpenWeather>(){}.type

            openWeather = gson.fromJson<OpenWeather>(result,mType)
            pd.dismiss()

            //Set Information into UI
            txtCity.text = "${openWeather.name},${openWeather.sys!!.country}"
            txtLastUpdate.text = "Last Updated: ${Common.dateNow}"
            txtDescription.text = "${openWeather.weather!![0].description}"
            txtTime.text = "${Common.unixTimeStampToDateTime(openWeather.sys!!.sunrise)}/${Common.unixTimeStampToDateTime(openWeather.sys!!.sunset)}"
            txtHumidity.text = "${openWeather.main!!.humidity}"
            txtTemperature.text = "${openWeather.main!!.temp} F"
            Picasso.with(this@WeatherActivity)
                    .load(Common.getImage(openWeather.weather!![0].icon!!))
                    .into(imgWeatherIcon)

            locationCaptured = true;
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
