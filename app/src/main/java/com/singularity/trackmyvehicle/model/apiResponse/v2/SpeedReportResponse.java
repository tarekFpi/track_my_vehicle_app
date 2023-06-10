package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.singularity.trackmyvehicle.model.entity.SpeedAlertReport;
import com.singularity.trackmyvehicle.model.entity.Vehicle;

import java.util.List;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

public class SpeedReportResponse {
	
	@Expose
	@SerializedName("speed_alert")
	public List<SpeedAlertReport> speedAlert;
	@Expose
	@SerializedName("vehicle")
	public Vehicle                vehicle;
	
	
}
