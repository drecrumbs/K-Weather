package com.kiss.www.kweather.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kiss.www.kweather.R
import kotlinx.android.synthetic.main.card_outlook_list_item.view.*

class OutlookRecyclerViewAdapter(private val data: Array<OutlookObject>) : RecyclerView.Adapter<OutlookRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(val weatherItemView: ConstraintLayout) : RecyclerView.ViewHolder(weatherItemView)

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.weatherItemView.txtDayOfWeek.text = data[position].dayOfWeek
        holder.weatherItemView.txtTemperature.text = data[position].temp
        holder.weatherItemView.txtWeatherIcon.text = data[position].icon
        holder.weatherItemView.weatherContent.setCardBackgroundColor(data[position].color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val weatherItemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_outlook_list_item, parent, false) as ConstraintLayout

        weatherItemView.txtWeatherIcon.typeface = Typeface.createFromAsset(parent.context.assets, "fonts/weather_font_regular.ttf")
        weatherItemView.txtWeatherIcon.alpha = 0.5f
        return ViewHolder(weatherItemView)
    }

}

class OutlookObject(val dayOfWeek: String, val temp: String, val icon: String, val color: Int) {
    companion object Factory {
        private val mockOutlook = mutableListOf<OutlookObject>()

        fun mockArray(context: Context, listLength: Int): Array<OutlookObject> {

            for (i in 1..listLength) {
                val mockOutlookObject = OutlookObject(generateDayOfWeek(i), generateTemperate(i), generateIcons(context, i), generateColor(i))
                mockOutlook.add(mockOutlookObject)
            }
            return mockOutlook.toTypedArray()
        }

        private fun generateDayOfWeek(num: Int): String {
            return when (num) {
                1 -> "Monday"
                2 -> "Tuesday"
                3 -> "Wednesday"
                4 -> "Thursday"
                5 -> "Friday"
                6 -> "Saturday"
                7 -> "Sunday"
                else -> "Monday"
            }
        }

        private fun generateTemperate(num: Int): String {
            return when (num) {
                1 -> "73"
                2 -> "71"
                3 -> "68"
                4 -> "84"
                5 -> "80"
                6 -> "71"
                7 -> "64"
                else -> "67"
            }
        }

        private fun generateIcons(context: Context, num: Int): String {
            return when (num) {
                1 -> context.getString(R.string.wi_night_fog)
                1 -> context.getString(R.string.wi_day_fog)
                2 -> context.getString(R.string.wi_night_snow)
                3 -> context.getString(R.string.wi_day_snow)
                4 -> context.getString(R.string.wi_night_thunderstorm)
                5 -> context.getString(R.string.wi_day_thunderstorm)
                6 -> context.getString(R.string.wi_night_rain)
                7 -> context.getString(R.string.wi_day_rain)
                else -> context.getString(R.string.wi_day_sunny)
            }
        }

        private fun generateColor(num: Int): Int {
            return when (num) {
                1 -> Color.parseColor("#28E0AE")
                1 -> Color.parseColor("#FF0090")
                2 -> Color.parseColor("#FFAE00")
                3 -> Color.parseColor("#0090FF")
                4 -> Color.parseColor("#DC0000")
                5 -> Color.parseColor("#0051FF")
                6 -> Color.parseColor("#FF0090")
                7 -> Color.parseColor("#FFAE00")
                else -> Color.parseColor("#28E0AE")
            }
        }
    }
}
