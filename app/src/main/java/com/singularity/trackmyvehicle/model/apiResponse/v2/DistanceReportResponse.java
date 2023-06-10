package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.singularity.trackmyvehicle.model.entity.DistanceReport;
import com.singularity.trackmyvehicle.model.entity.Vehicle;

import java.util.List;

/**
 * Created by Sadman Sarar on 3/12/18.
 */
public class DistanceReportResponse {
	
	@Expose
	@SerializedName("distance")
	public List<DistanceReport> distance;
	@Expose
	@SerializedName("vehicle")
	public Vehicle              vehicle;
	
	
}
