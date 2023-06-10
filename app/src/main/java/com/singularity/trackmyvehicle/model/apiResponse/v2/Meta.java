package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

public class Meta {
	
	@Expose
	@SerializedName("total")
	public String total;
	@Expose
	@SerializedName("per_page")
	public int    perPage;
	@Expose
	@SerializedName("to")
	public int    to;
	@Expose
	@SerializedName("from")
	public int    from;
	@Expose
	@SerializedName("last_page")
	public int    lastPage;
	@Expose
	@SerializedName("current_page")
	public int    currentPage;
}
