package com.kiss.www.kweather

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kiss.www.kweather.model.Model
import com.kiss.www.kweather.model.newsModel.News

class NewsFragmentViewModel : ViewModel() {


    val logTag = javaClass.simpleName
    val model: Model = Model.getInstance()

    var newsList: MutableLiveData<List<News>> = model.newsList

    // TODO: Implement the ViewModel

    fun refreshNews() {
        model.refreshNews()
    }

    fun newsUpdate(): MutableLiveData<List<News>> {
        Log.d(logTag, "HERE")
        return newsList
    }
}
