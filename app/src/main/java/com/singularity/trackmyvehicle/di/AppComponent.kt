package com.singularity.trackmyvehicle.di

import android.app.Application
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.admin.AdminDimensionModule
import com.singularity.trackmyvehicle.fcm.AppFirebaseMessagingService
import com.singularity.trackmyvehicle.utils.NetworkChangeReceiver
import com.singularity.trackmyvehicle.view.activity.AnalyticsFragment
import com.singularity.trackmyvehicle.view.adapter.ReportButtonAdapter
import com.singularity.trackmyvehicle.view.dialog.SuspendDialog
import com.singularity.trackmyvehicle.view.fragment.*
import com.singularity.trackmyvehicle.view.map.MapDrawer
import com.singularity.trackmyvehicle.view.viewholder.FeedBackItemViewHolder
import com.singularity.trackmyvehicle.view.viewholder.VehicleItemViewHolder
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

/**
 * https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample/app/src/main/java/com/android/example/github/di
 */
@Singleton
@Component(modules = arrayOf(
        AndroidInjectionModule::class,
        AppModule::class,
        MainActivityModule::class,
        LoginActivityModule::class,
        SplashActivityModule::class,
        HomeActivityModule::class,
        AdminDimensionModule::class,
        ForgotPasswordModule::class,
        ChangePasswordModule::class,
        BillingModule::class))
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: VehicleTrackApplication)
    fun inject(app: BottomNavFragment)
    fun inject(expenseListFragment: ExpenseListFragment)
    fun inject(expenseCreateFragment: ExpenseCreatorFragment)
    fun inject(accountFragment: AccountFragment)
    fun inject(feedbackListFragment: FeedbackListFragment)
    fun inject(feedbackFormFragment: FeedbackFormFragment)
    fun inject(feedbackViewFragment: FeedbackViewFragment)
    fun inject(speedReportFragment: SpeedReportFragment)
    fun inject(distanceReportFragment: DistanceReportFragment)
    fun inject(mapFragment: MapFragment)
    fun inject(suspendDialog: SuspendDialog)
    fun inject(analyticsFragment: AnalyticsFragment)
    fun inject(speedViolationReportFragment: SpeedViolationReportFragment)
    fun inject(notificationListFragment: NotificationListFragment)
    fun inject(notificationFragment: NotificationFragment)
    fun inject(hourlyDistanceReportFragment: HourlyDistanceReportFragment)
    fun inject(dailyEngineReportFragment: DailyEngineReportFragment)
    fun inject(reportButtonAdapter: ReportButtonAdapter)
    fun inject(vehicleItemViewHolder: VehicleItemViewHolder)
    fun inject(locationReportFragment: LocationReportFragment)
    fun inject(monthlyReportFragment: MonthlyReportFragment)
    fun inject(feedBackItemViewHolder: FeedBackItemViewHolder)
    fun inject(supportFormFragment: SupportFormFragment)
    fun inject(appFirebaseMessagingService: AppFirebaseMessagingService)
    fun inject(networkChangeReceiver: NetworkChangeReceiver)

}