package com.singularity.trackmyvehicle.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "expenses")
public class Expense {
	
	@NonNull
	@PrimaryKey
	@Expose
	@SerializedName("id")
	public String id;
	
	@NonNull
	@Expose
	@SerializedName("date")
	public String date = "";
	
	@NonNull
	@Expose
	@SerializedName("bstid")
	public String bstid = "";
	
	@NonNull
	@Expose
	@SerializedName("name")
	public String expenseHeader = "";
	
	@NonNull
	@Expose
	@SerializedName("amount")
	public String amount;
	
	@NonNull
	@Expose
	@SerializedName("details")
	public String description = "";

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Expense){
			Expense expense = (Expense) obj;
			if (this.id.equals(expense.id)
					&& this.amount.equals(expense.amount)
					&& this.bstid.equals(expense.bstid)
					&& this.expenseHeader.equals(expense.expenseHeader)) {
				return true;
			}
		}
		return false;
	}
}
