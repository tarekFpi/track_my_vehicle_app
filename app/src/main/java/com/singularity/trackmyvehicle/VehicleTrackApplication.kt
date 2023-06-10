package com.singularity.trackmyvehicle

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.multidex.MultiDex
import com.facebook.stetho.Stetho
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.db.AppDb
import com.singularity.trackmyvehicle.di.AppComponent
import com.singularity.trackmyvehicle.di.DaggerAppComponent
import com.singularity.trackmyvehicle.fcm.FCMRepository
import com.singularity.trackmyvehicle.model.event.LogoutEvent
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.implementation.v3.ILoginRepository
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.view.activity.ForgetPasswordActivity
import com.singularity.trackmyvehicle.view.activity.LoginActivity
import com.singularity.trackmyvehicle.view.activity.MapsActivity
import com.singularity.trackmyvehicle.view.activity.SplashScreenActivity
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import net.danlew.android.joda.JodaTimeAndroid
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import javax.inject.Inject
import javax.inject.Named


/**
 * Created by Sadman Sarar on 8/3/18.
 * Application class
 */
class VehicleTrackApplication : Application(), HasActivityInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    @field:Named("OkHttpClientV3")
    lateinit var v3OkHttpClient: OkHttpClient
    @Inject
    @field:Named("OkHttpClientV2")
    lateinit var v2OkHttpClient: OkHttpClient

    /*

    @Inject
    lateinit var dispatchingFragmentAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingFragmentAndroidInjector
    }
*/

    @Inject
    lateinit var mAppDb: AppDb
    @Inject
    lateinit var mPrefRepository: PrefRepository
    @Inject
    lateinit var mFCMRepository: FCMRepository
    @Inject
    lateinit var v3LoginRepository: ILoginRepository

    @Inject
    lateinit var executors: AppExecutors

    companion object {
        var appComponent: AppComponent? = null
        var app: VehicleTrackApplication? = null
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingAndroidInjector
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        app = this
        /*val fabric = Fabric.Builder(this)
                .kits(Crashlytics())
                .debuggable(true)
                .build()

        Fabric.with(fabric)*/
        JodaTimeAndroid.init(this);

        //Instantiate Dagger
        appComponent = DaggerAppComponent.builder()
                .application(this)
                .build()

        appComponent?.inject(this)

        EventBus.getDefault().register(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks() {


            override fun onActivityResumed(p0: Activity) {
                super.onActivityResumed(p0)
                isCurrentlyLoginActivity = p0 is SplashScreenActivity || p0 is LoginActivity || p0 is ForgetPasswordActivity

            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                isCurrentlyLoginActivity = activity is SplashScreenActivity || activity is LoginActivity || activity is ForgetPasswordActivity
                if (activity is MapsActivity)
                    return
                try {
                    activity.let { AndroidInjection.inject(activity) }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    FirebaseCrashlytics.getInstance().recordException(ex)
                }
            }
        })

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

        if (BuildConfig.DEBUG) {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false);
        }

        var firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseRemoteConfig.setConfigSettings(FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build());
        firebaseRemoteConfig.setDefaults(R.xml.default_firebase_map)
        val fetchTask = FirebaseRemoteConfig.getInstance().fetch(30)
        fetchTask.addOnSuccessListener {
            FirebaseRemoteConfig.getInstance().activateFetched()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(
                    Context.NOTIFICATION_SERVICE) as NotificationManager?
            val channelId = "tmv-robi-channe"
            val vibrationPatter = longArrayOf(0, 500, 500, 500, 500, 500, 500, 500, 500)
            val channel = NotificationChannel(
                    channelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            )
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            channel.setSound(uri, AudioAttributes.Builder().build())
            channel.enableVibration(true)
            channel.vibrationPattern = vibrationPatter
            channel.setShowBadge(false)
            notificationManager?.createNotificationChannel(channel)
        }
    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
        }
    }

    var isCurrentlyLoginActivity: Boolean = false


    abstract class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(p0: Activity) {
        }

        override fun onActivityResumed(p0: Activity) {
        }

        override fun onActivityStarted(p0: Activity) {
        }

        override fun onActivityDestroyed(p0: Activity) {
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        }

        override fun onActivityStopped(p0: Activity) {
        }

        override fun onActivityCreated(p0: Activity, savedInstanceState: Bundle?) {
        }
    }

    var isTryingBackgroundLogin: Boolean = false
    var isAuthEventProcessing: Boolean = false

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        if (isAuthEventProcessing) {
            return
        }
        isAuthEventProcessing = true
        if (event.isAutomatic) {
            v3OkHttpClient.dispatcher().cancelAll()
            v2OkHttpClient.dispatcher().cancelAll()
        }
        val passwordHash = mPrefRepository.passwordHash()
        val username = mPrefRepository.userName()
        if (event.isAutomatic && mPrefRepository.userSource == UserSource.VERSION_3.identifier && passwordHash.isNotEmpty() && username.isNotEmpty() && !isTryingBackgroundLogin) {
            isTryingBackgroundLogin = true
            executors.networkThread {
                val response = v3LoginRepository.doLoginUsingNetworkWithPasswordHash(username,
                        passwordHash)
                if (response.status != Status.SUCCESS) {
                    startLogoutProcedure()
                } else {
                    isAuthEventProcessing = false
                }
                isTryingBackgroundLogin = false
            }
            return
        }

        startLogoutProcedure()

    }

    fun startLogoutProcedure(callback: (() -> Unit)? = null, earlyScreenChange: Boolean = false) {
        if(callback != null ){
            v3OkHttpClient.dispatcher().cancelAll()
            v2OkHttpClient.dispatcher().cancelAll()
        }
        if(earlyScreenChange){
            executors.mainThread {
                mPrefRepository.saveApiToken("")
                mPrefRepository.saveUser(null)
                mPrefRepository.saveProfile(null)
                mPrefRepository.saveUserSource(UserSource.NOT_DETERMINED.identifier)
                mPrefRepository.saveCookie("")
                mPrefRepository.changeCurrentVehicle("", "", "")
                mPrefRepository.changeCurrentVehicle("", "", "", "")
                mPrefRepository.saveUserName("")
                mPrefRepository.savePasswordHash("")
                mPrefRepository.saveDeviceFCM("")
                mPrefRepository.saveUnreadMessageCount(0)
                callback?.invoke()
            }
            return
        }
        executors.ioThread {
            mAppDb.vehicleDao().deleteAllRoute()
            mAppDb.vehicleDao().deleteAllVehicle()
            mAppDb.vehicleDao().deleteAllRoute()
            mAppDb.terminalDao().deleteAllTerminal()
            mAppDb.reportDao().deleteAllDistanceReport()
            mAppDb.reportDao().deleteAllExpense()
            mAppDb.reportDao().deleteAllSpeedAlert()
            mAppDb.feedbackDao().deleteAllFeedbacks()
            mAppDb.feedbackDao().deleteAllFeedbackRemarks()
            mAppDb.terminalAggregatedDataDao().deleteAllTerminalAggregatedData()
            mAppDb.terminalDataMinutelyDao().deleteAll()

            executors.mainThread {
                mPrefRepository.saveApiToken("")
                mPrefRepository.saveUser(null)
                mPrefRepository.saveProfile(null)
                mPrefRepository.saveUserSource(UserSource.NOT_DETERMINED.identifier)
                mPrefRepository.saveCookie("")
                mPrefRepository.changeCurrentVehicle("", "", "")
                mPrefRepository.changeCurrentVehicle("", "", "", "")
                mPrefRepository.saveUserName("")
                mPrefRepository.savePasswordHash("")
                mPrefRepository.saveDeviceFCM("")
                mPrefRepository.saveUnreadMessageCount(0)

                if(callback == null) {
                    if (!isCurrentlyLoginActivity) {
                        isCurrentlyLoginActivity = true
                        val intent = Intent(this, SplashScreenActivity::class.java)
                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        this.startActivity(intent)
                    }
                    isAuthEventProcessing = false
                }else {
                    callback.invoke()
                }
            }
        }

    }
}


class CustomException : Exception() {
    override val message: String?
        get() = "This is just a CustomException"
}