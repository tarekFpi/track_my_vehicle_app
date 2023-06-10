package com.singularity.trackmyvehicle.view.map;

import android.os.AsyncTask;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.singularity.trackmyvehicle.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlin.Triple;

/**
 * Created by ocittwo on 11/14/16.
 *
 * @Author Ahmad Rosid
 * @Email ocittwo@gmail.com
 * @Github https://github.com/ar-android
 * @Web http://ahmadrosid.com
 */
public class RouteDrawerTask
		extends AsyncTask<String, Integer, Triple<List<List<HashMap<String, String>>>, Integer, Integer>> {
	
	private final DistanceDurationCallback mCallback;
	private       PolylineOptions          lineOptions;
	private       GoogleMap                mMap;
	private       int                      routeColor;
	
	public RouteDrawerTask(GoogleMap mMap, DistanceDurationCallback callback) {
		this.mMap = mMap;
		this.mCallback = callback;
	}
	
	@Override
	protected Triple<List<List<HashMap<String, String>>>, Integer, Integer> doInBackground(String... jsonData) {
		JSONObject jObject;
		
		Triple<List<List<HashMap<String, String>>>, Integer, Integer> routes = null;
		
		try {
			jObject = new JSONObject(jsonData[0]);
			Log.d("RouteDrawerTask", jsonData[0]);
			DataRouteParser parser = new DataRouteParser();
			Log.d("RouteDrawerTask", parser.toString());
			
			// Starts parsing data
			routes = parser.parse(jObject);
			Log.d("RouteDrawerTask", "Executing routes");
			Log.d("RouteDrawerTask", routes.toString());
			
		} catch (Exception e) {
			Log.d("RouteDrawerTask", e.toString());
			e.printStackTrace();
			FirebaseCrashlytics.getInstance().recordException(e);
		}
		return routes;
	}
	
	@Override
	protected void onPostExecute(Triple<List<List<HashMap<String, String>>>, Integer, Integer> result) {
		if (result != null) {
			drawPolyLine(result.getFirst());
			mCallback.onData(result.getSecond(), result.getThird());
		}
	}
	
	private void drawPolyLine(List<List<HashMap<String, String>>> result) {
		ArrayList<LatLng> points;
		lineOptions = null;
		
		for (int i = 0; i < result.size(); i++) {
			points = new ArrayList<>();
			lineOptions = new PolylineOptions();
			
			// Fetching i-th route
			List<HashMap<String, String>> path = result.get(i);
			
			// Fetching all the points in i-th route
			for (int j = 0; j < path.size(); j++) {
				HashMap<String, String> point = path.get(j);
				
				double lat      = Double.parseDouble(point.get("lat"));
				double lng      = Double.parseDouble(point.get("lng"));
				LatLng position = new LatLng(lat, lng);
				
				points.add(position);
			}
			
			// Adding all the points in the route to LineOptions
			lineOptions.addAll(points);
			lineOptions.width(20);
			routeColor = ContextCompat.getColor(DrawRouteMaps.getContext(), R.color.colorRouteLine);
			if (routeColor == 0) {
				lineOptions.color(0xFF0A8F08);
			} else {
				lineOptions.color(routeColor);
			}
			lineOptions.zIndex(Float.MAX_VALUE);
		}
		
		// Drawing polyline in the Google Map for the i-th route
		if (lineOptions != null && mMap != null) {
			mMap.addPolyline(lineOptions);
		} else {
			Log.d("onPostExecute", "without Polylines draw");
		}
	}
	
	
	public interface DistanceDurationCallback {
		void onData(int distance, int duration);
	}
	
}
