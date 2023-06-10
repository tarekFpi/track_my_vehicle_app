package com.singularity.trackmyvehicle.model.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

@Entity(tableName = "feedback_headers")
public class FeedbackHeader {
	@NonNull
	@PrimaryKey
	@Expose
	@SerializedName("name")
	public String name = "";
	@Ignore
	public String id = "";

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof  FeedbackHeader) {
			if (this.name.equals(((FeedbackHeader) obj).name)) {
				return true;
			}
		}
		return false;
	}

	@NonNull
	@Override
	public String toString() {
		return name;
	}
}
