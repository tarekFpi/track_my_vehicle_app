package com.singularity.trackmyvehicle.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.singularity.trackmyvehicle.viewmodel.*
import com.singularity.trackmyvehicle.viewmodel.LoginViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample/app/src/main/java/com/android/example/github/di
 */
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(VehiclesViewModel::class)
    internal abstract fun bindVehicleViewModel(vehiclesViewModel: VehiclesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    internal abstract fun bindLoginViewModel(vehiclesViewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReportsViewModel::class)
    internal abstract fun bindReportsViewModel(vehiclesViewModel: ReportsViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    internal abstract fun bindProcileViewModel(vehiclesViewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ExpenseViewModel::class)
    internal abstract fun bindExpenseViewModel(viewModel: ExpenseViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeedbackViewModel::class)
    internal abstract fun bindFeedbackViewModel(viewModel: FeedbackViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AnalyticsViewModel::class)
    internal abstract fun bindAnalyticsViewModel(viewModel: AnalyticsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SecureModeViewModel::class)
    internal abstract fun bindSecureModeViewModel(viewModel: SecureModeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    internal abstract fun bindNotificationViewModel(viewModel: NotificationViewModel): ViewModel


    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}