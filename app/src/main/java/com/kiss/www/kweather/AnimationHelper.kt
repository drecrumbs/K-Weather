package com.kiss.www.kweather

import android.animation.ValueAnimator
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_main.*

object AnimationHelper {
    lateinit var mainActivity: MainActivity
    private val TAG: String = AnimationHelper.javaClass.simpleName

    fun registerMainActivity(activity: MainActivity) {
        log("Activity Registered")
        mainActivity = activity
    }

    fun hideBottomContainer() {
        log("Hide Bottom Container")

        val bottomContainer = mainActivity.outlookRecyclerView
        val bottomContainerTitle = mainActivity.fifteenDayOutlookTitle
        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = 100

        animator.addUpdateListener {
            bottomContainer.alpha = it.animatedFraction
            bottomContainerTitle.alpha = it.animatedFraction

            if (it.animatedFraction == 0f) {
                bottomContainer.visibility = View.GONE
                bottomContainerTitle.visibility = View.GONE
            }
        }

        animator.start()
    }

    fun showBottomContainer() {
        log("Show Bottom Container")
        val bottomContainer = mainActivity.outlookRecyclerView
        val bottomContainerTitle = mainActivity.fifteenDayOutlookTitle
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 100
        animator.addUpdateListener {
            bottomContainer.alpha = it.animatedFraction
            bottomContainerTitle.alpha = it.animatedFraction

            if (it.animatedFraction == 0.0f) {
                bottomContainer.visibility = View.VISIBLE
                bottomContainerTitle.visibility = View.VISIBLE
            }
        }
        animator.start()
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

    fun animateAlphaToHide(view: View, duration: Long) {
        log("animateAlphaToHide: $view $duration")
        val animator = ValueAnimator.ofFloat(view.alpha, 0f)

        animator.duration = duration

        animator.addUpdateListener {
            view.alpha = it.animatedFraction
        }

        animator.start()
    }

    fun animateAlphaToShow(view: View, duration: Long) {
        log("animateAlphaToShow: $view $duration")
        val animator = ValueAnimator.ofFloat(view.alpha, 1f)
        animator.duration = duration

        animator.addUpdateListener {
            view.alpha = it.animatedFraction
        }

        animator.start()
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }
}