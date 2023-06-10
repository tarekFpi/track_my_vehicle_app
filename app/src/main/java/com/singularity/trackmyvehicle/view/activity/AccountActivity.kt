package com.singularity.trackmyvehicle.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.singularity.trackmyvehicle.BuildConfig
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.Constants.REFUND_POLICY
import com.singularity.trackmyvehicle.Constants.URL_TERMS_AND_CONDITIONS
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.model.event.LogoutEvent
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.fragment.AccountFragment
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.ProfileViewModel
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.activity_login.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class AccountActivity : AppCompatActivity() {

    @Inject
    lateinit var mProfileViewModel: ProfileViewModel
    @Inject
    lateinit var mAnalytics: AnalyticsViewModel
    @Inject
    lateinit var userSource: UserSource

    @Inject
    lateinit var mPrefRepository: PrefRepository

    private lateinit var appPreference : AppPreference

    var signInName : String? = ""

    var BstId : String? = ""

    var SELECT_PICTURE = 200

    private var mDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        appPreference = AppPreferenceImpl(this)
        signInName = appPreference.getString(AppPreference.SignInName)


        updateUi(mProfileViewModel.getProfile())

        mProfileViewModel.fetchProfileInformation().observe(this,
                Observer { data ->
                    if (data == null)
                        return@Observer
                    if (data.data != null) {
                        updateUi(data.data)
                    }
                })

        selectImageAndPreview()

        updateName()

        updatePassword()

        helpLine()

        supportTicket()

        termsAndConditions()

        refundPolicies()

        logOut()

        appVersion()
    }

    private fun appVersion() {
        var versionName = BuildConfig.VERSION_NAME
        textView_versionName.text = "${getString(R.string.app_version)} $versionName"
    }

    private fun termsAndConditions() {
        textView_termsAndConditions.setOnClickListener {
            browsWebUrl(URL_TERMS_AND_CONDITIONS)
        }
    }

    private fun refundPolicies() {
        textView_refundPolicies.setOnClickListener {
            browsWebUrl(REFUND_POLICY)
        }
    }

    private fun browsWebUrl(url : String){
        var browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun supportTicket() {
        textView_supportTicket.setOnClickListener {
            FeedbackActivity.intent(this, FeedbackActivity.TAG_FEEDBACK_ADD)
        }
    }

    private fun helpLine() {
        textView_helpLine.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@AccountActivity,
                            Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@AccountActivity,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        HelpAndSupportActivity.RC_PERMISSION_CALL_PHONE)
                return@setOnClickListener
            }
            callPhoneNumber()
        }
    }

    @SuppressLint("MissingPermission")
    private fun callPhoneNumber() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + Constants.SUPPORT_PHONE_NUMBER)
        this@AccountActivity.startActivity(intent)
    }

    private fun selectImageAndPreview() {
        imageView_upload_image.setOnClickListener {
            imageChooser()
        }
    }

    private fun imageChooser() {
        var intent : Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK){

            if(requestCode == SELECT_PICTURE){
                var selectedImageUri : Uri? = data?.data

                if(selectedImageUri != null){
                    imageView_profile_image.setImageURI(selectedImageUri)
                }
            }
        }
    }


    private fun logOut() {
        layout_logout.setOnClickListener {

            mDialog = DialogHelper.getMessageDialog(this, "Confirm Logout",
                "Are you sure you want to logout?")
                ?.positiveText("Yes")
                ?.cancelable(false)
                ?.onPositive { dialog, which ->
                    dialog.dismiss()
                    //finish()
                    mAnalytics.logOut()
                    mProfileViewModel.logout {}
                    EventBus.getDefault().post(LogoutEvent())
                }
                ?.negativeText("No")
                ?.onNegative { dialog, which ->
                    dialog.dismiss()

                }
                ?.show()




        }
    }

    private fun updateUi(profile: Profile?) {
        textView_email.text = if (profile?.email?.isEmpty() == true) {
            "N/A"
        } else {
            profile?.email
        }
        textView_MobileNumber.text = if (profile?.mobile?.isEmpty() == true) {
            "N/A"
        } else {
            profile?.mobile
        }
        textView_signInName.text = if(signInName?.isEmpty() == true){
            "N/A"
        } else {
            signInName
        }
    }

    companion object {
        fun newInstance(): AccountFragment {
            val fragment = AccountFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        mAnalytics.accountFragmentViewed()
        mProfileViewModel.fetchOrGetProfileInformation().observe(this, Observer {
            textView_value_name.text = it.data?.name ?: "Bondstein Technologies Ltd."
        })
    }

    private fun updatePassword() {
        imageView_editPassword.setOnClickListener {
            var intent = Intent(this@AccountActivity, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateName() {
        imageView_editName.setOnClickListener {
            var intent = Intent(this@AccountActivity, UpdateNameActivity::class.java)
            var bundle : Bundle = Bundle()
            bundle.putString("name", textView_value_name.text.toString())
            bundle.putString("cookie", mPrefRepository.cookie)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }
}