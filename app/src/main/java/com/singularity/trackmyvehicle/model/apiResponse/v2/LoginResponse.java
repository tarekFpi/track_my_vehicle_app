package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sadman Sarar on 3/11/18.
 */

public class LoginResponse {

  @Expose
  @SerializedName("user")
  public User user;
  @Expose
  @SerializedName("access_time")
  public String accessTime;
  @Expose
  @SerializedName("access_token")
  public String accessToken;
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
  public int code;

  public static class User {
  	public String id;
    @Expose
    @SerializedName("phone")
    public String phone;
    @Expose
    @SerializedName("email")
    public String email;
    @Expose
    @SerializedName("name")
    public String name;
    public String userGroupIdentifier = "";
  }
}
