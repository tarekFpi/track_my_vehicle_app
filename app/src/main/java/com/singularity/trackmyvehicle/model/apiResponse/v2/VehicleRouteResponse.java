package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.singularity.trackmyvehicle.model.entity.Vehicle;
import com.singularity.trackmyvehicle.model.entity.VehicleRoute;

import java.util.List;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

public class VehicleRouteResponse {
	
	
	@Expose
	@SerializedName("route")
	public List<VehicleRoute> route;
	@Expose
	@SerializedName("vehicle")
	public Vehicle            vehicle;
	@Expose
	@SerializedName("polyline")
	public String            polyline;
}
