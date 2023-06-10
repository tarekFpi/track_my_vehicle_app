package com.singularity.trackmyvehicle.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "feedbacks")
public class Feedback {
	
	@PrimaryKey
	@NonNull
	@Expose
	@SerializedName("feedback_id")
	public String feedbackId = "";
	@Expose
	@SerializedName("sl")
	public int    sl;
	@Expose
	@SerializedName("remarks")
	public String remarks;
	@Expose
	@SerializedName("solved_on")
	public String solvedOn;
	@Expose
	@SerializedName("raised_on")
	public String raisedOn;
	@Expose
	@SerializedName("feedback")
	public String feedback;
	@Expose
	@SerializedName("vrn")
	public String vrn;
	@Expose
	@SerializedName("bstid")
	public String bstid;
	@Expose
	@SerializedName("feedback_status")
	public String feedbackStatus;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Feedback) {
			Feedback feedbackObj = (Feedback) obj;
			if (this.bstid.equals(feedbackObj.bstid)
					&& this.feedback.equals(feedbackObj.feedback)
					&& this.feedbackId.equals(feedbackObj.feedbackId)
					&& this.feedbackStatus.equals(feedbackObj.feedbackStatus)
					&& this.raisedOn.equals(feedbackObj.raisedOn)
					&& this.remarks.equals(feedbackObj.remarks)
					&& this.sl == feedbackObj.sl
					&& this.solvedOn.equals(feedbackObj.solvedOn)){
				return true;
			}
		}
		return false;
	}
}
