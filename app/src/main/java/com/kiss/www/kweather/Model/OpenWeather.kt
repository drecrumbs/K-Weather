package com.kiss.www.kweather.Model

/**
 * Created by Dre on 12/24/2017.
 */

class OpenWeather{

    var coord:Coord?=null
    var weather:List<Weather>?=null
    var base:String?=null
    var main:Main?=null
    var visibility:Int?=null
    var wind:Wind?=null
    var clouds:Clouds?=null
    var rain:Rain?=null
    var dt:Int=0
    var sys:Sys?=null
    var id:Int=0
    var name:String?=null
    var cod:Int=0

    constructor()
    constructor(coord:Coord, weather:List<Weather>, base:String, main:Main, visibility:Int, wind:Wind, clouds:Clouds, rain:Rain, dt:Int, sys:Sys, id:Int, name:String, cod:Int)
    {
        this.coord = coord
        this.weather = weather
        this.base = base
        this.main = main
        this.visibility = visibility
        this.wind = wind
        this.clouds = clouds
        this.rain = rain
        this.dt = dt
        this.sys = sys
        this.id = id
        this.name = name
        this.cod = cod
    }
}