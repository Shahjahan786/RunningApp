package com.shahjahan.runningapp.repositories

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.shahjahan.runningapp.R
import com.shahjahan.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.shahjahan.runningapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.shahjahan.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.shahjahan.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.shahjahan.runningapp.other.Constants.FASTEST_UPDATE_INTERVAL
import com.shahjahan.runningapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.shahjahan.runningapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.shahjahan.runningapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.shahjahan.runningapp.other.Constants.NOTIFICATION_ID
import com.shahjahan.runningapp.other.TrackingUtility
import com.shahjahan.runningapp.ui.MainActivity
import timber.log.Timber

typealias PolyLine = MutableList<LatLng>
typealias PolyLines = MutableList<PolyLine>

class TrackingServices : LifecycleService() {

    private var isFirstRun = true;

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTRacking(it)
        })
    }

    companion object {
        var isTracking = MutableLiveData<Boolean>()
        var pathPoints = MutableLiveData<PolyLines>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    private fun pauseService(){
        isTracking.postValue(false)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
               ACTION_START_OR_RESUME_SERVICE ->{
                   if (isFirstRun){
                       startForegroundService()
                    isFirstRun = false;
                   }else {
                       Timber.d("resumed service")
                       startForegroundService()
                   }
               }
               ACTION_PAUSE_SERVICE ->{
                   Timber.d("paused service")
                   pauseService()
               }
              ACTION_STOP_SERVICE ->{
                  Timber.d("stoped service")
              }
            }
        }

        return super.onStartCommand(intent, flags, startId)


    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTRacking(isTracking: Boolean){
        if(isTracking){
            if(TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_UPDATE_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }else{
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    val locationCallback = object : LocationCallback(){
        override fun onLocationResult(res: LocationResult?) {
            super.onLocationResult(res)
            if(isTracking.value!!){
               res?.locations?.let { locations ->
                   for (location in locations){
                       addPathPoint(location)
                       Timber.d("NEW LOCATION: ${location.latitude} , ${location.longitude}")
                   }
               }
            }
        }
    }

    private fun addPathPoint(location: Location?){
        location?.let {
            val pos = LatLng(it.latitude, it.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyLine() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService(){
        addEmptyPolyLine()
        isTracking.postValue(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())


        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )
}