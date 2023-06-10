package com.singularity.trackmyvehicle.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.singularity.trackmyvehicle.model.entity.Notification;

import java.util.List;

/**
 * Created by Sadman Sarar on 3/8/18.
 */

@Dao
public abstract class NotificationDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	public abstract void save(List<Notification> data);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	public abstract void save(Notification... data);
	
	@Query("SELECT * FROM notifications " + "ORDER BY id DESC")
	public abstract LiveData<List<Notification>> getNotification();
	
	@Query("SELECT * FROM notifications WHERE subject LIKE :query OR message LIKE :query ORDER BY id DESC")
	public abstract List<Notification> getAllNotification(String query);
	
	
	@Query("SELECT * FROM notifications limit :count ")
	public abstract LiveData<List<Notification>> getNotificationWithMaxCount(int count);
	
	@Query("DELETE from notifications;")
	public abstract void deleteAllNotification();
	
	@Transaction
	public void refresh(List<Notification> data) {
		deleteAllNotification();
		save(data);
	}
	
}