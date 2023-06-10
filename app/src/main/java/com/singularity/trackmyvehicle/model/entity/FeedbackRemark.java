package com.singularity.trackmyvehicle.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "feedback_remarks")
public class FeedbackRemark {
	
	
	@Expose
	@SerializedName("update_on")
	public String updateOn = "";
	@Expose
	@SerializedName("update_by")
	public String updateBy;
	@Expose
	@SerializedName("remarks")
	public String remarks;
	@PrimaryKey
	@NotNull
	@Expose
	@SerializedName("remarks_id")
	public String remarksId;
	@NonNull
	@Expose
	@SerializedName("feedback_id")
	public String feedbackId = "";

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FeedbackRemark) {
			FeedbackRemark fbRemark = (FeedbackRemark) obj;
			if (this.feedbackId.equals(fbRemark.feedbackId)
					&& this.remarks.equals(fbRemark.remarks)
					&& this.remarksId.equals(fbRemark.remarksId)
					&& this.updateBy.equals(fbRemark.updateBy)
					&& this.updateOn.equals(fbRemark.updateOn)) {
				return  true;
			}
		}
		return false;
	}
}
