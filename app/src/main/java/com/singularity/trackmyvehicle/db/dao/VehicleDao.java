package com.singularity.trackmyvehicle.db.dao;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.annotation.NonNull;

import com.singularity.trackmyvehicle.model.entity.Vehicle;
import com.singularity.trackmyvehicle.model.entity.VehicleRoute;
import com.singularity.trackmyvehicle.model.entity.VehicleRoutePolyline;

import java.util.List;

/**
 * Created by Sadman Sarar on 3/8/18.
 */

@Dao
public interface VehicleDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void save(List<Vehicle> data);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void save(Vehicle... data);
	
	@Query("SELECT * FROM vehicle "
	    + "ORDER BY vrn DESC")
	LiveData<List<Vehicle>> getVehicle();

	@Query("SELECT * FROM vehicle "
			+ "ORDER BY vrn DESC")
	List<Vehicle> getVehicleSync();
	
	@Query("SELECT * FROM vehicle limit :count ")
	LiveData<List<Vehicle>> getVehicleWithMaxCount(int count);
	
	@Query("SELECT * FROM vehicle "
	    + "where bstid =  :bstid")
	LiveData<Vehicle> getByIdAsync(String bstid);
	
	@Nullable
	@Query("SELECT * FROM vehicle "
	    + "where bstid =  :bstid")
	Vehicle getById(String bstid);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveRoute(List<VehicleRoute> data);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveRoute(VehicleRoute... data);
	
	@Query("SELECT * FROM route "
	    + "where bstid =  :bstid and updatedAt like :date")
	LiveData<List<VehicleRoute>> getRoutesAsnyc(String bstid, String date);
	
	@Query("SELECT * FROM route where bstid =  :bstid and updatedAt like :date")
	List<VehicleRoute> getRoutes(String bstid, String date);
	
	@Query("Delete  FROM route; ")
	void deleteAllRoute();
	
	@Query("DELETE from vehicle;")
	void deleteAllVehicle();
	
	//Vehicle Route Polyline
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void save(VehicleRoutePolyline... data);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void savePolylines(List<VehicleRoutePolyline> data);
	
	@Delete
	void delete(VehicleRoutePolyline... data);
	
	@NonNull
	@Query("Select * from route_polylines where bstId = :bstId and date = :date")
	VehicleRoutePolyline getRoutePolyline(String bstId, String date);
}