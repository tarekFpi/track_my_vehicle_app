package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sadman Sarar on 3/11/18.
 */

public class PaginatedWrapper<T> {
	
	@Expose
	@SerializedName("meta")
	public Meta meta;
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
	public T      data;
	
	public Meta getMeta() {
		return meta;
	}
	
	public void setMeta(Meta meta) {
		this.meta = meta;
	}
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	
	public String getUserMessage() {
		return userMessage;
	}
	
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}
	
	public String getAppMessage() {
		return appMessage;
	}
	
	public void setAppMessage(String appMessage) {
		this.appMessage = appMessage;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public T getData() {
		return data;
	}
	
	public void setData(T data) {
		this.data = data;
	}
}
