package com.kiss.www.kweather.Common

import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

/**
 * Created by Dre on 12/24/2017.
 */

object Utils {

    private const val API_KEY = "540717bb25664e4ed48aede738fb520f"
    private const val API_LINK = "http://api.openweathermap.org/data/2.5/weather"
    const val HH_MM_A = "h:mm a"
    const val DAY_OF_WEEK_FORMAT_ = "EEE"
    const val EEE_HH_MM_A = "EEE hh:mm a"

    fun getOpenWeatherUrlString(lat: String, lon: String): String {
        var ab = StringBuilder(API_LINK)
        ab.append("?lat=${lat}&lon=${lon}&APPID=${API_KEY}&units=imperial")

        return ab.toString()
    }

    fun unixTimeStampToDateTime(unixTimeStamp: Double, format: String): String {
        val dateFormat = SimpleDateFormat(format)
        val date = Date()
        date.time = unixTimeStamp.toLong() * 1000

        return dateFormat.format(date)
    }

    fun getOpenWeatherForecastIcon(icon: String): String {
        return "http://openweathermap.org/img/w/${icon}.png"
    }

    val dayOfWeek: String?
        get() {
            val dateFormat = SimpleDateFormat(DAY_OF_WEEK_FORMAT_, Locale.US)
            val currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.now()
            } else {
                null
            }
            return if (currentDate != null)
                dateFormat.format(currentDate)
            else
                return null
        }

    val dateNow: String
        get() {
            val dateFormat = SimpleDateFormat("EEE, d MMM yyyy h:mm a")
            val date = Date()
            return dateFormat.format(date)
        }
}