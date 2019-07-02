package com.kiss.www.kweather.common

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.lang.reflect.InvocationTargetException

class HttpHelper {
    suspend fun okHttpGet(urlString: String): Response? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
                .url(urlString)
                .build()

        try {
            response = httpClient.newCall(request).execute()
            Log.i("OKHTTP", "RESPONSE -> ${response?.body.toString()}")
        } catch (ex: InvocationTargetException) {
            Log.e("OKHTTP", "Could not connect to target", ex)
        }
        response
    }

    companion object {
        private var stream: String? = null
        var response: Response? = null
        private val httpClient: OkHttpClient = OkHttpClient()
    }
}
