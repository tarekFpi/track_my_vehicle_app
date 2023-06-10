package com.singularity.trackmyvehicle.model.apiResponse.v2;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

public class GenericApiResponse<Data> {
	
	@Expose
	@SerializedName("context")
	public String context;
	@Expose
	@SerializedName("user_message")
	public String userMessage;
	@Expose
	@SerializedName("app_message")
	public String appMessage;
	@Expose
	@SerializedName("code")
	public String code;
	@Nullable
	@Expose
	@SerializedName("data")
	public Data   data;
	@Nullable
	@Expose
	@SerializedName("profile")
	public Data   profile;
}
