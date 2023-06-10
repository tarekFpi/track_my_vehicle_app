package com.singularity.trackmyvehicle.model.entity;

import android.util.Pair;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.singularity.trackmyvehicle.utils.polylineDecoder.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

@Entity(tableName = "route_polylines",
		indices = {@Index(value = {"bstId", "date"},
				unique = true)},
		primaryKeys = {"bstId", "date"}

)
public class VehicleRoutePolyline {
	
	@NonNull
	public String bstId;
	
	@NonNull
	@Expose
	@SerializedName("date")
	public String date = "";
	
	public String polyline;

	@Ignore
	public List<Point>  latlagns = new ArrayList<>();
	
}
