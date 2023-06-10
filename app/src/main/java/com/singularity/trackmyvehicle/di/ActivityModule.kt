package com.singularity.trackmyvehicle.di

import com.singularity.trackmyvehicle.view.activity.*
import com.singularity.trackmyvehicle.view.fragment.MapFragment
import com.singularity.trackmyvehicle.view.map.MapDrawer

import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample/app/src/main/java/com/android/example/github/di
 */
@Module
abstract class MainActivityModule {

    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity

}

@Module
abstract class LoginActivityModule {

    @ContributesAndroidInjector
    internal abstract fun contributeLoginActivity(): LoginActivity

}

@Module
abstract class SplashActivityModule {

    @ContributesAndroidInjector
    internal abstract fun contributeSplashActivity(): SplashScreenActivity

}


@Module
abstract class HomeActivityModule {

    @ContributesAndroidInjector
    internal abstract fun contributeHomeActivity(): HomeActivity

    @ContributesAndroidInjector
    internal abstract fun contributeVehicleRouteActivity(): VehicleRouteActivity

    @ContributesAndroidInjector
    internal abstract fun contributeVehicleRouteAnalyticsActivity(): VehicleRouteAnalyticsActivity

    @ContributesAndroidInjector
    internal abstract fun contributeHomeActivity2(): HomeActivity2

    @ContributesAndroidInjector
    internal abstract fun contributeVehicleMonitoringActivity(): VehicleMonitoringActivity

    @ContributesAndroidInjector
    internal abstract fun contributeTripReportActivity(): TripReportActivity

    @ContributesAndroidInjector
    internal abstract fun contributeMonitoringActivity(): MonitoringActivity

    @ContributesAndroidInjector
    internal abstract fun contributeDirectionActivity(): DirectionActivity

    @ContributesAndroidInjector
    internal abstract fun contributeSpeedReportActivity(): ReportTabActivity

    @ContributesAndroidInjector
    internal abstract fun contributeSecureModeActivity(): SecureModeActivity

//    @ContributesAndroidInjector
//    internal abstract fun contributeSpeedReportFragment(): SpeedReportFragment

    @ContributesAndroidInjector
    internal abstract fun contributeAccountActivity(): FragmentActivity

    @ContributesAndroidInjector
    internal abstract fun contributeVirtualWatchmanSet(): VirtualWatchmanSet

    @ContributesAndroidInjector
    internal abstract fun contributeVirtualWatchmanActivation(): VirtualWatchmanActivation

    @ContributesAndroidInjector
    internal abstract fun contributeAnalytics(): AnalyticsActivity

    @ContributesAndroidInjector
    internal abstract fun contributeAccountPageActivity(): AccountActivity

}

@Module
abstract class ForgotPasswordModule {
    @ContributesAndroidInjector
    internal abstract fun contributeHomeActivity(): ForgetPasswordActivity

}

@Module
abstract class ChangePasswordModule {
    @ContributesAndroidInjector
    internal abstract fun contributeHomeActivity(): ChangePasswordActivity

}

@Module
abstract class BillingModule {
    @ContributesAndroidInjector
    internal abstract fun contributeHomeActivity(): BillingActivity

    @ContributesAndroidInjector
    internal abstract fun contributeFeedbackActivity(): FeedbackActivity

    @ContributesAndroidInjector
    internal abstract fun contributeReportsActivity(): ReportsActivity
}

//@Module
//abstract class BottomNavFragmentModule {
//    @ContributesAndroidInjector
//    internal abstract fun contributeBottomNavFragment(): BottomNavFragment
//}
