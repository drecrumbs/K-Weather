package com.kiss.www.kweather.Model

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kiss.www.kweather.Common.HttpHelper
import com.kiss.www.kweather.Common.Utils
import com.kiss.www.kweather.Model.WeatherModel.OpenWeather

class Model {
    private var myInstancesCount = 0

    init {
        /*
        *  every time init is called increment instance count
        *  just in case somehow we break singleton rule, this will be
        *  called more than once and myInstancesCount > 1 == true
        */
        ++myInstancesCount
    }


    companion object {
        //Debuggable field to check instance count
        var myInstancesCount = 0
        private val mInstance: Model = Model()

        @Synchronized
        fun getInstance(): Model {
            return mInstance
        }
    }


    private val logTag = javaClass.simpleName
    var location: MutableLiveData<Pair<String, String>> = MutableLiveData()
    var openWeather: MutableLiveData<OpenWeather> = MutableLiveData()

    fun refreshWeather(location: Pair<String, String>) {
        val openWeatherUrlString = Utils.getOpenWeatherUrlString(location.first, location.second)
        GetWeatherAsync().execute(openWeatherUrlString)
        Log.d(logTag, "WEATHER URL -> $openWeatherUrlString")
    }

    fun refreshWeather() {
        val openWeatherUrlString = Utils.getOpenWeatherUrlString(location.value!!.first, location.value!!.second)
        GetWeatherAsync().execute(openWeatherUrlString)
        Log.d(logTag, "WEATHER URL -> $openWeatherUrlString")
    }


    private inner class GetWeatherAsync : AsyncTask<String, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            openWeather.value = OpenWeather()
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            openWeather = if (result != null) getOpenWeatherObject(result) else openWeather
            // weatherActivityModel.setWeatherData(openWeather)
            Log.d("WeatherAsyncTask", "Weather Update <- ${openWeather.value?.name}")
        }

        override fun doInBackground(vararg params: String?): String {
            val stream: String?
            val urlString = params[0]

            val http = HttpHelper()
            stream = http.getHTTPData(urlString)
            return stream
        }

        fun getOpenWeatherObject(vararg json: String): MutableLiveData<OpenWeather> {
            if (json.contains("Error: Not found city")) {
                Log.e(this.javaClass.simpleName, "City Was Not Found!")
                return openWeather
            }

            val gson = Gson()
            val mType = object : TypeToken<OpenWeather>() {}.type
            openWeather.value = gson.fromJson<OpenWeather>(json[0], mType)
            return openWeather
        }

    }

}