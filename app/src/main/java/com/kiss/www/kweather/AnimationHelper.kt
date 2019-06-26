package com.kiss.www.kweather

import android.transition.TransitionManager
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_main.*

object AnimationHelper {
    lateinit var mainActivity: MainActivity
    val TAG = AnimationHelper.javaClass.simpleName

    fun registerMainActivity(activity: MainActivity) {
        log("Activity Registered")
        mainActivity = activity
    }

    fun hideBottomContainer() {
        log("Hide Bottom Container")
        mainActivity.outlookRecyclerView.visibility = View.GONE
        mainActivity.fifteenDayOutlookTitle.visibility = View.GONE
        // mainActivity.bottomContainer.visibility = View.GONE
    }

    fun showBottomContainer() {
        log("Show Bottom Container")
        mainActivity.outlookRecyclerView.visibility = View.VISIBLE
        mainActivity.fifteenDayOutlookTitle.visibility = View.VISIBLE
        //  mainActivity.bottomContainer.visibility = View.GONE
    }

    fun mainWeatherCardExpandFullscreen() {
        log("Main Weather Card Fullscreen")
        val constraintSet2 = ConstraintSet()
        constraintSet2.clone(mainActivity, R.layout.activity_main_today_fullscreen)

        TransitionManager.beginDelayedTransition(mainActivity.rootLayout)
        constraintSet2.applyTo(mainActivity.rootLayout)
    }

    fun mainWeatherCardCollapse() {
        log("Main Weather Card Collapse")
        val constraintSet2 = ConstraintSet()
        constraintSet2.clone(mainActivity, R.layout.activity_main)

        TransitionManager.beginDelayedTransition(mainActivity.rootLayout)
        constraintSet2.applyTo(mainActivity.rootLayout)
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }
}