package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;
import androidx.room.Ignore;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

public class LocationResponse {
	
	@Expose
	@Nullable
	@SerializedName("place")
	public String place;
	@Expose
	@Nullable
	@SerializedName("direction")
	public String direction;
	@Expose
	@SerializedName("longitude")
	@Nullable
	public String longitude;
	@Expose
	@SerializedName("latitude")
	@Nullable
	public String latitude;


	@Ignore
	public String distance;


}
