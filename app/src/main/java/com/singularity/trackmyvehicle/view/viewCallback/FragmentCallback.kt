package com.singularity.trackmyvehicle.view.viewCallback

import com.singularity.trackmyvehicle.model.apiResponse.v3.EventsVehicleRouteAnalyticsItem
import org.joda.time.DateTime

/**
 * Created by Sadman Sarar on 3/13/18.
 */
interface ExpenseListFragmentCallback {
    fun addExpenseClicked()
    fun getSelectedDate(): DateTime

}

interface ExpenseCreatorFragmentCallback {
    fun expenseAdded()
    fun getSelectedDate(): DateTime
}

interface EventListCallback {
    fun onEventListFound(list: ArrayList<EventsVehicleRouteAnalyticsItem>)
}