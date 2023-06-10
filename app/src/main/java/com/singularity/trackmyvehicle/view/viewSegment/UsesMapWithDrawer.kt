package com.singularity.trackmyvehicle.view.viewSegment

import android.app.Activity
import androidx.fragment.app.FragmentManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.SupportMapFragment
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.view.map.MapDrawer

interface UsesMapWithDrawer {

//    fun getSupportFragmentManager(): FragmentManager
//    fun getActivity(): Activity
    var mMapDrawer: MapDrawer?
    var mPrefRepository: PrefRepository
    var mVehicleRepository: VehicleRepository

    fun setUpMap(activity: Activity, supportFragmentManager: FragmentManager) {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if (isGooglePlayServicesAvailable(activity)) {
            mMapDrawer = MapDrawer(mapFragment, mVehicleRepository, mPrefRepository)
            mapFragment.getMapAsync(mMapDrawer)
        }
    }

    fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance();
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404)?.show();
            }
            return false;
        }
        return true;
    }
}