package com.singularity.trackmyvehicle.db.dao;

import com.singularity.trackmyvehicle.model.entity.SupportRequestCategory;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

/**
 * Created by Sadman Sarar on 3/8/18.
 */

@Dao
public abstract class SupportTicketCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(List<SupportRequestCategory> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(SupportRequestCategory... data);

    @Query("SELECT * FROM support_request_categories " + "ORDER BY supportRequestCategoryID DESC")
    public abstract LiveData<List<SupportRequestCategory>> getSupportRequestCategory();

    @Query("SELECT * FROM support_request_categories " + "ORDER BY supportRequestCategoryID DESC")
    public abstract List<SupportRequestCategory> getAllSupportRequestCategory();

    @Query("SELECT * FROM support_request_categories limit :count ")
    public abstract LiveData<List<SupportRequestCategory>> getSupportRequestCategoryWithMaxCount(
            int count);

    @Query("DELETE from support_request_categories;")
    public abstract void deleteAllSupportRequestCategory();

    @Transaction
    public void refresh(List<SupportRequestCategory> data) {
        deleteAllSupportRequestCategory();
        save(data);
    }

}