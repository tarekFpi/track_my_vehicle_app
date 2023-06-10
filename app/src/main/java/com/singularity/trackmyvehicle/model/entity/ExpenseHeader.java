package com.singularity.trackmyvehicle.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

@Entity(tableName = "expense_headers")
public class ExpenseHeader {
	@NonNull
	@PrimaryKey
	@Expose
	@SerializedName("name")
	public String name = "";

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ExpenseHeader) {
			if (this.name.equals(((ExpenseHeader) obj).name)) {
				return true;
			}
		}
		return false;
	}
}
