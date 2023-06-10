package com.singularity.trackmyvehicle.viewmodel

import androidx.lifecycle.ViewModel
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * Created by Sadman Sarar on 3/19/18.
 */
class AnalyticsViewModel @Inject constructor(
        private val mFirebaseAnalytics: FirebaseAnalytics
) : ViewModel() {

    fun routePlayStop(dateTime: DateTime?, progress: Int) {
        val params = Bundle()
        params.putString("selected_date", dateTime?.toString("dd-MMM-YY"))
        params.putInt("stopped_at", progress)
        mFirebaseAnalytics.logEvent("route_stop", params)
    }

    fun routePlayStarted(dateTime: DateTime?, startAt: Int) {
        val params = Bundle()
        params.putString("selected_date", dateTime?.toString("dd-MMM-YY"))
        params.putInt("started_at", startAt)
        mFirebaseAnalytics.logEvent("route_stop", params)
    }

    fun currentLocationViewed() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("current_location_screen", params)
    }

    fun routeViewed(dateTime: DateTime?) {
        val params = Bundle()
        params.putString("selected_date", dateTime?.toString("dd-MMM-YY"))
        mFirebaseAnalytics.logEvent("route_screen", params)
    }

    fun reportsViewed(dateTime: DateTime?) {
        val params = Bundle()
        params.putString("selected_month", dateTime?.toString("MMM-YY"))
        mFirebaseAnalytics.logEvent("report_screen", params)
    }

    fun expenseViewed(dateTime: DateTime?) {
        val params = Bundle()
        params.putString("selected_date", dateTime?.toString("MMM-YY"))
        mFirebaseAnalytics.logEvent("expense_list_screen", params)
    }

    fun expenseAddScreen() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("expense_add_screen", params)
    }

    fun expenseAdded() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("expense_added", params)
    }

    fun passwordChangePageViewed() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("password_change_screen", params)
    }

    fun passwordChanged() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("password_changed", params)
    }

    fun payBillsScreenViewed() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("pay_bill_screen", params)
    }

    fun secureModeViewed() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("secure_mode_screen", params)
    }

    fun payBillSuccesful() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("pay_bill_success", params)
    }

    fun payBillFailed() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("pay_bill_failed", params)
    }

    fun myFeedbackScreenViewed() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("feedback_list_screen", params)
    }

    fun myFeedbackAddScreenViewed() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("feedback_add_screen", params)
    }

    fun myFeedbackAdded() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("feedback_added", params)
    }

    fun myFeedbackDidNotAdd() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("feedback_not_add", params)
    }

    fun myFeedbackRemarksListScreenViewed() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("feedback_remarks_screen", params)
    }


    fun logOut() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("logout", params)
    }

    fun login() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("login", params)
    }

    fun forgetPasswordScreenViewed() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("reset_password_screen", params)
    }

    fun otpRequested() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("otp_requested", params)
    }

    fun otpValidated() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("otp_validated", params)
    }

    fun passwordReset() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("password_reset", params)
    }

    fun accountFragmentViewed() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("account_screen", params)
    }

    fun vehicleChangeScreenViewed() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("vehicle_change_screen_viewed", params)
    }


    fun vehicleSelected() {
        val params = Bundle()
        mFirebaseAnalytics.logEvent("vehicle_changed", params)
    }
}