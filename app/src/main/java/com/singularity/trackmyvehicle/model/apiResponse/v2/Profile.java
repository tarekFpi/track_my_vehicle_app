package com.singularity.trackmyvehicle.model.apiResponse.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sadman Sarar on 3/13/18.
 */

public class Profile {
	
	@Expose
	@SerializedName("address")
	public String address;
	@Expose
	@SerializedName("mobile")
	public String mobile;
	@Expose
	@SerializedName("email")
	public String email;
	@Expose
	@SerializedName("name")
	public String name;
	@Expose
	@SerializedName("role")
	public String role;
	@Expose
	@SerializedName("power")
	public String power;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof  Profile) {
			Profile profile = (Profile) obj;
			if (this.address.equals(profile.address)
					&& this.email.equals(profile.email)
					&& this.mobile.equals(profile.mobile)
					&& this.name.equals(profile.name)
					&& this.role.equals(profile.role)
					&& this.power.equals(profile.power)) {
				return  true;
			}
		}
		return false;
	}
}
