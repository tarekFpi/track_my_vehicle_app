package com.singularity.trackmyvehicle.model.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

@Entity(tableName = "speed_alert_report",
		indices = {@Index(value = {"bstId", "date"},
				unique = true)},
		primaryKeys = {"bstId", "date"}

)
public class SpeedAlertReport {
	
	@NonNull
	public String bstId = "";
	@NonNull
	@Expose
	@SerializedName("date")
	public String date  = "";
	@Expose
	@SerializedName("speed")
	public String speed;
	@Expose
	@SerializedName("place")
	public String place;
	@Expose
	@SerializedName("longitude")
	public String longitude;
	@Expose
	@SerializedName("latitude")
	public String latitude;
}
