package com.singularity.trackmyvehicle.view.map;

import android.content.res.Resources;

import com.google.android.gms.maps.model.LatLng;
import com.singularity.trackmyvehicle.R;

/**
 * Created by ocittwo on 11/14/16.
 *
 * @Author Ahmad Rosid
 * @Email ocittwo@gmail.com
 * @Github https://github.com/ar-android
 * @Web http://ahmadrosid.com
 */
public class FetchUrl {
	public static String getUrl(String apikey, LatLng origin, LatLng dest) {
		String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
		String str_dest   = "destination=" + dest.latitude + "," + dest.longitude;
		String sensor     = "sensor=false";
		String parameters = str_origin + "&" + str_dest + "&" + sensor + "&key=" + apikey;
		String output     = "json";
		return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
	}
}
