package com.singularity.trackmyvehicle.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.singularity.trackmyvehicle.model.entity.Feedback;
import com.singularity.trackmyvehicle.model.entity.FeedbackHeader;
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark;

import java.util.List;

/**
 * Created by Sadman Sarar on 3/8/18.
 */

@Dao
public interface FeedbackDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveFeedbackHeader(List<FeedbackHeader> data);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveFeedbackHeader(FeedbackHeader... data);
	
	@Query("SELECT * FROM feedback_headers "
			+ "ORDER BY name DESC")
	LiveData<List<FeedbackHeader>> getFeedbackHeaderAllAsync();
	
	@Query("SELECT * FROM feedback_headers")
	List<FeedbackHeader> getFeedbackHeaderAll();
	
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveFeedback(List<Feedback> data);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveFeedback(Feedback... data);
	
	@Query("SELECT * FROM feedbacks order by sl desc")
	LiveData<List<Feedback>> getFeedbackAllAsync();
	
	@Query("SELECT * FROM feedbacks")
	List<Feedback> getFeedbackAll();
	
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveFeedbackRemark(List<FeedbackRemark> data);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void saveFeedbackRemark(FeedbackRemark... data);
	
	@Query("SELECT * FROM feedback_remarks where feedbackId = :feedbackId ")
	LiveData<List<FeedbackRemark>> getFeedbackRemarkAllAsync(String feedbackId);
	
	@Query("SELECT * FROM feedback_remarks where feedbackId = :feedbackId")
	List<FeedbackRemark> getFeedbackRemarkAll(String feedbackId);
	
	@Query("delete FROM feedbacks")
	void deleteAllFeedbacks();
	
	@Query("delete FROM feedback_remarks")
	void deleteAllFeedbackRemarks();
}
