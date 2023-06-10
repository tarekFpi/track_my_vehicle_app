package com.singularity.trackmyvehicle.model.dataModel

/**
 * Created by Imran Chowdhury on 2020-01-23.
 */

data class LoginModel(
        var userId: String? = null,
        var userEmail: String? = null,
        var userSignInName: String? = null,
        var userName: String? = null,
        var userPhone: String? = null,
        var userGroupIdentifier: String? = null,
        var userGroupName: String? = null,
        var userGroupWeight: String? = null
)