package com.kiss.www.kweather.Common

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Dre on 12/24/2017.
 */

object Common{
    var API_KEY = "540717bb25664e4ed48aede738fb520f"
    var API_LINK = "http://api.openweathermap.org/data/2.5/weather"

    fun apiRequest(lat:String, lon:String):String{
        var ab = StringBuilder(API_LINK)
        ab.append("?lat=${lat}&lon=${lon}&APPID=${API_KEY}&units=imperial")

        return ab.toString()
    }

    fun unixTimeStampToDateTime(unixTimeStamp:Double):String{
        val dateFormat = SimpleDateFormat("h:mm a")
        val date = Date()
        date.time = unixTimeStamp.toLong()*1000

        return dateFormat.format(date)
    }

    fun getImage(icon:String):String{
        return "http://openweathermap.org/img/w/${icon}.png"
    }

    val dateNow:String
        get(){
            val dateFormat = SimpleDateFormat("EEE, d MMM yyyy h:mm a")
            val date = Date()
            return dateFormat.format(date)
        }

}