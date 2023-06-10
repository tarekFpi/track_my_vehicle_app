package com.singularity.trackmyvehicle.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.singularity.trackmyvehicle.BuildConfig
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.data.readUserSource
import com.singularity.trackmyvehicle.db.AppDb
import com.singularity.trackmyvehicle.db.dao.*
import com.singularity.trackmyvehicle.di.v2.SubModuleV2
import com.singularity.trackmyvehicle.di.v3.Dummy
import com.singularity.trackmyvehicle.di.v3.DummyV2
import com.singularity.trackmyvehicle.di.v3.DummyV3
import com.singularity.trackmyvehicle.di.v3.SubModuleV3
import com.singularity.trackmyvehicle.fcm.FCMRepository
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.IAppExecutors
import com.singularity.trackmyvehicle.network.PaginatedFeedbackFetcher
import com.singularity.trackmyvehicle.network.PaginatedVehicleFetcher
import com.singularity.trackmyvehicle.network.interceptor.CookieInterceptor
import com.singularity.trackmyvehicle.network.parser.DateTimeParser
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.repository.implementation.v2.FeedbackRepositoryImpl
import com.singularity.trackmyvehicle.repository.implementation.v2.LoginRepositoryImpl
import com.singularity.trackmyvehicle.repository.implementation.v2.PrefRepositoryImpl
import com.singularity.trackmyvehicle.repository.implementation.v3.INotificationRepository
import com.singularity.trackmyvehicle.repository.implementation.v3.ISupportTicketRepository
import com.singularity.trackmyvehicle.repository.interfaces.*
import com.singularity.trackmyvehicle.retrofit.LiveDataCallAdapterFactory
import com.singularity.trackmyvehicle.retrofit.MiddleWebService
import com.singularity.trackmyvehicle.utils.*
import com.singularity.trackmyvehicle.utils.firebase.FirebaseCredProvider
import com.singularity.trackmyvehicle.utils.firebase.FirebaseCredProviderImpl
import com.singularity.trackmyvehicle.view.adapter.*
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import com.singularity.trackmyvehicle.repository.implementation.v2.ProfileRepositoryImpl as ProfileRepositoryV2
import com.singularity.trackmyvehicle.repository.implementation.v2.ReportsRepositoryImpl as ReportsRepositoryV2
import com.singularity.trackmyvehicle.repository.implementation.v2.VehicleRepositoryImpl as VehicleRepositoryV2
import com.singularity.trackmyvehicle.repository.implementation.v3.ProfileRepositoryImpl as ProfileRepositoryV3
import com.singularity.trackmyvehicle.repository.implementation.v3.ReportsRepositoryImpl as ReportsRepositoryV3
import com.singularity.trackmyvehicle.repository.implementation.v3.VehicleRepositoryImpl as VehicleRepositoryV3
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService as WebServiceV2
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService as WebServiceV3


/**
 * https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample/app/src/main/java/com/android/example/github/di
 */
@Module(includes = [ViewModelModule::class, SubModuleV3::class, SubModuleV2::class])
class AppModule {

    @Singleton
    @Provides
    @Named("OkHttpClientV3")
    fun provideOkHttpClientV3(
            context: Context,
            loggingInterceptor: HttpLoggingInterceptor,
            cookieInterceptor: CookieInterceptor? = null
    ): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.readTimeout(3 * 60, TimeUnit.SECONDS)
                .connectTimeout(3 * 60, TimeUnit.SECONDS)
        clientBuilder.addInterceptor(loggingInterceptor)
        //clientBuilder.addInterceptor(ChuckerInterceptor(context))

        if (BuildConfig.DEBUG) {
            clientBuilder.addNetworkInterceptor(StethoInterceptor())
        }
        cookieInterceptor?.let {
            clientBuilder.addNetworkInterceptor(it)
        }
        return clientBuilder.build()
    }


    @Singleton
    @Provides
    fun provideWebService(@Named("OkHttpClientV3") client: OkHttpClient): WebServiceV3 {
        val builder = GsonBuilder()
        builder.setLenient()
        val dateTimeParser = DateTimeParser()
        builder.registerTypeAdapter(DateTime::class.java, dateTimeParser)

        val gson = builder.create()
        return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL_V3)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .client(client)
                .build()
                .create(WebServiceV3::class.java)
    }

    @Singleton
    @Provides
    @Named("OkHttpClientV2")
    fun provideOkHttpClientV2(
        context: Context,
        loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.readTimeout(Constants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(Constants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)

        clientBuilder.addInterceptor(loggingInterceptor)
       // clientBuilder.addInterceptor(ChuckerInterceptor(context))

        if (BuildConfig.DEBUG) {
            clientBuilder.addNetworkInterceptor(StethoInterceptor())
        }
        return clientBuilder.build()
    }

    @Singleton
    @Provides
    fun provideGithubService(@Named("OkHttpClientV2") client: OkHttpClient): WebServiceV2 {

        return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .client(client)
                .build()
                .create(WebServiceV2::class.java)
    }

    @Singleton
    @Provides
    fun provideMiddlewareService(context: Context,loggingInterceptor: HttpLoggingInterceptor): MiddleWebService {

        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)

        clientBuilder.addInterceptor(loggingInterceptor)
       /// clientBuilder.addInterceptor(ChuckerInterceptor(context))

        if (BuildConfig.DEBUG) {
            clientBuilder.addNetworkInterceptor(StethoInterceptor())
        }
        val client = clientBuilder.build()

        return Retrofit.Builder()
                .baseUrl(Constants.MiddleWare_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .client(client)
                .build()
                .create(MiddleWebService::class.java)
    }

    @Provides
    fun providesUserSource(sp: SharedPreferences): UserSource {
        return readUserSource(sp)
    }

    @Provides
    @Named("menu-buttons")
    fun providesVehicleMenuItems(userSource: UserSource): List<ReportButtonModel> {
        return when (userSource) {
            UserSource.VERSION_2 -> listOf(
                    ReportButtonModel("Vehicle Route Analytics", R.drawable.nav_icon_vehilce_route_svg),
                    ReportButtonModel("Distance Report", R.drawable.nav_icon_daily_distance_svg),
                    ReportButtonModel("Engine Report", R.drawable.nav_icon_daily_engine_svg),
                    ReportButtonModel("Speed report", R.drawable.nav_icon_speed_report_svg),
                    ReportButtonModel("Pay Bill", R.drawable.nav_icon_pay_bill_svg)
//                    ReportButtonModel("Disarm Engine", R.drawable.dailyengine)
            )
            UserSource.VERSION_3 -> listOf(
                    ReportButtonModel("Vehicle Route Analytics", R.drawable.nav_icon_vehilce_route_svg),
                    ReportButtonModel("Hourly Report", R.drawable.nav_icon_hourly_distance_svg),
                    ReportButtonModel("Engine Report", R.drawable.nav_icon_daily_engine_svg),
                    ReportButtonModel("Speed report", R.drawable.nav_icon_speed_report_svg),
                    ReportButtonModel("Location Report", R.drawable.nav_icon_location_report_svg),
                    ReportButtonModel("Distance Report", R.drawable.nav_icon_daily_distance_svg)
//                    ReportButtonModel("Notifications", R.drawable.nav_icon_notification_svg)
            )
            else -> listOf()
        }
    }

    @Provides
    fun providesDummy(v3: DummyV3, v2: DummyV2, userSource: UserSource): Dummy {
        return if (userSource == UserSource.VERSION_3) v3 else v2
    }

    @Provides
    fun providesSharedPreference(app: Application): SharedPreferences {
        return app.getSharedPreferences(Constants.APP_SHARED_PREF, Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideDb(app: Application): AppDb {
        return Room.databaseBuilder(app, AppDb::class.java, Constants.APP_DB)
                .fallbackToDestructiveMigration()
                .build()
    }

    @Singleton
    @Provides
    fun provideContext(app: Application): Context {
        return app
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Singleton
    @Provides
    fun providePreferenceRepository(context: Context): PrefRepository {
        return PrefRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun provideAppPreference(context: Context): AppPreference {
        return AppPreferenceImpl(context)
    }

    @Singleton
    @Provides
    fun providFeedbackRepository(feedbackDao: FeedbackDao,
                                 mWebService: WebServiceV2,
                                 mPrefRepository: PrefRepository,
                                 mPaginatedFeedbackFetcher: PaginatedFeedbackFetcher,
                                 appExecutors: AppExecutors,
                                 supportTicketRepository: ISupportTicketRepository,
                                 userSource: UserSource
    ): FeedbackRepository {
        return when (userSource.identifier) {
            UserSource.VERSION_2.identifier -> {
                return FeedbackRepositoryImpl(
                        feedbackDao,
                        mWebService,
                        mPrefRepository,
                        mPaginatedFeedbackFetcher,
                        appExecutors)
            }
            else -> supportTicketRepository
        }

    }

    @Provides
    fun providReportRepository(v2: ReportsRepositoryV2,
                               v3: ReportsRepositoryV3,
                               userSource: UserSource
    ): ReportsRepository {
        return if (userSource == UserSource.VERSION_3) v3 else v2
    }

    @Singleton
    @Provides
    fun provideProfileRepositoryV2(repo: ProfileRepositoryV2): ProfileRepository {
        return repo
    }

    @Singleton
    @Provides
    fun provideProfileRepositoryV3(repo: ProfileRepositoryV3): ProfileRepository {
        return repo
    }

    @Provides
    fun provideNetworkAvailabilityChecker(context: Context): NetworkAvailabilityChecker {
        return NetworkAvailabilityCheckerImpl(context)
    }

    @Singleton
    @Provides
    fun providVehicleRepositoryV2(vehicleDao: VehicleDao, mWebService: WebServiceV2,
                                  mPaginatedVehicleFetcher: PaginatedVehicleFetcher,
                                  mPrefRepository: PrefRepository, appExecutors: AppExecutors
    ): VehicleRepositoryV2 {

        return VehicleRepositoryV2(
                vehicleDao,
                mWebService,
                mPaginatedVehicleFetcher,
                mPrefRepository,
                appExecutors
        )
    }

    @Provides
    fun providVehicleRepository(v2: VehicleRepositoryV2, v3: VehicleRepositoryV3,
                                userSource: UserSource): VehicleRepository {
        return if (userSource == UserSource.VERSION_3) v3 else v2
    }

    @Singleton
    @Provides
    fun provideAppExecutors(
    ): AppExecutors {
        return IAppExecutors()
    }

    @Singleton
    @Provides
    fun provideHttpInterceptro(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    @Singleton
    @Provides
    fun provideVehicleDao(db: AppDb): VehicleDao {
        return db.vehicleDao()
    }

    @Singleton
    @Provides
    fun provideLoginRepository(
            webService: WebServiceV2,
            networkAvailabilityChecker: NetworkAvailabilityChecker,
            prefRepository: PrefRepository,
            appExecutors: AppExecutors,
            fcmRepository: FCMRepository,
            firebaseCredProvider: FirebaseCredProvider,
            randomStringGenerator: RandomStringGenerator
    ): LoginRepository {
        return LoginRepositoryImpl(
                webService,
                networkAvailabilityChecker,
                prefRepository,
                appExecutors,
                fcmRepository,
                firebaseCredProvider,
                randomStringGenerator
        )
    }

    @Singleton
    @Provides
    fun provideReportsDao(db: AppDb): ReportsDao {
        return db.reportDao()
    }

    @Singleton
    @Provides
    fun provideFeedbackDao(db: AppDb): FeedbackDao {
        return db.feedbackDao()
    }

    @Singleton
    @Provides
    fun provideterminalAggregatedDataDao(db: AppDb): TerminalAggregatedDataDao {
        return db.terminalAggregatedDataDao()
    }


    @Singleton
    @Provides
    fun provideTerminalDao(db: AppDb): TerminalDao {
        return db.terminalDao()
    }

    @Singleton
    @Provides
    fun proviedeTerminalDataMinutelyDao(db: AppDb): TerminalDataMinutelyDao {
        return db.terminalDataMinutelyDao()
    }


    @Provides
    fun provideVehicleAdapter(): VehicleListAdapter {
        return VehicleListAdapter()
    }

    @Provides
    fun provideExpenseListAdapter(): ExpenseListAdapter {
        return ExpenseListAdapter()
    }

    @Provides
    fun provideFeedbackListAdapter(): FeedbackListAdapter {
        return FeedbackListAdapter()
    }

    @Provides
    fun provideRemarkListAdapter(): FeedbackRemarkListAdapter {
        return FeedbackRemarkListAdapter()
    }

    @Provides
    fun provideFirebaseAnalytics(context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    fun provideFirebaseCredProvider(): FirebaseCredProvider {
        return FirebaseCredProviderImpl()
    }

    @Provides
    fun provideRandomStringGenerator(): RandomStringGenerator {
        return RandomStringGeneratorImpl()
    }

    @Provides
    fun provideNotificationRepository(model: INotificationRepository): NotificationRepository {
        return model
    }

    @Provides
    fun provideSupportTicketDao(appdb: AppDb): SupportTicketDao {
        return appdb.supportTicketDao()
    }

    @Provides
    fun provideSupportTicketCategoryDao(appdb: AppDb): SupportTicketCategoryDao {
        return appdb.supportTicketCategoryDao()
    }

    @Provides
    fun provideNotificationDao(appdb: AppDb): NotificationDao {
        return appdb.notificationDao()
    }

}