package com.kiss.www.kweather

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.kiss.www.kweather.adapter.OutlookObject
import com.kiss.www.kweather.model.Model
import com.kiss.www.kweather.model.weatherModel.OpenWeather

class MainActivityViewModel : ViewModel() {
    var cachedOutlookObject: Array<OutlookObject>? = null
    val logTag = javaClass.simpleName

    val model: Model = Model.getInstance()

    val googleApiClient: MutableLiveData<GoogleApiClient> = MutableLiveData()
    var currentBackgroundColor: Int = Color.BLACK
    var bitmojiURL: MutableLiveData<String> = MutableLiveData()
    private var locationCallback: MutableLiveData<LocationCallback> = MutableLiveData()
    var weather: MutableLiveData<OpenWeather> = MutableLiveData()
    var location: MutableLiveData<Pair<String, String>> = MutableLiveData()

    init {
        locationCallback.value = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                location.value = Pair(locationResult?.locations?.get(0)?.latitude.toString(),
                        locationResult?.locations?.get(0)?.longitude.toString())

                val locationLogString =
                        "  LAT: ${location.value!!.first}" +
                                "  LON: ${location.value!!.second}" +
                                "  ALT: ${locationResult?.locations?.get(0)?.altitude.toString()}" +
                                "  ACCURACY: ${locationResult?.locations?.get(0)?.accuracy.toString()}"

                Log.d(logTag, "onLocationResult() -> $locationLogString")
                model.location = location
                model.refreshWeather(location.value!!)
            }
        }
        //  refreshNews()
    }

    fun locationUpdate(): MutableLiveData<Pair<String, String>> {
        return location
    }

    fun bitmojiUrlUpdate(): MutableLiveData<String> {
        return bitmojiURL
    }

    fun weatherUpdate(): MutableLiveData<OpenWeather> {
        return weather
    }

    fun locationCallback(): MutableLiveData<LocationCallback> {
        return locationCallback
    }

    fun googleApiClient(): MutableLiveData<GoogleApiClient> {
        return googleApiClient
    }

    fun refreshNews() {
        model.refreshNews()
    }

    fun refreshWeather() {
        model.refreshWeather()
    }

}