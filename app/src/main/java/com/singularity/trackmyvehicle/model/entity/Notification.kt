package com.singularity.trackmyvehicle.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

/**
 * Created by Imran Chowdhury on 2020-01-16.
 */
@Entity(tableName = "notifications")
data class Notification(
        @PrimaryKey
        @SerializedName("MessageID")
        var id: Int? = 0,
        @SerializedName("MessageTime")
        var time: DateTime? = null,
        @SerializedName("MessageSubject")
        var subject: String? = "",
        @SerializedName("MessageContent")
        var message: String? = "",
        @SerializedName("MessageContentPlain")
        var messageContentPlain: String? = "",
        @SerializedName("MessageIsRead")
        var isRead: String? = ""
)