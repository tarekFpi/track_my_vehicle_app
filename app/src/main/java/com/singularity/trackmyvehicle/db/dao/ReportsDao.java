package com.singularity.trackmyvehicle.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.singularity.trackmyvehicle.model.dataModel.SpeedViolationModel;
import com.singularity.trackmyvehicle.model.entity.DistanceReport;
import com.singularity.trackmyvehicle.model.entity.Expense;
import com.singularity.trackmyvehicle.model.entity.ExpenseHeader;
import com.singularity.trackmyvehicle.model.entity.SpeedAlertReport;

import java.util.List;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

@Dao
public interface ReportsDao {
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveDistanceReport(List<DistanceReport> data);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveDistanceReport(DistanceReport... data);
	
	/**
	 * @param bstid
	 * @param date  like this "%yyyy-MM%"
	 */
	@Query("SELECT * FROM distance_report where bstid =  :bstid and date like :date")
	LiveData<List<DistanceReport>> getDistanceReportAsnyc(String bstid, String date);
	
	/**
	 * @param bstid
	 * @param date  like this "%yyyy-MM%"
	 */
	@Query("SELECT * FROM distance_report where bstid =  :bstid and date like :date")
	List<DistanceReport> getDistanceReport(String bstid, String date);
	
	/**
	 * @param bstid
	 * @param date  like this "%yyyy-MM%"
	 */
	@Query("SELECT sum(km) FROM distance_report where bstid =  :bstid and date like :date")
	LiveData<Double> getTotalDistance(String bstid, String date);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveSpeedReport(List<SpeedAlertReport> data);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveSpeedReport(SpeedAlertReport... data);
	
	/**
	 * @param bstid
	 * @param date  like this "%yyyy-MM%"
	 */
	@Query("SELECT * FROM speed_alert_report where bstid =  :bstid and date like :date")
	LiveData<List<SpeedAlertReport>> getSpeedReportAsnyc(String bstid, String date);
	
	/**
	 * @param bstid
	 * @param date  like this "%yyyy-MM%"
	 */
	@Query("SELECT * FROM speed_alert_report where bstid =  :bstid and date like :date")
	List<SpeedAlertReport> getSpeedReport(String bstid, String date);
	
	
	/**
	 * @param bstid
	 * @param startDate like this "yyyy-MM-dd 00:00:00"
	 * @param endDate   like this "yyyy-MM-dd 00:00:00"
	 */
	@Query("select date(date) as date, COUNT(*) as violations from speed_alert_report where bstid = :bstid and date between :startDate and :endDate group by date(date)")
	List<SpeedViolationModel> getSpeedViolation(String bstid, String startDate, String endDate);
	
	/**
	 * @param bstid
	 * @param date  like this "%yyyy-MM%"
	 */
	@Query("SELECT count(latitude) FROM speed_alert_report where bstid =  :bstid and date like :date")
	LiveData<Integer> getTotalSpeed(String bstid, String date);
	
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveExpenseHeader(List<ExpenseHeader> data);
	
	@Query("SELECT * FROM expense_headers")
	List<ExpenseHeader> getExpenseHeader();
	
	@Query("SELECT * FROM expense_headers")
	LiveData<List<ExpenseHeader>> getExpenseHeaderAsync();
	
	/**
	 * @param bstid
	 */
	@Query("select * from speed_alert_report where bstId = :bstid and date between :startDate and :endDate ")
	List<SpeedAlertReport> getSpeedViolationReports(String bstid, String startDate, String endDate);
	
	
	/**
	 * Expense
	 */
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveExpense(List<Expense> data);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveExpense(Expense... data);
	
	/**
	 * @param bstid
	 */
	@Query("SELECT * FROM expenses where bstid =  :bstid and date like :date")
	LiveData<List<Expense>> getExpenseAsync(String bstid, String date);
	
	/**
	 * @param bstid
	 * @param date  -> yyyy-MM
	 */
	@Query("SELECT * FROM expenses where bstid =  :bstid and date like :date")
	List<Expense> getExpense(String bstid, String date);
	
	
	@Query("delete from speed_alert_report;")
	void deleteAllSpeedAlert();
	
	@Query("delete from distance_report;")
	void deleteAllDistanceReport();
	
	@Query("delete from expenses;")
	void deleteAllExpense();
}
