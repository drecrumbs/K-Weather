package com.kiss.www.kweather.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.kiss.www.kweather.common.HttpHelper
import com.kiss.www.kweather.common.Utils
import com.kiss.www.kweather.model.newsModel.News
import com.kiss.www.kweather.model.weatherModel.OpenWeather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.nio.charset.Charset


class Model {
    companion object {
        private val mInstance: Model = Model()

        @Synchronized
        fun getInstance(): Model {
            return mInstance
        }
    }


    private val logTag = javaClass.simpleName
    var location: MutableLiveData<Pair<String, String>> = MutableLiveData()
    var openWeather: MutableLiveData<OpenWeather> = MutableLiveData()
    var newsList: MutableLiveData<List<News>> = MutableLiveData()

    fun refreshWeather(mLocation: Pair<String, String> = location.value!!) = runBlocking {
        val openWeatherUrlString = Utils.getOpenWeatherUrlString(mLocation.first, mLocation.second)
        openWeather.value = this.async (Dispatchers.IO) { fetchWeather(mLocation)}.await()
        Log.d(logTag, "WEATHER DATA FETCH -> $openWeatherUrlString")
    }

    fun refreshNews() = runBlocking {
        val newsUrlString = Utils.getNewsUrlString()
        newsList.value = this.async(Dispatchers.IO){ fetchNews() }.await()
        Log.d(logTag, "NEWS DATA FETCH -> $newsUrlString")
    }

    private fun fetchWeather(location: Pair<String, String>):OpenWeather {
            val stream: String?
            val urlString = Utils.getOpenWeatherUrlString(location.first, location.second)
            val http = HttpHelper()
            stream = http.getHTTPData(urlString)

            return if (stream != null) OpenWeather.getOpenWeatherObject(stream) else OpenWeather()
    }

    private fun fetchNews():List<News> {
        val stream: String?
        val urlString = Utils.getNewsUrlString()
        val http = HttpHelper()

        stream = http.getHTTPData(urlString)

        val inputStream = ByteArrayInputStream(stream.toByteArray(Charset.defaultCharset())) // Default is always UTF-8 on Android
        return if (stream != null) News.parseFeed(inputStream) else ArrayList()
    }

}