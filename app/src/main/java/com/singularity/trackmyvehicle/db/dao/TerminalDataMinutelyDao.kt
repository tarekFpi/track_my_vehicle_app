package com.singularity.trackmyvehicle.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.singularity.trackmyvehicle.model.apiResponse.v3.TerminalDataMinutely

/**
 * Created by Imran Chowdhury on 2020-02-03.
 */


@Dao
abstract class TerminalDataMinutelyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(data: List<TerminalDataMinutely>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(vararg model: TerminalDataMinutely)

    @Query("DELETE FROM terminal_data_minutely")
    abstract fun deleteAll()

    @Query("SELECT * FROM terminal_data_minutely")
    abstract fun getTerminalMinutelyData(): LiveData<List<TerminalDataMinutely>>

    @Query("SELECT * FROM terminal_data_minutely WHERE terminalAssignmentCode =  :bstId")
    abstract fun getTerminalMinutelyDataAsync(bstId: String): List<TerminalDataMinutely>

    @Query("SELECT * FROM terminal_data_minutely WHERE terminalID =  :terminalId AND terminalDataMinutelyTimeFirst >= :dateStart AND terminalDataMinutelyTimeFirst < :dateEnd")
    abstract fun getTerminalMinutelyDataByDateRangeAndId(terminalId: String, dateStart: String,
                                                         dateEnd: String): List<TerminalDataMinutely>

}