package com.kiss.www.kweather

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kiss.www.kweather.model.newsModel.News
import kotlinx.android.synthetic.main.fragment_news.*
import java.util.*
import kotlin.random.Random


class NewsFragment : Fragment() {

    companion object {
        fun newInstance() = NewsFragment()
    }

    private lateinit var viewModel: NewsFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    private val logTag: String? = this.javaClass.simpleName
    private var news: List<News> = ArrayList()
    private var timer: Timer = Timer()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NewsFragmentViewModel::class.java)

        viewModel.newsUpdate().observe(this, Observer { newsList ->
            Log.d(logTag, "News Update: Setting News Feed UI")
            news = newsList
            activity?.runOnUiThread{setNewsFeed(newsList)}
        })

    }

    override fun onPause() {
        super.onPause()
        Log.d(logTag, "OnPause() -> Canceling News Feed Timer")
        timer.cancel()
    }

    private fun updateNewsUI() {

        fun updateItem(newsItemTextView: TextView){
            val newsItemNumber = IntRange.random(0, news.size)
            newsItemTextView.visibility = View.GONE
            newsItemTextView.isClickable = false
            Handler().postDelayed({
                newsItemTextView.setOnClickListener { openWebsite(Uri.parse(news[newsItemNumber].link)) }
                newsItemTextView.text = format(news[newsItemNumber])
                newsItemTextView.visibility = View.VISIBLE
                newsItemTextView.isClickable = true
            },2000)
        }

        if (!news.isEmpty()) {
            val random = IntRange.random(1, 5)
            when (random) {
                1 -> {updateItem(newsItemOne)}
                2 -> {updateItem(newsItemTwo)}
                3 -> {updateItem(newsItemThree)}
                4 -> {updateItem(newsItemFour)}
                5 -> {updateItem(newsItemFive)}
            }
        }
    }

    private fun setNewsFeed(newsList: List<News>) {
        val titlesList: ArrayList<String> = ArrayList()
        val linksList: ArrayList<Uri> = ArrayList()

        repeat(5, action = {
            val randomNumer = IntRange.random(0, newsList.size)
            titlesList.add(format(newsList[randomNumer]))
            linksList.add(Uri.parse(newsList[randomNumer].link))
        })

        newsItemOne.text = titlesList[0]
        newsItemOne.setOnClickListener { openWebsite(linksList[0]) }

        newsItemTwo.text = titlesList[1]
        newsItemTwo.setOnClickListener { openWebsite(linksList[1]) }

        newsItemThree.text = titlesList[2]
        newsItemThree.setOnClickListener { openWebsite(linksList[2]) }

        newsItemFour.text = titlesList[3]
        newsItemFour.setOnClickListener { openWebsite(linksList[3]) }

        newsItemFive.text = titlesList[4]
        newsItemFive.setOnClickListener { openWebsite(linksList[4]) }

    }

    private fun openWebsite(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun format(newsItem:News):String{
        return StringBuilder()
                .append(newsItem.title)
                .append("\n")
                .append(newsItem.author)
                .toString()
    }
}

private fun IntRange.Companion.random(start: Int, end: Int): Int {
    return Random.nextInt(start, end)
}
