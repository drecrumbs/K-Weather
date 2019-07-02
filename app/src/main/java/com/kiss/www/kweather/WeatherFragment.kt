package com.kiss.www.kweather

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kiss.www.kweather.common.Utils
import com.kiss.www.kweather.common.Utils.unixTimeStampToDateTime
import com.kiss.www.kweather.model.weatherModel.OpenWeather
import kotlinx.android.synthetic.main.card_weather_main.*
import kotlinx.coroutines.runBlocking


class WeatherFragment : Fragment() {

    companion object {
        fun newInstance() = WeatherFragment()
        private lateinit var weatherFont: Typeface
    }

    private val logTag = javaClass.simpleName
    private var isFullscreen = false
    private lateinit var viewModel: WeatherFragmentViewModel
    private var openWeather: OpenWeather = OpenWeather()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        retainInstance = true
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(WeatherFragmentViewModel::class.java)
        weatherFont = Typeface.createFromAsset(context?.assets, "fonts/weather_font_regular.ttf")

        init()

        //Register ViewModels
        viewModel.weatherUpdate().observe(this, Observer<OpenWeather> { weather ->
                openWeather = weather
                if (openWeather.weather != null) {
                    Log.d(logTag, "weatherUpdate() Obeservable -> Weather: $weather")
                    updateWeatherUI(weather)
                }

        })

//        viewModel.locationUpdate().observe(this, Observer { location ->
//            Log.i(logTag, "Location Updated: ${location.first}, ${location.second}")
//        })

        weatherContent.setOnClickListener {
            isFullscreen = !isFullscreen
            toggleFullScreenLayout()
        }
    }

    private fun init() {
        //Set TypeFaces
        txtWeatherIcon.typeface = weatherFont
        txtTempUnit.typeface = weatherFont

        txtCity.alpha = 0f
        txtDescription.alpha = 0f
        txtSunriseTime.alpha = 0f
        txtSunsetTime.alpha = 0f
        txtHumidity.alpha = 0f
        txtTemperature.alpha = 0f
        txtTempUnit.alpha = 0f
        txtWeatherIcon.alpha = 0f
        imgSunrise.alpha = 0f
        imgSunset.alpha = 0f
        weatherContent.alpha = 0f
    }

    private fun updateWeatherUI(openWeather: OpenWeather) = runBlocking {
        activity?.runOnUiThread {
            //Set Information into UI
            txtCity.text = "${openWeather.name}"
            //.text = "Last Updated: ${Utils.dateNow}"
            //txtLastUpdate.text = "Last Update: ${Utils.unixTimeStampToDateTime(openWeather.dt.toDouble(), Utils.EEE_HH_MM_A)}"
            txtDescription.text = "${openWeather.weather!![0].description}"
                    .replace(" ", "\n", true)
            txtSunriseTime.text = String.format("Sunrise: %s", unixTimeStampToDateTime(openWeather.sys!!.sunrise, Utils.HH_MM_A))
            txtSunsetTime.text = String.format("Sunset: %s", unixTimeStampToDateTime(openWeather.sys!!.sunset, Utils.HH_MM_A))
            txtHumidity.text = String.format("Humidity: %.0f%%", openWeather.main!!.humidity)
            txtTemperature.text = "${openWeather.main!!.temp.toInt()}"
            txtWeatherIcon.text = when (openWeather.weather!![0].icon!!) {
                "50n" -> getString(R.string.wi_night_fog)
                "50d" -> getString(R.string.wi_day_fog)
                "13n" -> getString(R.string.wi_night_snow)
                "13d" -> getString(R.string.wi_day_snow)
                "11n" -> getString(R.string.wi_night_thunderstorm)
                "11d" -> getString(R.string.wi_day_thunderstorm)
                "10n" -> getString(R.string.wi_night_rain)
                "10d" -> getString(R.string.wi_day_rain)
                "09n" -> getString(R.string.wi_night_showers)
                "09d" -> getString(R.string.wi_day_showers)
                "04n" -> getString(R.string.wi_forecast_io_partly_cloudy_night)
                "04d" -> getString(R.string.wi_forecast_io_partly_cloudy_day)
                "03n" -> getString(R.string.wi_night_cloudy)
                "03d" -> getString(R.string.wi_day_cloudy)
                "02n" -> getString(R.string.wi_night_alt_partly_cloudy)
                "02d" -> getString(R.string.wi_day_sunny_overcast)
                "01n" -> getString(R.string.wi_night_clear)
                "01d" -> getString(R.string.wi_day_sunny)
                else -> getString(R.string.wi_alien)
            }

            AnimationHelper.animateAlphaToShow(weatherContent, 500)
            AnimationHelper.animateAlphaToShow(txtCity, 1000)
            AnimationHelper.animateAlphaToShow(txtDescription, 1000)
            AnimationHelper.animateAlphaToShow(txtSunriseTime, 1000)
            AnimationHelper.animateAlphaToShow(txtSunsetTime, 1000)
            AnimationHelper.animateAlphaToShow(txtHumidity, 1000)
            AnimationHelper.animateAlphaToShow(txtTemperature, 1000)
            AnimationHelper.animateAlphaToShow(txtTempUnit, 1000)
            AnimationHelper.animateAlphaToShow(txtWeatherIcon, 1000)
            AnimationHelper.animateAlphaToShow(imgSunrise, 1000)
            AnimationHelper.animateAlphaToShow(imgSunset, 1000)

        }
    }

    private fun toggleFullScreenLayout() {
        if (!isFullscreen) AnimationHelper.mainWeatherCardExpandFullscreen()
        else AnimationHelper.mainWeatherCardCollapse()
    }


}
