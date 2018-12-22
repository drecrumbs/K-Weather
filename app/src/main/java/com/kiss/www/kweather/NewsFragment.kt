package com.kiss.www.kweather

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kiss.www.kweather.Model.NewsModel.News
import kotlinx.android.synthetic.main.fragment_news.*
import java.util.*
import kotlin.concurrent.timerTask
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
            news = newsList
            activity!!.runOnUiThread {
                Log.d(logTag, "Setting News Feed..")
                setNewsFeed(news)
            }
        })

    }

    override fun onResume() {
        super.onResume()
        timer = Timer("newsFeed", false)
        timer.scheduleAtFixedRate(
                timerTask {
                    Log.d(logTag, "TIMER: Updating News Feed...")
                    activity!!.runOnUiThread {
                        updateNewsFeed()
                    }
                },
                10000,
                10000)
    }

    override fun onPause() {
        super.onPause()
        Log.d(logTag, "OnPause() -> Canceling News Feed Timer")
        timer.cancel()
    }

    private fun updateNewsFeed() {
        if (!news.isEmpty()) {
            val random = IntRange.random(1, 5)
            val randomNewsItem = IntRange.random(0, news.size)
            when (random) {
                1 -> {
                    newsItemOne.visibility = View.GONE
                    newsItemOne.text = news[randomNewsItem].title
                    newsItemOne.setOnClickListener { openWebsite(Uri.parse(news[randomNewsItem].link)) }
                }
                2 -> {
                    newsItemTwo.visibility = View.GONE
                    newsItemTwo.text = news[randomNewsItem].title
                    newsItemTwo.setOnClickListener { openWebsite(Uri.parse(news[randomNewsItem].link)) }
                }
                3 -> {
                    newsItemThree.visibility = View.GONE
                    newsItemThree.text = news[randomNewsItem].title
                    newsItemThree.setOnClickListener { openWebsite(Uri.parse(news[randomNewsItem].link)) }
                }
                4 -> {
                    newsItemFour.visibility = View.GONE
                    newsItemFour.text = news[randomNewsItem].title
                    newsItemFour.setOnClickListener { openWebsite(Uri.parse(news[randomNewsItem].link)) }
                }
                5 -> {
                    newsItemFive.visibility = View.GONE
                    newsItemFive.text = news[randomNewsItem].title
                    newsItemFive.setOnClickListener { openWebsite(Uri.parse(news[randomNewsItem].link)) }
                }
            }

            newsItemOne.visibility = View.VISIBLE
            newsItemTwo.visibility = View.VISIBLE
            newsItemThree.visibility = View.VISIBLE
            newsItemFour.visibility = View.VISIBLE
            newsItemFive.visibility = View.VISIBLE

        }
    }

    private fun setNewsFeed(newsList: List<News>) {
        val titlesList: ArrayList<String> = ArrayList()
        val linksList: ArrayList<Uri> = ArrayList()

        repeat(5, action = {
            val randomNumer = IntRange.random(0, newsList.size)
            titlesList.add(newsList[randomNumer].title)
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

}

private fun IntRange.Companion.random(start: Int, end: Int): Int {
    return Random.nextInt(start, end)
}
