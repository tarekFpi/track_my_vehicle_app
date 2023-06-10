package com.singularity.trackmyvehicle.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singularity.trackmyvehicle.db.converter.DateTimeTypeConverter
import com.singularity.trackmyvehicle.db.dao.*
import com.singularity.trackmyvehicle.model.apiResponse.v3.SupportTicket
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.apiResponse.v3.TerminalDataMinutely
import com.singularity.trackmyvehicle.model.entity.*

/**
 * Created by Sadman Sarar on 8/3/18.
 * Database Class including the Dao
 */
@Database(entities = [
    Vehicle::class,
    VehicleRoute::class,
    DistanceReport::class,
    SpeedAlertReport::class,
    ExpenseHeader::class,
    Expense::class,
    FeedbackHeader::class,
    Feedback::class,
    VehicleRoutePolyline::class,
    FeedbackRemark::class,
    TerminalAggregatedData::class,
    Terminal::class,
    TerminalDataMinutely::class,
    Notification::class,
    SupportTicket::class,
    SupportRequestCategory::class
], version = 15, exportSchema = false)
@TypeConverters(DateTimeTypeConverter::class)
abstract class AppDb : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun notificationDao(): NotificationDao
    abstract fun terminalDao(): TerminalDao
    abstract fun terminalDataMinutelyDao(): TerminalDataMinutelyDao
    abstract fun reportDao(): ReportsDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun terminalAggregatedDataDao(): TerminalAggregatedDataDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun supportTicketCategoryDao(): SupportTicketCategoryDao
}

