package com.kiss.www.kweather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kiss.www.kweather.model.Model
import com.kiss.www.kweather.model.newsModel.News

class NewsFragmentViewModel : ViewModel() {


    val logTag = javaClass.simpleName
    val model: Model = Model.getInstance()

    var newsList: MutableLiveData<List<News>> = model.newsList

    fun refreshNews() {
        model.refreshNews()
    }

    fun newsUpdate(): MutableLiveData<List<News>> {
        return newsList
    }
}
