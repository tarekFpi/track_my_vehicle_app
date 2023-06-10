package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.singularity.trackmyvehicle.model.entity.Expense;

import java.util.List;

public class ExpenseListResponse {

	@Expose
	@SerializedName("data")
	public List<Expense> data;
}
