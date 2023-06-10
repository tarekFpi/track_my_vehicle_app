package com.singularity.trackmyvehicle.model.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.annotation.NonNull;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

@Entity(tableName = "distance_report",
		indices = {@Index(value = {"bstId", "date"},
				unique = true)},
		primaryKeys = {"bstId", "date"}

)
public class DistanceReport {
	
	@NonNull
	public String bstId;
	
	@NonNull
	@Expose
	@SerializedName("date")
	public String date = "";
	
	@Expose
	@SerializedName("km")
	public String km;

	@Expose
	@SerializedName("TerminalAssignmentIsSuspended")
	public String TerminalAssignmentIsSuspended;

	@Expose
	@SerializedName("CarrierRegistrationNumber")
	public String vrn;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DistanceReport) {
			DistanceReport dr = (DistanceReport) obj;
			if (this.bstId.equals(dr.bstId)
					&& this.date.equals(dr.date)
					&& this.TerminalAssignmentIsSuspended.equals(dr.TerminalAssignmentIsSuspended)
					&& this.km.equals(dr.km)
			    		&& this.vrn.equals(dr.vrn)) {
				return true;
			}
		}
		return false;
	}
}
