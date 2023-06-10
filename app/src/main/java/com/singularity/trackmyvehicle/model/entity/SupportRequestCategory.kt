package com.singularity.trackmyvehicle.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "support_request_categories")
data class SupportRequestCategory(
        @PrimaryKey
        @SerializedName("SupportRequestCategoryID")
        var supportRequestCategoryID: String = "",
        @SerializedName("SupportRequestCategoryName")
        var supportRequestCategoryName: String? = "",
        @SerializedName("SupportRequestCategoryIdentifier")
        var supportRequestCategoryIdentifier: String? = ""
) {
    fun toFeedbackHeader(): FeedbackHeader {
        return FeedbackHeader().apply {
            this.name = supportRequestCategoryName ?: ""
            this.id = supportRequestCategoryID
        }
    }
}