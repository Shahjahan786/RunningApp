package com.shahjahan.runningapp.ui.fragments

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.shahjahan.runningapp.R
import com.shahjahan.runningapp.other.Constants
import com.shahjahan.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.shahjahan.runningapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.shahjahan.runningapp.other.TrackingUtility
import com.shahjahan.runningapp.repositories.PolyLine
import com.shahjahan.runningapp.repositories.PolyLines
import com.shahjahan.runningapp.repositories.TrackingServices
import com.shahjahan.runningapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels();
    private var map: GoogleMap? = null

    private var isTracking = false;
    private var pathPoints = mutableListOf<PolyLine>()

    private fun addAllPolyLines() {
        for (polyLine in pathPoints) {
            var polyLineOptions = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)
                .addAll(polyLine)
            map?.addPolyline(polyLineOptions)
        }

    }

    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
           map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    16f
                )
            )
        }
    }

    private fun  updateTracking(isTracking: Boolean){
        this.isTracking = isTracking;

        if(!isTracking){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        }else{
            btnToggleRun.text = "Stop"
            btnFinishRun.visibility = View.GONE
        }

    }

    private fun toggleRun(){
        if(isTracking){
            sendCommandToService(Constants.ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
        }

    }

    private fun subscribeToObservers(){
        TrackingServices.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingServices.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addAllPolyLines()
            moveCameraToUser()
        })
    }

    private fun addLatestPolyLine() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            var preLastLatLang = pathPoints.last()[pathPoints.last().size - 2]
            var lastLatLng = pathPoints.last().last()
            var polyLineOptions = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)
                .add(preLastLatLang)
                .add(lastLatLng)

            map?.addPolyline(polyLineOptions)
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        mapView.getMapAsync {
            map = it
            addAllPolyLines()
        }

        subscribeToObservers()

    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingServices::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

}