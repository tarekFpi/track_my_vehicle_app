package com.singularity.trackmyvehicle.view.map;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ocittwo on 11/14/16.
 *
 * @Author Ahmad Rosid
 * @Email ocittwo@gmail.com
 * @Github https://github.com/ar-android
 * @Web http://ahmadrosid.com
 */

public class DrawRoute extends AsyncTask<String, Void, String> {
	
	private GoogleMap                                mMap;
	private RouteDrawerTask.DistanceDurationCallback mCallback;
	
	public DrawRoute(GoogleMap mMap, RouteDrawerTask.DistanceDurationCallback callback) {
		this.mMap = mMap;
		mCallback = callback;
	}
	
	@Override
	protected String doInBackground(String... url) {
		String data = "";
		try {
			data = getJsonRoutePoint(url[0]);
			Log.d("Background Task data", data);
		} catch (Exception e) {
			FirebaseCrashlytics.getInstance().recordException(e);
			Log.d("Background Task", e.toString());
		}
		return data;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		RouteDrawerTask routeDrawerTask = new RouteDrawerTask(mMap, mCallback);
		routeDrawerTask.execute(result);
	}
	
	/**
	 * A method to download json data from url
	 */
	private String getJsonRoutePoint(String strUrl) throws IOException {
		String            data          = "";
		InputStream       iStream       = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);
			
			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();
			
			// Connecting to url
			urlConnection.connect();
			
			// Reading data from url
			iStream = urlConnection.getInputStream();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
			
			StringBuffer sb = new StringBuffer();
			
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			
			data = sb.toString();
			Log.d("getJsonRoutePoint", data.toString());
			br.close();
			
		} catch (Exception e) {
			FirebaseCrashlytics.getInstance().recordException(e);
			Log.d("Exception", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}
	
}
