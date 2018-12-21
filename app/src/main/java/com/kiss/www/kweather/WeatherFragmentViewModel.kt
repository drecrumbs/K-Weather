package com.kiss.www.kweather

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.kiss.www.kweather.Model.Model
import com.kiss.www.kweather.Model.WeatherModel.OpenWeather

class WeatherFragmentViewModel : ViewModel() {
    val logTag = javaClass.simpleName

    val model: Model = Model.getInstance()

    val googleApiClient: MutableLiveData<GoogleApiClient> = MutableLiveData()

    var currentBackgroundColor: Int = Color.BLACK
    var bitmojiURL: MutableLiveData<String> = MutableLiveData()
    var locationCallback: MutableLiveData<LocationCallback> = MutableLiveData()
    var weather: MutableLiveData<OpenWeather> = MutableLiveData()

    init {
        weather = model.openWeather
    }

    fun refreshWeather() {
        if (model.location.value?.second != null) {
            Log.d(logTag, "refreshWeather() -> Fetching Weather")
            model.refreshWeather()
        } else {
            Log.d(logTag, "refreshWeather() | Location Was Null")
        }
    }

    fun locationUpdate(): MutableLiveData<Pair<String, String>> {
        return model.location
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

    fun setWeatherData(weather: MutableLiveData<OpenWeather>) {
        this.weather = weather
    }
}
