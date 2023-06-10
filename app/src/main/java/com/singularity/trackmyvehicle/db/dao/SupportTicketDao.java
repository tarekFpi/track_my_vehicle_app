package com.singularity.trackmyvehicle.db.dao;

import com.singularity.trackmyvehicle.model.apiResponse.v3.SupportTicket;

import java.util.List;

import androidx.annotation.Nullable;
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
public abstract class SupportTicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(List<SupportTicket> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(SupportTicket... data);

    @Query("SELECT * FROM support_tickets " + "ORDER BY supportRequestResponseID DESC")
    public abstract LiveData<List<SupportTicket>> getSupportTicket();

    @Query("SELECT * FROM support_tickets " + "ORDER BY supportRequestResponseID DESC")
    public abstract List<SupportTicket> getAllSupportTicket();

    @Query("SELECT * FROM support_tickets WHERE terminalAssignmentCode LIKE :bstId")
    public abstract LiveData<SupportTicket> getSupportTicketById(String bstId);

    @Query("SELECT * FROM support_tickets limit :count ")
    public abstract LiveData<List<SupportTicket>> getSupportTicketWithMaxCount(int count);

    @Query("SELECT * FROM support_tickets "
            + "where terminalAssignmentCode =  :bstid")
    public abstract LiveData<SupportTicket> getByIdAsync(String bstid);

    @Nullable
    @Query("SELECT * FROM support_tickets "
            + "where terminalAssignmentCode =  :bstid")
    public abstract SupportTicket getById(String bstid);

    @Query("DELETE from support_tickets;")
    public abstract void deleteAllSupportTicket();

    @Transaction
    public void refresh(List<SupportTicket> data) {
        deleteAllSupportTicket();
        save(data);
    }

}