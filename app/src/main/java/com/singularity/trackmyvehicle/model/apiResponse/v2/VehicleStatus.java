package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import androidx.annotation.Nullable;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

public class VehicleStatus {
	@Expose
	/*@Expose is used to decide whether the variable will be exposed for Serialisation and Deserialisation ,or not.*/
	@SerializedName("speed")
	public String           speed;
	@Expose
	@SerializedName("engine_status")
	public String           engineStatus;
	@Expose
	@SerializedName("location")
	public LocationResponse location;

	@Expose
	@SerializedName("updated_at")
	public String           updatedAt;
	@Expose
	@SerializedName("vrn")
	public String           vrn;
	@Expose
	@SerializedName("bstid")
	public String           bstid;
	@Expose
	@SerializedName("bid")
	public int   bid;




	@Nullable
	public DateTime updateAtTime() {
		try {
			return DateTime.parse(updatedAt,
					DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VehicleStatus) {
			VehicleStatus vs = (VehicleStatus) obj;
			return this.bid == vs.bid
					&& this.bstid.equals(vs.bstid)
					&& this.engineStatus.equals(vs.engineStatus)
					&& this.updatedAt.equals(vs.updatedAt)
					&& this.speed.equals(vs.speed)
					&& this.vrn.equals(vs.vrn);
		}
		return false;
	}
}
