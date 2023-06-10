package com.singularity.trackmyvehicle.repository.interfaces

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.singularity.trackmyvehicle.model.apiResponse.v2.LoginResponse
import com.singularity.trackmyvehicle.model.dataModel.LoginModel
import com.singularity.trackmyvehicle.network.Resource

/**
 * Created by Imran Chowdhury on 2020-01-21.
 */


interface LoginRepository {

    @WorkerThread
    fun login(username: String, password: String): Resource<LoginModel>

}