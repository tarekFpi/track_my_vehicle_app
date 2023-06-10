package com.singularity.trackmyvehicle.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.afollestad.materialdialogs.MaterialDialog
import com.github.florent37.viewanimator.ViewAnimator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.BuildConfig
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.db.AppDb
import com.singularity.trackmyvehicle.fcm.FCMRepository
import com.singularity.trackmyvehicle.model.dataModel.LoginModel
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.utils.disableFCM
import com.singularity.trackmyvehicle.utils.enableFCM
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.LoginViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_monitoring.*
import javax.inject.Inject


class LoginActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var mAnalytics: AnalyticsViewModel
    @Inject
    lateinit var mFCMRepository: FCMRepository

    @Inject
    lateinit var executors: AppExecutors
    @Inject
    lateinit var mAppDb: AppDb
    @Inject
    lateinit var mPrefRepository: PrefRepository

    private var mDialog: MaterialDialog? = null

    lateinit var viewModel: LoginViewModel

    private var mLoginLiveData: LiveData<Resource<LoginModel>>? = null

    private lateinit var appPreference : AppPreference

    var userName : String = ""
    var password : String = ""
    var oneChecked = false

    private lateinit var relativelayout :RelativeLayout

    private val mLoginObserver = Observer<Resource<LoginModel>> { data ->
        when (data?.status) {
            Status.LOADING -> showLoading()
            Status.ERROR -> showDialogError(data.message ?: "")
            Status.SUCCESS -> loginSuccess(data?.data)
        }

    }

    private val RC_PERMISSION_CALL_PHONE = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)



        disableFCM()

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

        }

        mPrefRepository.saveApiToken("")
        mPrefRepository.saveUser(null)
        mPrefRepository.saveProfile(null)
        mPrefRepository.saveUserSource(UserSource.NOT_DETERMINED.identifier)
        mPrefRepository.saveCookie("")
        mPrefRepository.changeCurrentVehicle("", "", "")
        mPrefRepository.changeCurrentVehicle("", "", "", "")
        mPrefRepository.saveUserName("")
        mPrefRepository.savePasswordHash("")
        mPrefRepository.saveUnreadMessageCount(0)

         //relativelayout=findViewById(R.id.relative_layout)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(
                LoginViewModel::class.java
        )

        ViewAnimator.animate(imgLogo)
                .slideTop()
                .duration(750)
                .start()

        btnLogin.setOnClickListener {
            if (validateInput()) {
                postLogin()
            }


        }


        txtForgetPassword.setOnClickListener {
            ForgetPasswordActivity.intent(this)
        }

        txtVersionNumber.text = StringBuilder().apply {
            this.append("Version: ")
            this.append(BuildConfig.VERSION_NAME)
        }.toString()

        txtKnowMore.setOnClickListener {
            val wrapInScrollView = true
            val dialog = MaterialDialog.Builder(this)
                    .customView(R.layout.dialog_know_more, wrapInScrollView)
                    .backgroundColor(ContextCompat.getColor(this, R.color.colorDialogBackground))
                    .show()
            val dialogView = dialog.customView
            dialogView?.findViewById<Button>(R.id.btnCallUs)?.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this@LoginActivity,
                                Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@LoginActivity,
                            arrayOf(Manifest.permission.CALL_PHONE),
                            RC_PERMISSION_CALL_PHONE)
                    return@setOnClickListener
                }
                callPhoneNumber()
            }

            dialogView?.findViewById<Button>(R.id.btnVisitUs)?.setOnClickListener {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.VISIT_URL))
                    startActivity(browserIntent)
                } catch (ex: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(ex)
                    Toasty.warning(this@LoginActivity, "Could not open link. visit: ").show()
                }
            }
        }

        appPreference = AppPreferenceImpl(this)

        etUsername.setText(appPreference.getString(AppPreference.UserName))
        etPassword.setText(appPreference.getString(AppPreference.Password))
        saveLoginCheckBox.isChecked = appPreference.getBoolean(AppPreference.isRememberMe)
    }

    private fun validateInput(): Boolean {
        if (etUsername.text.isEmpty()) {
            Toasty.error(this@LoginActivity, "Please enter a valid username", Toast.LENGTH_SHORT,
                    true).show()
            return false
        }
        if (etPassword.text != null && etPassword.text!!.isEmpty()) {
            Toasty.error(this@LoginActivity, "Please enter a valid password", Toast.LENGTH_SHORT,
                    true).show()
            return false
        }
        return true
    }
//
    @SuppressLint("MissingPermission")
    private fun callPhoneNumber() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + Constants.PHONE_NUMBER)
        this@LoginActivity.startActivity(intent)
    }

    private fun postLogin() {
        getLoginLiveData()
    }

    private fun getLoginLiveData() {
        mLoginLiveData?.removeObserver(mLoginObserver)
        mLoginLiveData = viewModel.login(etUsername.text.toString(), etPassword.text.toString())
        mLoginLiveData?.observe(this, mLoginObserver)
    }

    private fun loginSuccess(data: LoginModel?) {
        mAnalytics.login()
        dismissDialog()
        rememberMe(data)

        enableFCM(mFCMRepository, appPreference, mPrefRepository)
        //startActivity(Intent(this@LoginActivity, HomeActivity2::class.java))
        //startActivity(Intent(this@LoginActivity, VehicleMonitoringActivity::class.java))

       Toasty.success(this@LoginActivity, "Login Success", Toast.LENGTH_SHORT,
            true).show()

        val intent = Intent(this, MonitoringActivity::class.java)
        val bundle = Bundle()
        bundle.putBoolean("isAfterLogin", true)
        intent.putExtras(bundle)
        startActivity(intent)
        this@LoginActivity.finish()



    }

    private fun rememberMe(data: LoginModel?) {

        appPreference.setString(AppPreference.SignInName, data?.userSignInName.toString())

        userName = etUsername.text.toString()
        password = etPassword.text.toString()

        if(saveLoginCheckBox.isChecked){
            oneChecked = true
            appPreference.setBoolean(AppPreference.isRememberMe, oneChecked)
            appPreference.setString(AppPreference.UserName, userName)
            appPreference.setString(AppPreference.Password, password)
        }else{
            oneChecked = false
            appPreference.setBoolean(AppPreference.isRememberMe, oneChecked)
            appPreference.setString(AppPreference.UserName, "")
            appPreference.setString(AppPreference.Password, "")
        }
    }

    private fun showDialogError(msg: String) {
        dismissDialog()
        mDialog = DialogHelper.getMessageDialog(this, getString(R.string.title_error), msg)
                ?.positiveText(getString(R.string.action_ok))
                ?.show()
    }

    private fun showLoading() {
        dismissDialog()
        mDialog = DialogHelper.getLoadingDailog(this, getString(R.string.msg_hold_on),
                getString(R.string.msg_signin_in))
                ?.show()
    }

    private fun dismissDialog() {
        if (mDialog?.isShowing == true) {
            mDialog?.dismiss()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            RC_PERMISSION_CALL_PHONE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    callPhoneNumber()
                }
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
