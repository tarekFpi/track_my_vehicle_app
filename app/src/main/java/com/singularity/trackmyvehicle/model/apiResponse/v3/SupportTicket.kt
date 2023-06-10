package com.singularity.trackmyvehicle.model.apiResponse.v3

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.singularity.trackmyvehicle.model.entity.Feedback
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark


@Entity(tableName = "support_tickets")
data class SupportTicket(
        @PrimaryKey
        @SerializedName("SupportRequestResponseID")
        var supportRequestResponseID: Int = 0,
        @SerializedName("SupportRequestID")
        var supportRequestID: String = "",
        @SerializedName("SupportRequestResponseTime")
        var supportRequestResponseTime: String? = "",
        @SerializedName("SupportRequestResponseMessage")
        var supportRequestResponseMessage: String? = "",
        @SerializedName("UserSignInName")
        var userSignInName: String? = "",
        @SerializedName("UserEmail")
        var userEmail: String? = "",
        @SerializedName("UserName")
        var userName: String? = "",
        @SerializedName("SupportRequestSubject")
        var supportRequestSubject: String? = "",
        @SerializedName("SupportRequestIsRead")
        var supportRequestIsRead: String? = "",
        @SerializedName("SupportRequestResponseIsReply")
        var supportRequestResponseIsReply: String? = "",
        @SerializedName("SupportRequestReadTime")
        var supportRequestReadTime: String? = "",
        @SerializedName("SupportRequestTime")
        var supportRequestTime: String? = "",
        @SerializedName("SupportRequestUserSignInName")
        var supportRequestUserSignInName: String? = "",
        @SerializedName("SupportRequestUserEmail")
        var supportRequestUserEmail: String? = "",
        @SerializedName("SupportRequestUserName")
        var supportRequestUserName: String? = "",
        @SerializedName("CustomerCode")
        var customerCode: String? = "",
        @SerializedName("CustomerName")
        var customerName: String? = "",
        @SerializedName("TerminalAssignmentCode")
        var terminalAssignmentCode: String? = "",
        @SerializedName("TerminalAssignmentCustomerCode")
        var terminalAssignmentCustomerCode: String? = "",
        @SerializedName("TerminalAssignmentCustomerName")
        var terminalAssignmentCustomerName: String? = "",
        @SerializedName("CarrierRegistrationNumber")
        var carrierRegistrationNumber: String? = "",
        @SerializedName("SupportRequestCategoryIDList")
        var supportRequestCategoryIDList: String? = "",
        @SerializedName("SupportRequestCategoryIdentifierList")
        var supportRequestCategoryIdentifierList: String? = "",
        @SerializedName("CarrierName")
        var carrierName: String? = ""
) {

    fun toFeedback(input : List<SupportTicket>): Feedback {
        val output = Feedback()
        output.bstid = terminalAssignmentCode
        output.feedbackId = supportRequestID
        output.remarks = supportRequestResponseMessage
        output.solvedOn = supportRequestReadTime
        output.raisedOn = supportRequestTime
        output.feedback = if(!supportRequestSubject.isNullOrEmpty()) supportRequestSubject + "\n" + supportRequestResponseMessage else supportRequestResponseMessage
        output.vrn = carrierRegistrationNumber
        val lastMessage = input.filter { it.supportRequestID == this.supportRequestID }?.sortedByDescending { it.supportRequestResponseTime }.firstOrNull()
        output.feedbackStatus = if (lastMessage?.supportRequestResponseIsReply == "1") "Answered" else "Not Answered"
        return output
    }

    fun toFeedbackRemark(): FeedbackRemark {
        val output = FeedbackRemark()
        output.feedbackId = supportRequestID.toString()
        output.remarks = supportRequestResponseMessage
        output.updateOn = supportRequestResponseTime
        output.updateBy = userName
        output.remarks = supportRequestResponseMessage
        output.remarksId = supportRequestResponseID.toString()
        return output
    }
}