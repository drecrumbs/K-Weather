package com.kiss.www.kweather.Model

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import android.util.Xml
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kiss.www.kweather.Common.HttpHelper
import com.kiss.www.kweather.Common.Utils
import com.kiss.www.kweather.Model.NewsModel.News
import com.kiss.www.kweather.Model.WeatherModel.OpenWeather
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
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
    var newList: MutableLiveData<List<News>> = MutableLiveData()

    fun refreshWeather(location: Pair<String, String>) {
        val openWeatherUrlString = Utils.getOpenWeatherUrlString(location.first, location.second)
        GetWeatherAsync().execute(openWeatherUrlString)
        Log.d(logTag, "WEATHER DATA FETCH -> $openWeatherUrlString")
    }

    fun refreshNews() {
        val newsUrlString = Utils.getNewsUrlString()
        GetNewsAsync().execute(newsUrlString)
        Log.d(logTag, "NEWS DATA FETCH -> $newsUrlString")
    }

    fun refreshWeather() {
        val openWeatherUrlString = Utils.getOpenWeatherUrlString(location.value!!.first, location.value!!.second)
        GetWeatherAsync().execute(openWeatherUrlString)
        Log.d(logTag, "WEATHER DATA FETCH -> $openWeatherUrlString")
    }


    @SuppressLint("StaticFieldLeak")
    private inner class GetWeatherAsync : AsyncTask<String, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            openWeather.value = OpenWeather()
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            openWeather = if (result != null) getOpenWeatherObject(result) else openWeather
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

    @SuppressLint("StaticFieldLeak")
    private inner class GetNewsAsync : AsyncTask<String, Void, String>() {

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val inputStream = ByteArrayInputStream(result?.toByteArray(Charset.defaultCharset())) // Default is always UTF-8 on Android
            newList = if (result != null) parseFeed(inputStream) else newList
            Log.d("NewsAsyncTask", "News Update <- $result")
        }

        override fun doInBackground(vararg params: String?): String {
            val stream: String?
            val urlString = params[0]

            val http = HttpHelper()
            stream = http.getHTTPData(urlString)

            Log.i(logTag, stream ?: "stream was null")
            return stream
        }


        @Throws(XmlPullParserException::class, IOException::class)
        fun parseFeed(inputStream: InputStream): MutableLiveData<List<News>> {
            val newsList: MutableLiveData<List<News>> = MutableLiveData()
            var title: String? = null
            var link: String? = null
            var description: String? = null
            var isItem = false
            val items: ArrayList<News> = ArrayList()

            try {
                val xmlPullParser = Xml.newPullParser()
                xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                xmlPullParser.setInput(inputStream, null)
                xmlPullParser.nextTag()
                while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                    val eventType = xmlPullParser.eventType

                    val name = xmlPullParser.name ?: continue

                    if (eventType == XmlPullParser.END_TAG) {
                        if (name.equals("item", ignoreCase = true)) {
                            isItem = false
                        }
                        continue
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (name.equals("item", ignoreCase = true)) {
                            isItem = true
                            continue
                        }
                    }

                    Log.d("MyXmlParser", "Parsing name ==> $name")
                    var result = ""
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        result = xmlPullParser.text
                        xmlPullParser.nextTag()
                    }

                    if (name.equals("title", ignoreCase = true)) {
                        title = result
                    } else if (name.equals("link", ignoreCase = true)) {
                        link = result
                    } else if (name.equals("description", ignoreCase = true)) {
                        description = result
                    }

                    if (title != null && link != null && description != null) {
                        if (isItem) {
                            val item = News(title, link, description)
                            items.add(item)
                        } else {
                            Log.d(logTag, "NEWS: Do Nothing..")
//                            mFeedTitle = title
//                            mFeedLink = link
//                            mFeedDescription = description
                        }

                        title = null
                        link = null
                        description = null
                        isItem = false
                    }
                }

                newsList.value = items
                return newsList
            } finally {
                inputStream.close()
            }
        }

    }

}