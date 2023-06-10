package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sadman Sarar on 3/13/18.
 */

public class OtpResponse {
	
	@Expose
	@SerializedName("otp_token")
	public String otpToken;
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
	public int    code;
}
