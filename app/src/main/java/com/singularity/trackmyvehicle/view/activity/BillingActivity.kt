package com.singularity.trackmyvehicle.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.afollestad.materialdialogs.MaterialDialog
import com.github.florent37.expectanim.ExpectAnim
import com.github.florent37.expectanim.core.Expectations
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.fragment.BottomNavFragment
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import kotlinx.android.synthetic.main.activity_billing.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.net.URLEncoder
import javax.inject.Inject


class BillingActivity : BaseActivity() {


    @Inject
    lateinit var mPrefRepository: PrefRepository
    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel
    @Inject
    lateinit var mAnalytics: AnalyticsViewModel
    @Inject
    lateinit var mAppExecutors: AppExecutors

    private var mDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_billing)

        updateHeader()

        webView.settings.javaScriptEnabled = true

        val mWebViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (view.url.equals(Constants.BASE_URL + "/app2018/billing_success.php")) {
                    mDialog?.dismiss()
                    mDialog = DialogHelper.getMessageDialog(this@BillingActivity, "Success", "Billing successfully finished.")
                            ?.positiveText("OK")
                            ?.onPositive { dialog, which ->
                                dialog.dismiss()
                                hideWebView()
                            }
                            ?.show()

                    mAnalytics.payBillSuccesful()
                    return
                }
                if (view.url.equals(Constants.BASE_URL + "/app2018/billing_failed.php")) {
                    mDialog?.dismiss()
                    mDialog = DialogHelper.getMessageDialog(this@BillingActivity, "Error", "Billing could not be finished.")
                            ?.positiveText("OK")
                            ?.onPositive { dialog, which ->
                                dialog.dismiss()
                                hideWebView()
                            }
                            ?.show()
                    mAnalytics.payBillFailed()
                    return
                }
            }
        }
        webView.webViewClient = mWebViewClient
        ExpectAnim()
                .expect(screenWebView).toBe(Expectations.outOfScreen(Gravity.END))
                .toAnimation()
                .setNow()


        btnProceed.setOnClickListener {

            val url = Constants.BASE_URL + "/app2018/billing_portal.php"
            val postData = "access_token=" + URLEncoder.encode(mPrefRepository.apiToken(), "UTF-8") +
                    "&bstid=" + URLEncoder.encode(mPrefRepository.currentVehicle(), "UTF-8")
//                    "&sim=" + URLEncoder.encode("01847214731", "UTF-8") +
//                    "&category=" + URLEncoder.encode("postpaid", "UTF-8") +
//                    "&amount=" + URLEncoder.encode("50", "UTF-8")
            webView.postUrl(url, postData.toByteArray())

            ExpectAnim()
                    .expect(screenIntroduction).toBe(Expectations.outOfScreen(Gravity.START))
                    .expect(screenWebView).toBe(Expectations.atItsOriginalPosition())
                    .toAnimation()
                    .start()
        }

        btnCloseWebView.setOnClickListener {
            hideWebView()
        }

        vehicleSelectionToolbarView.setChangeVehicleClickListener(View.OnClickListener {
            showVehicleSelectFragment()
        })
        vehicleSelectionToolbarView.setImgBackClickListener(View.OnClickListener {
            this.finish()
        })
    }

    private fun showVehicleSelectFragment() {
        val dialogFrag = BottomNavFragment.newInstance()
        dialogFrag.show(supportFragmentManager, dialogFrag.tag)
    }

    private fun hideWebView() {
        ExpectAnim()
                .expect(screenWebView).toBe(Expectations.outOfScreen(Gravity.END))
                .expect(screenIntroduction).toBe(Expectations.atItsOriginalPosition())
                .toAnimation()
                .start()
    }

    companion object {
        fun intent(context: Context) {
            context.startActivity(Intent(context, BillingActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        mAnalytics.payBillsScreenViewed()

    }

    override fun onPause() {
        super.onPause()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

    }

    private fun updateHeader() {
        vehicleSelectionToolbarView.setCurrentVehicleId(mPrefRepository.currentVehicle())

        mVehicleViewModel.getCurrentVehicle { _vehicle ->

            _vehicle?.let { vehicle ->
                mAppExecutors.mainThread {
                    try {
                        txtDueDate.text = DateTime.parse(vehicle.expiryDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toString("MMM dd, yyyy\nhh:mm a")
                    } catch (ex: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(ex)
                        txtDueDate.text = "N/A"
                    }
                    txtSimNo.text = vehicle.sim
                    txtDueAmount.text = vehicle.dueAmount
                }
            }

        }

    }

    @Subscribe
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {
        updateHeader()
    }
}
