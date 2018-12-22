package com.kiss.www.kweather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kiss.www.kweather.Model.Model
import com.kiss.www.kweather.Model.NewsModel.News

class NewsFragmentViewModel : ViewModel() {


    val logTag = javaClass.simpleName
    val model: Model = Model.getInstance()

    var newsList: MutableLiveData<List<News>> = MutableLiveData()

    // TODO: Implement the ViewModel

    init {
        newsList = model.newList
    }

    fun refreshNews() {
        model.refreshNews()
    }

    fun newsUpdate(): MutableLiveData<List<News>> {
        return newsList
    }
}
