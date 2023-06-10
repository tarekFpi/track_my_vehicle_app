package com.singularity.trackmyvehicle.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.singularity.trackmyvehicle.di.v3.ApiHandler
import com.singularity.trackmyvehicle.di.v3.RetrofitClient
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.model.apiResponse.v3.ProfileInfoResponse
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import okhttp3.Cookie
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/**
 * Created by Kariba Yasmin on 10/28/21.
 */
class ProfileUpdateViewModel : ViewModel() {

    var mutableProfileData = MutableLiveData<Profile>()

    var script : String = ENDPOINTS.PROFILE_INFO_SCRIPT

    var subject : String = ENDPOINTS.PROFILE_INFO_SUBJECT

    var action : String = ENDPOINTS.PROFILE_INFO_ACTION

    var nameFirst : String = ""

    var nameMiddle : String = ""

    var nameLast : String = ""

    var cookie : String = ""

    private lateinit var nameFirstRequestBody : RequestBody
    private lateinit var nameMiddleRequestBody : RequestBody
    private lateinit var nameLastRequestBody : RequestBody

    fun loadProfileData(context : Context, ck: String, nFirst : String, nMiddle : String, nLast : String) : LiveData<Profile> {
        mutableProfileData = MutableLiveData()
        cookie = ck
        nameFirst = nFirst
        nameMiddle = nMiddle
        nameLast = nLast

        nameFirstRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), nameFirst)
        nameMiddleRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), nameMiddle)
        nameLastRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), nameLast)

        updateName(context)
        return mutableProfileData

    }


    private fun updateName(context: Context) {
        val call = RetrofitClient.getInstance(cookie)?.create(ApiHandler::class.java)?.profileNameUpdate(
                script,
                subject,
                action,
                nameFirstRequestBody,
                nameMiddleRequestBody,
                nameLastRequestBody)

       call?.enqueue(object : Callback<ProfileInfoResponse>{
           override fun onResponse(call: Call<ProfileInfoResponse>, response: Response<ProfileInfoResponse>) {
               if(response.isSuccessful && response.body() != null){
                   val model = Profile()
                   response.body()?.let {
                       model.email = it.profileResponse?.email
                       model.name = it.profileResponse?.nameFirst + " " + it.profileResponse?.nameMiddle + " " + it.profileResponse?.nameLast
                       model.mobile = it.profileResponse?.mobileNumber

                   }

                   setUpdateProfileData(model)

               }
           }

           override fun onFailure(call: Call<ProfileInfoResponse>, t: Throwable) {
               Toast.makeText(context, "Something Went wrong, Please try again", Toast.LENGTH_SHORT).show()
           }

       })
    }

   fun setUpdateProfileData(data : Profile){
       mutableProfileData.value = data
   }
}