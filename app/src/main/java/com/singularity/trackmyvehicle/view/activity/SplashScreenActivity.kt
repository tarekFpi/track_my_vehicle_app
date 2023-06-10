package com.singularity.trackmyvehicle.view.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.florent37.viewanimator.ViewAnimator
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.utils.NetworkAvailabilityChecker
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import kotlinx.android.synthetic.main.activity_splash_screen.*
import javax.inject.Inject


class SplashScreenActivity : BaseActivity() {

    companion object {
        private const val LONG_SPLASH_SCREEN_TIME: Long = 1500
        private const val LONG_SPLASH_SCREEN_TIME_SHORT: Long = 1000
        const val EXTRA_REMOVE_DATA = "EXTRA_REMOVE_DATA"
    }

    @Inject
    lateinit var mPrefRepository: PrefRepository
    @Inject
    lateinit var mNetworkAvailabilityChecker: NetworkAvailabilityChecker

    private var mDialog: MaterialDialog? = null

    private val UPDATE_REQUEST_CODE =1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


    }

    override fun onResume() {
        super.onResume()


//        if (intent?.extras?.containsKey(EXTRA_REMOVE_DATA) == true) {
//            VehicleTrackApplication.app?.startLogoutProcedure({
//                gotoNextActivity()
//            })
//            return
//        }
//
//        Handler().postDelayed(Runnable {
//            gotoNextActivity()
//        }, LONG_SPLASH_SCREEN_TIME)

        if (mNetworkAvailabilityChecker.isNetworkAvailable()) {

        // checkVersion()

            InAppsUpdate()
        } else {

            gotoNextActivity()
        }
    }

  /*   private fun checkVersion() {

        val database = FirebaseDatabase.getInstance().reference.child("build-version")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value.toString().toIntOrNull() ?: 0 > BuildConfig.VERSION_CODE) {

                    //  showUpgradeAlert()

                    //Support in-app updates
                    /// InAppsUpdate()

                } else {
                    gotoNextActivity()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                gotoNextActivity()
            }
        })
    }*/

    private fun gotoNextActivity() {
        ViewAnimator
                .animate(logo, powered)
                .alpha(1f, 0f)
                .scale(1f, 5f)
                .duration(750)
                .andAnimate(container)
                .alpha(1f, 0f)
                .onStop {
                    if (!mPrefRepository.isUserLoggedIn) {
                        val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {

                        /*val intent = Intent(this@SplashScreenActivity, HomeActivity2::class.java)
                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)*/

                        val intent = Intent(this@SplashScreenActivity, MonitoringActivity::class.java)
                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                    this@SplashScreenActivity.finish()
                }
                .start()
    }

  /*  private fun showUpgradeAlert() {
        mDialog = DialogHelper.getMessageDialog(this, "Alert",
                "You have a new update. Please update your app from Playstore")
                ?.positiveText("Update")
                ?.onPositive { dialog, which ->
                    dialog.dismiss()
                    openPlaystore()
                }
                ?.negativeText("Cancel")
                ?.onNegative { dialog, which ->
                    dialog.dismiss()
                    finish()
                }
                ?.show()
    }*/


    private fun InAppsUpdate(){

        val appUpdateManager = AppUpdateManagerFactory.create(this)

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            ) {
                try {

                    mDialog = DialogHelper.getMessageDialog(this, "Alert",
                        "You have a new update. Please update your app from Playstore")
                        ?.positiveText("Update")
                        ?.cancelable(false)
                        ?.onPositive { dialog, which ->
                            openPlaystore()
                        }
                        ?.show()

                }catch (exception :IntentSender.SendIntentException){

                    Toast.makeText(this@SplashScreenActivity,"exception: ${exception.toString()}",Toast.LENGTH_SHORT).show()
                }
            }else{

                gotoNextActivity()
            }
        }
    }

    private fun openPlaystore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }
}
