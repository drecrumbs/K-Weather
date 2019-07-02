package com.kiss.www.kweather.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.kiss.www.kweather.common.HttpHelper
import com.kiss.www.kweather.common.Utils
import com.kiss.www.kweather.model.newsModel.News
import com.kiss.www.kweather.model.weatherModel.OpenWeather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.nio.charset.Charset


class Model {
    companion object {
        private val mInstance: Model = Model()
        val http = HttpHelper()

        @Synchronized
        fun getInstance(): Model {
            return mInstance
        }
    }


    private val logTag = javaClass.simpleName
    var location: MutableLiveData<Pair<String, String>> = MutableLiveData(Pair("0","0"))
    var openWeather: MutableLiveData<OpenWeather> = MutableLiveData()
    var newsList: MutableLiveData<List<News>> = MutableLiveData()

    suspend fun refreshWeather(mLocation: Pair<String, String> = location.value!!) = runBlocking {
        val openWeatherUrlString = Utils.getOpenWeatherUrlString(mLocation.first, mLocation.second)
        Log.d(logTag, "WEATHER DATA FETCH -> $openWeatherUrlString")
        openWeather.value = withContext(Dispatchers.Default) { weatherFetch(openWeatherUrlString) }
    }

    suspend fun weatherFetch(urlString: String): OpenWeather = withContext(Dispatchers.IO) {
        val response = withContext(Dispatchers.Default) {
            http.okHttpGet(urlString)
        }
        OpenWeather.getOpenWeatherObject(response)
    }

    fun refreshNews() = runBlocking {
        val newsUrlString = Utils.getNewsUrlString()
        Log.d(logTag, "NEWS DATA FETCH -> $newsUrlString")

        //  newsList.value = withContext(Dispatchers.IO) {fetchNews()}
    }

    private suspend fun fetchNews(): List<News> {
        val stream: Response?
        val urlString = Utils.getNewsUrlString()
        val http = HttpHelper()

        //  stream = http.getHTTPData(urlString)
        stream = http.okHttpGet(urlString)

        val inputStream = ByteArrayInputStream(stream?.toString()?.toByteArray(Charset.defaultCharset())) // Default is always UTF-8 on Android
        return if (stream != null) News.parseFeed(inputStream) else ArrayList()
    }

}