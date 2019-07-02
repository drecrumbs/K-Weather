package com.kiss.www.kweather.model.weatherModel

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response

/**
 * Created by Dre on 12/24/2017.
 */

class OpenWeather(var coord: Coord? = null,
                  var weather: List<Weather>? = null,
                  var base: String? = null,
                  var main: Main? = null,
                  var visibility: Int? = null,
                  var wind: Wind? = null,
                  var clouds: Clouds? = null,
                  var rain: Rain? = null,
                  var dt: Int = 0,
                  var sys: Sys? = null,
                  var id: Int = 0,
                  var name: String? = null,
                  var cod: Int = 0) {

    companion object {
        private const val TAG = "OpenWeather"
        private val gson = Gson()

        suspend fun getOpenWeatherObject(response: Response?): OpenWeather = withContext(Dispatchers.Default) {
            Log.i(TAG, "NEW WEATHER OBJECT -> ${response?.body}")
            gson.fromJson(response?.body?.charStream(), OpenWeather::class.java)
        }
    }
}