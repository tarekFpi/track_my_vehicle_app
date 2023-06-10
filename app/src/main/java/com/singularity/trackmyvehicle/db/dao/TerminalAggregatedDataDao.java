package com.singularity.trackmyvehicle.db.dao;

import com.singularity.trackmyvehicle.model.entity.TerminalAggregatedData;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Created by Sadman Sarar on 3/8/18.
 */

@Dao
public abstract class TerminalAggregatedDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(List<TerminalAggregatedData> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(TerminalAggregatedData... data);

    @Query("SELECT * FROM terminal_aggregated_data WHERE terminalDataMinutelyTimeFrom > :from AND terminalDataMinutelyTimeFrom < :to AND aggregatedDurationType = :type ORDER BY carrierRegistrationNumber DESC")
    public abstract LiveData<List<TerminalAggregatedData>> getTerminalAggregatedDataByRange(
            long from, long to, String type);

    @Query("SELECT * FROM terminal_aggregated_data WHERE terminalDataMinutelyTimeFrom > :from AND terminalDataMinutelyTimeFrom < :to AND aggregatedDurationType = :type AND terminalID = :terminalId ORDER BY carrierRegistrationNumber DESC")
    public abstract LiveData<TerminalAggregatedData> getTerminalAggregatedDataByRangeTerminal(
            long from, long to, String type, String terminalId);

    @Query("SELECT * FROM terminal_aggregated_data WHERE aggregatedDurationStartFrom >= :from AND aggregatedDurationStartFrom <= :to AND aggregatedDurationType = :type AND terminalID = :terminalId ORDER BY carrierRegistrationNumber DESC")
    public abstract List<TerminalAggregatedData> getTerminalAggregatedDataByRangeTerminalList(
            long from, long to, String type, String terminalId);

    @Query("SELECT * FROM terminal_aggregated_data WHERE aggregatedDurationStartFrom >= :from AND aggregatedDurationStartFrom <= :to AND aggregatedDurationType = :type ORDER BY carrierRegistrationNumber DESC")
    public abstract List<TerminalAggregatedData> getTerminalAggregatedDataAllByRangeTerminalList(
            long from, long to, String type);

    @Query("SELECT * FROM terminal_aggregated_data WHERE aggregatedDurationStartFrom >= :from AND aggregatedDurationStartFrom < :to AND aggregatedDurationType = :type ORDER BY carrierRegistrationNumber DESC")
    public abstract LiveData<List<TerminalAggregatedData>> getTerminalAggregatedAllDataByRangeTerminal(
            long from, long to, String type);

    @Query("SELECT * FROM terminal_aggregated_data WHERE aggregatedDurationStartFrom >= :from AND aggregatedDurationType = :type AND terminalID = :terminalId ORDER BY aggregatedDurationType ASC")
    public abstract LiveData<TerminalAggregatedData> getTerminalAggregatedDataByDayTerminal(
            long from, String type, String terminalId);

    @Query("SELECT * FROM terminal_aggregated_data WHERE terminalAssignmentCode LIKE :bstId")
    public abstract LiveData<TerminalAggregatedData> getTerminalAggregatedDataById(String bstId);

    @Query("DELETE from terminal_aggregated_data;")
    public abstract void deleteAllTerminalAggregatedData();

}