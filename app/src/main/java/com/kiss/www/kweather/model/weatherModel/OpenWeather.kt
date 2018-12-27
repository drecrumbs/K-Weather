package com.kiss.www.kweather.model.weatherModel

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
        fun getOpenWeatherObject(vararg json:String): OpenWeather {
            var openWeather = OpenWeather()
            if (json.contains("Error: Not found city")) {
                Log.e(this.javaClass.simpleName, "City Was Not Found!")
                return openWeather
            }

            val gson = Gson()
            val mType = object : TypeToken<OpenWeather>() {}.type
            openWeather = gson.fromJson<OpenWeather>(json[0], mType)
            return openWeather
        }
    }
}