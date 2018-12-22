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
import com.kiss.www.kweather.Common.Utils
import com.kiss.www.kweather.Common.Utils.unixTimeStampToDateTime
import com.kiss.www.kweather.Model.WeatherModel.OpenWeather
import kotlinx.android.synthetic.main.fragment_weather.*


class WeatherFragment : Fragment() {

    companion object {
        fun newInstance() = WeatherFragment()
    }

    private lateinit var viewModel: WeatherFragmentViewModel
    private var openWeather: OpenWeather = OpenWeather()

    private val localClassName = javaClass.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        retainInstance = true
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(WeatherFragmentViewModel::class.java)

        //Set TypeFaces
        txtWeatherIcon.typeface = Typeface.createFromAsset(context?.assets, "fonts/weather_font_regular.ttf")
        txtTempUnit.typeface = Typeface.createFromAsset(context?.assets, "fonts/weather_font_regular.ttf")

        viewModel.weatherUpdate().observe(this, Observer<OpenWeather> { weather ->
            run {
                openWeather = weather
                if (openWeather.weather != null) {
                    Log.d(localClassName, "weatherUpdate() Obeservable -> Weather: ${weather?.weather?.get(0)}")
                    updateWeatherUI(weather)
                }
            }
        })

        viewModel.locationUpdate().observe(this, Observer { location ->
            Log.i(localClassName, "Location Updated: ${location.first}, ${location.second}")
        })
    }

    private fun updateWeatherUI(openWeather: OpenWeather) {
        container.visibility = View.INVISIBLE
        //Set Information into UI
        txtCity.text = "${openWeather.name}"
        //.text = "Last Updated: ${Utils.dateNow}"
        // txtLastUpdate.text = "Last Update: ${Utils.unixTimeStampToDateTime(openWeather.dt.toDouble(), Utils.EEE_HH_MM_A)}"
        txtDescription.text = "${openWeather.weather!![0].description}"
                .replace(" ", "\n", true)
        txtSunriseTime.text = String.format("Sunrise: %s", unixTimeStampToDateTime(openWeather.sys!!.sunrise, Utils.HH_MM_A))
        txtSunsetTime.text = String.format("Sunset: %s", unixTimeStampToDateTime(openWeather.sys!!.sunset, Utils.HH_MM_A))
        txtHumidity.text = String.format("Humidity: %.0f%%", openWeather.main!!.humidity)
        txtTemperature.text = "${openWeather.main!!.temp.toInt()}"

        when (openWeather.weather!![0].icon!!) {
            "50n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_fog))
            "50d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_fog))
            "13n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_snow))
            "13d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_snow))
            "11n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_thunderstorm))
            "11d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_thunderstorm))
            "10n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_rain))
            "10d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_rain))
            "09n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_showers))
            "09d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_showers))
            "04n" -> my_weather_icon.setIconResource(getString(R.string.wi_forecast_io_partly_cloudy_night))
            "04d" -> my_weather_icon.setIconResource(getString(R.string.wi_forecast_io_partly_cloudy_day))
            "03n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_cloudy))
            "03d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_cloudy))
            "02n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_alt_partly_cloudy))
            "02d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_sunny_overcast))
            "01n" -> my_weather_icon.setIconResource(getString(R.string.wi_night_clear))
            "01d" -> my_weather_icon.setIconResource(getString(R.string.wi_day_sunny))

            else -> my_weather_icon.setIconResource(getString(R.string.wi_alien))
        }

        container.visibility = View.VISIBLE
    }

}
