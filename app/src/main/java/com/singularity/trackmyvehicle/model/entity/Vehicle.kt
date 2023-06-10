package com.singularity.trackmyvehicle.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Sadman Sarar on 8/3/18.
 * Model Class for Vehicle
 */
@Entity(tableName = "vehicle")
data class Vehicle(
        @PrimaryKey
        @SerializedName("bid")
        var bid: String = "",
        @SerializedName("bstid")
        var bstid: String = "",
        @SerializedName("vrn")
        var vrn: String = "",
        @SerializedName("sim")
        var sim: String = "",
        @SerializedName("expiry_date")
        var expiryDate: String = "",
        @SerializedName("due_amount")
        var dueAmount: String = "",
        @SerializedName("is_suspended")
        var isSuspended: Boolean = false


)