package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sadman Sarar on 8/27/18.
 */
public class SecureModeReponse {
	
	@Expose
	@SerializedName("msg")
	public String msg;
	@Expose
	@SerializedName("ignition")
	public int    ignition;
	@Expose
	@SerializedName("secure")
	public int    secure;
	@Expose
	@SerializedName("vrn")
	public String vrn;
	@Expose
	@SerializedName("bstid")
	public String bstid;
	@Expose
	@SerializedName("bid")
	public int    bid;
}
