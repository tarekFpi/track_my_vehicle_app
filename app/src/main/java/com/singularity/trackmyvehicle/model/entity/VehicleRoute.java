package com.singularity.trackmyvehicle.model.entity;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.singularity.trackmyvehicle.model.apiResponse.v2.LocationResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

@Entity(tableName = "route",
		indices = {@Index(value = {"bstId", "updatedAt"},
				unique = true)},
		primaryKeys = {"bstId", "updatedAt"}

)
public class VehicleRoute {
	
	@NonNull
	public String bstId;
	
	@Expose
	@SerializedName("speed")
	public String speed;
	@Expose
	@SerializedName("engine_status")
	public String engineStatus;
	@NonNull
	@Expose
	@SerializedName("updated_at")
	public String updatedAt = "";
	@Expose
	@SerializedName("sl")
	public int              sl;
	@Embedded
	@Expose
	@SerializedName("location")
	public LocationResponse location;
	
	@Nullable
	public DateTime updatedAtDate() {
		try {
			return DateTime.parse(updatedAt, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
		} catch (Exception ex) {
			FirebaseCrashlytics.getInstance().recordException(ex);
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VehicleRoute) {
			VehicleRoute vr = (VehicleRoute) obj;
			if (this.bstId.equals(vr.bstId)
					&& this.sl == vr.sl
					&& this.updatedAt.equals(vr.updatedAt)
					&& this.speed.equals(vr.speed)) {
				return true;
			}
		}
		return false;
	}
}
