package com.kiss.www.kweather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.kiss.www.kweather.model.Model
import com.kiss.www.kweather.model.weatherModel.OpenWeather

class WeatherFragmentViewModel : ViewModel() {
    val logTag = javaClass.simpleName

    val model: Model = Model.getInstance()

    val googleApiClient: MutableLiveData<GoogleApiClient> = MutableLiveData()

    var bitmojiURL: MutableLiveData<String> = MutableLiveData()
    var locationCallback: MutableLiveData<LocationCallback> = MutableLiveData()
    var weather: MutableLiveData<OpenWeather> = MutableLiveData()

    init {
        weather = model.openWeather
    }

    fun locationUpdate(): MutableLiveData<Pair<String, String>> {
        return model.location
    }

    fun weatherUpdate(): MutableLiveData<OpenWeather> {
        return weather
    }
}
