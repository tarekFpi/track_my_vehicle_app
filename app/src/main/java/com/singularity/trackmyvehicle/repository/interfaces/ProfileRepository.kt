package com.singularity.trackmyvehicle.repository.interfaces

import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.OtpValidationResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.network.Resource

/**
 * Created by Imran Chowdhury on 2020-01-26.
 */


interface ProfileRepository {
    fun fetchOrGetProfileInformation(): MutableLiveData<Resource<Profile>>
    fun fetchProfileInformation(): MutableLiveData<Resource<Profile>>
    fun getProfile(): Profile?
    fun requestForgetPasswordOtp(username: String, mobile: String): MutableLiveData<Resource<OtpResponse>>
    fun validateOtp(otp: String): MutableLiveData<Resource<OtpValidationResponse>>
    fun resetPassword(username: String, passwordToken: String, password: String): MutableLiveData<Resource<GenericApiResponse<String>>>
    fun changePassword(username: String, currentPassword: String, newPassword: String): MutableLiveData<Resource<GenericApiResponse<String>>>
    fun logout(callbackComplete: () -> Unit, dontCare: Boolean = true)
}