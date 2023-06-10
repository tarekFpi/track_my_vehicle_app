package com.singularity.trackmyvehicle.db.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal;

import java.util.List;

/**
 * Created by Sadman Sarar on 3/8/18.
 */

@Dao
public abstract class TerminalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(List<Terminal> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(Terminal... data);

    @Query("SELECT * FROM terminals " + "ORDER BY carrierRegistrationNumber DESC")
    public abstract LiveData<List<Terminal>> getTerminal();

    @Query("SELECT * FROM terminals " + "ORDER BY carrierRegistrationNumber DESC")
    public abstract List<Terminal> getAllTerminal();

    @Query("SELECT * FROM terminals WHERE terminalAssignmentCode LIKE :bstId")
    public abstract LiveData<Terminal> getTerminalById(String bstId);

    @Nullable
    @Query("SELECT * FROM terminals WHERE terminalAssignmentCode LIKE :bstId")
    public abstract Terminal getTerminalByIdSync(String bstId);

    @Query("SELECT * FROM terminals limit :count ")
    public abstract LiveData<List<Terminal>> getTerminalWithMaxCount(int count);

    @Query("SELECT * FROM terminals "
            + "where terminalAssignmentCode =  :bstid")
    public abstract LiveData<Terminal> getByIdAsync(String bstid);

    @Nullable
    @Query("SELECT * FROM terminals "
            + "where terminalAssignmentCode =  :bstid")
    public abstract Terminal getById(String bstid);

    @Query("DELETE from terminals;")
    public abstract void deleteAllTerminal();

    @Transaction
    public void refresh(List<Terminal> data) {
        deleteAllTerminal();
        save(data);
    }

}