package com.kiss.www.kweather

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.kiss.www.kweather.model.weatherModel.OpenWeather
import com.ramotion.circlemenu.CircleMenuView
import com.snapchat.kit.sdk.Bitmoji
import com.snapchat.kit.sdk.bitmoji.OnBitmojiSelectedListener
import com.snapchat.kit.sdk.bitmoji.networking.FetchAvatarUrlCallback
import com.snapchat.kit.sdk.bitmoji.ui.BitmojiFragment
import com.snapchat.kit.sdk.core.controller.LoginStateController
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_weather.*
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.absoluteValue

class MainActivity : FragmentActivity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SwipeRefreshLayout.OnRefreshListener,
        LoginStateController.OnLoginStateChangedListener,
        OnBitmojiSelectedListener,
        ViewPager.OnPageChangeListener{


    object Constants {
        const val PERMISSIONS_REQUEST_CODE = 1001
        const val PLAY_SERVICE_RESOLUTION_REQUEST = 1000
        const val LOCATION_INTERVAL: Long = 36000000 // 10 * 1000 * 60 * 60 // Every Hour
        const val DATA_RETRIEVAL_INTERVAL: Long = LOCATION_INTERVAL / 2
        const val SHARED_PREFERENCES = "com.kiss.www.preferences"
        const val NUM_PAGES = 2
    }

    //Variables
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest = LocationRequest()
    private var mLocationCallback: LocationCallback? = null
    private var bitmojiUrl: String? = null
    private var timer: Timer = Timer()

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        // Get Stored Bitmoji URL todo (1) Cache the image instead of doing a fetch on startup
        bitmojiUrl = getSharedPreference(Constants.SHARED_PREFERENCES, null)

        //Initialization
        updateBitmojiAvatar()
        requestPermissions()
        observeViewModel()

        imgWeatherIcon.setOnClickListener { showBitmojiSelector() }
        layoutSwipeRefresh.setOnRefreshListener(this)
        circleMenu.eventListener = EventListener()

        //todo Make a "Home" loading fragment. Change background once done loading
        val colorArray = resources.getIntArray(R.array.colors)
        changeBackground(viewModel.currentBackgroundColor, colorArray[0])

        val pagerAdapter = ScreenSlidePageAdapter(supportFragmentManager)
        contentViewPager.adapter = pagerAdapter
        contentViewPager.setPageTransformer(false, ZoomOutPageTransformer())
        contentViewPager.addOnPageChangeListener(this)
    }

    //Activity Lifecycle and UI

    override fun onDestroy() {
        mGoogleApiClient?.disconnect()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        Log.d(localClassName, "Creating DataFetch Timer")
        timer = Timer("DataFetch", false)
        timer.scheduleAtFixedRate(timerTask {
            viewModel.refreshNews()
            viewModel.refreshWeather()
        },
        Constants.DATA_RETRIEVAL_INTERVAL,
        Constants.DATA_RETRIEVAL_INTERVAL)
        bitmojiUrl = getSharedPreferences("kweather", Context.MODE_PRIVATE)
                .getString("bitmojiUrl", null)

        restoreUI()
    }

    override fun onPause() {
        super.onPause()
        Log.d(localClassName, "Canceling DataFetch Timer")
        timer.cancel()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) restoreUI()
    }

    override fun onBackPressed() {
        when {
            bitmojiSelectorIsShowing -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.bottomContainer, androidx.fragment.app.Fragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        .commit()
                bitmojiSelectorIsShowing = false

            }

            contentViewPager.currentItem != 0 -> {
                contentViewPager.setCurrentItem(contentViewPager.currentItem - 1, true)
            }

            else -> finish()
        }
    }

    private fun observeViewModel() {
        viewModel.weatherUpdate().observe(this, Observer<OpenWeather> { weather ->
            run {
                layoutSwipeRefresh.isRefreshing = false
            }
        })

        viewModel.locationCallback().observe(this, Observer { locationCallback ->
            run {
                Log.d(localClassName, "locationCallback() Observable")
                mLocationCallback = locationCallback
            }
        })

        viewModel.googleApiClient().observe(this, Observer { googleApiClient ->
            run {
                Log.d(localClassName, "googleApiClient() Observable -> $googleApiClient")
                mGoogleApiClient = googleApiClient
                connectGoogleApiClient()
            }
        })

        viewModel.bitmojiUrlUpdate().observe(this, Observer { url ->
            bitmojiUrl = url
            updateBitmojiAvatar()
        })
    }

    private fun connectGoogleApiClient() {
        if (!mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.registerConnectionCallbacks(this)
            mGoogleApiClient!!.connect()
        }
    }

    private fun restoreUI() {

        rootLayout.setBackgroundColor(viewModel.currentBackgroundColor)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    //Swipe to Refresh

    override fun onRefresh() {
        Log.d(localClassName, "OnRefresh()")
        if (mGoogleApiClient != null) {
            createLocationRequest()
            layoutSwipeRefresh.isRefreshing = false
        }
    }

    //Bitmoji

    private var bitmojiSelectorIsShowing: Boolean = false

    private fun showBitmojiSelector() {
        runOnUiThread {
            val bitmojiFragment = BitmojiFragment()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.bottomContainer, bitmojiFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            bitmojiSelectorIsShowing = true
        }
    }

    private fun hideBitmojiSelector() {
        runOnUiThread {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.bottomContainer, androidx.fragment.app.Fragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit()
            bitmojiSelectorIsShowing = false
        }
    }

    private fun updateBitmojiAvatar() {
        Log.d("updateBitmojiAvatar", "BITMOJI URL -> ${bitmojiUrl}")
        if (bitmojiUrl == null) {
            Bitmoji.fetchAvatarUrl(this, object : FetchAvatarUrlCallback {
                override fun onSuccess(url: String?) {
                    bitmojiUrl = url
                    Log.d("BITMOJI", url)

                    runOnUiThread {
                        val picasso = Picasso.get()
                        picasso.setIndicatorsEnabled(true)
                        picasso
                                .load("${url}")
                                .resize(300, 300)
                                .centerCrop()
                                .into(imgWeatherIcon as ImageView)
                    }
                }

                override fun onFailure(p0: Boolean, p1: Int) {
                    Toast.makeText(applicationContext, "Failed to load bitmoji", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Picasso.get()
                    .load("${bitmojiUrl}")
                    .fit()
                    .into(imgWeatherIcon as ImageView)
        }
    }

    override fun onLogout() {
        Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show()
    }

    override fun onLoginFailed() {
        Toast.makeText(this, "Logout Failed", Toast.LENGTH_SHORT).show()
    }

    override fun onLoginSucceeded() {
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
    }

    override fun onBitmojiSelected(p0: String?, p1: Drawable?) {
        viewModel.bitmojiURL.value = p0
        saveSharedPreference(Constants.SHARED_PREFERENCES, "$bitmojiUrl")
        hideBitmojiSelector()
    }

    // Permissions & Google Play Services

    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Constants.PERMISSIONS_REQUEST_CODE)
        } else if (hasGooglePlayServices()) {
            buildGoogleApiClient()
        } else {
            Toast.makeText(this, "Uhh.. No access for you. Accept my permissions!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED } && hasGooglePlayServices()) {
                    buildGoogleApiClient()
                } else {
                    Toast.makeText(this, "Uhh.. No access for you. Accept my permissions!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun buildGoogleApiClient() {
        viewModel.googleApiClient.value =
                if (mGoogleApiClient == null) {
                    GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener { this }
                            .addApi(LocationServices.API)
                            .build()
                } else {
                    mGoogleApiClient
                }
    }

    private fun hasGooglePlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, Constants.PLAY_SERVICE_RESOLUTION_REQUEST).show()
            } else {
                Toast.makeText(applicationContext, "This device is not supported.", Toast.LENGTH_SHORT).show()
            }
            return false
        }
        return true
    }

    // Location

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("ERROR", "Connection failed: " + p0.errorCode)
    }

    override fun onConnected(p0: Bundle?) {
        Log.d(localClassName, "Creating Location Request")
        createLocationRequest()
    }

    private fun createLocationRequest() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mLocationRequest.interval = Constants.LOCATION_INTERVAL
        mLocationRequest.fastestInterval = Constants.LOCATION_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient!!.connect()
    }

    // Convenience Methods

    private fun changeBackground(fromColor: Int, toColor: Int) {
        val colorAnimator = ValueAnimator.ofArgb(fromColor, toColor)
        colorAnimator.duration = 1000
        colorAnimator.addUpdateListener { animator ->
            run {
                rootLayout.setBackgroundColor(animator.animatedValue as Int)
            }

        }
        colorAnimator.start()
    }

    private fun saveSharedPreference(key: String, value: String) {
        getSharedPreferences("kweather", Context.MODE_PRIVATE)
                .edit()
                .putString(key, "$value")
                .apply()
    }

    private fun getSharedPreference(key: String, defaultValue: String?): String? {
        return getSharedPreferences("kweather", Context.MODE_PRIVATE)
                .getString("$key", "${(defaultValue)}")
    }

    // ViewPager Scroll Listener

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        val colorArray = resources.getIntArray(R.array.colors)
        val toColor = colorArray[position]

        val fromColor =
                if (rootLayout.background is ColorDrawable)
                    (rootLayout.background as ColorDrawable).color
                else
                    Color.BLACK

        viewModel.currentBackgroundColor = toColor
        changeBackground(fromColor, toColor)
    }

    // Additional Classes

    private inner class ScreenSlidePageAdapter(supportFragmentManager: FragmentManager?) : FragmentStatePagerAdapter(supportFragmentManager) {
        override fun getItem(position: Int): Fragment {
            Log.d("GET ITEM", position.toString())
            when (position) {
                0 ->  // Weather
                    return WeatherFragment.newInstance()
                1 -> // News
                    return NewsFragment.newInstance()
            }
            Log.d("GET ITEM", "WTF")
            return Fragment()
        }


        override fun getCount(): Int = Constants.NUM_PAGES
    }

    private inner class ZoomOutPageTransformer : ViewPager.PageTransformer {
        private val minScale = 0.85f
        private val minAlpha = 0.5f

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    position.absoluteValue <= 1 -> { // [-1,1] Modify the default slide transition to shrink the page as well
                        val scaleFactor = Math.max(minScale, 1 - Math.abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            horzMargin + vertMargin / 2
                        }

                        // Scale the page down (between minScale and 1)
                        scaleX = scaleFactor
                        scaleY = scaleFactor

                        // Fade the page relative to its size.
                        alpha = (minAlpha +
                                (((scaleFactor - minScale) / (1 - minScale)) * (1 - minAlpha)))
                    }
                    else -> { alpha = 0f /* (-Infinity ,+Infinity) This page is way off-screen.*/ }
                }
            }
        }
    }

    private inner class EventListener : CircleMenuView.EventListener() {
        private var prevButtonIndex: Int = 0
        override fun onButtonClickAnimationStart(view: CircleMenuView, buttonIndex: Int) {
            Log.d(javaClass.simpleName, "Button Click: ${view.id}")
            super.onButtonLongClickAnimationStart(view, buttonIndex)
            val colorArray = resources.getIntArray(R.array.colors)
            val toColor = colorArray[buttonIndex]

            val fromColor =
                    if (rootLayout.background is ColorDrawable)
                        (rootLayout.background as ColorDrawable).color
                    else
                        Color.BLACK

            if (prevButtonIndex != buttonIndex) {
                when (buttonIndex) {
                    0 ->  // Weather
                        contentViewPager.setCurrentItem(0, true)

                    1 -> // News
                        contentViewPager.setCurrentItem(1, true)
                    else -> return
                }

                prevButtonIndex = buttonIndex
                viewModel.currentBackgroundColor = toColor
                changeBackground(fromColor, toColor)
            }
        }
    }
}


