package com.singularity.trackmyvehicle.view.dialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.view.activity.HelpAndSupportActivity
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.ProfileViewModel
import kotlinx.android.synthetic.main.activity_account.*
import javax.inject.Inject

class SuspendDialog: DialogFragment() {


    var shown = false

    override fun show(manager: FragmentManager, tag: String?) {
        if (shown) return
        super.show(manager, tag)
        shown = true
    }

    override fun onDismiss(dialog: DialogInterface) {
        shown = false
        super.onDismiss(dialog)
    }

    @Inject
    lateinit var mProfileViewModel: ProfileViewModel

    @Inject
    lateinit var mAnalytics: AnalyticsViewModel

    private lateinit var appPreference : AppPreference


    var BstId : String? = ""

    var UserAddress : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        VehicleTrackApplication.appComponent?.inject(this)

        appPreference = AppPreferenceImpl(requireContext())

        BstId = appPreference.getBstId(AppPreference.bstId)

        return inflater.inflate(R.layout.fragment_terminal_suspended, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val helplineBtn = view.findViewById<MaterialButton>(R.id.helplineBtn)

        val paymentBtn = view.findViewById<MaterialButton>(R.id.paymentBtn)

        val closeBtn = view.findViewById<ImageButton>(R.id.closeBtn)

        closeBtn.setOnClickListener {
            dismiss()
        }

        helplineBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.CALL_PHONE),
                    HelpAndSupportActivity.RC_PERMISSION_CALL_PHONE
                )
                return@setOnClickListener
            }
            callPhoneNumber()
        }

        paymentBtn.setOnClickListener{


            mProfileViewModel.fetchOrGetProfileInformation().observe(this, Observer {

                if(it.data?.address.toString().equals("null")){

                    UserAddress ="Dhaka,Bangladesh"

                }else{
                    UserAddress =it.data?.address
                }
                val uri = Uri.parse("https://tmv.bondstein.com/?_Script=Payment/Request&PaymentRequestCarriers=${BstId}&PaymentRequestAmount=${0}&PaymentRequestName=${it.data?.name}&PaymentRequestMobile=${it.data?.mobile}&PaymentRequestEmail=${it.data?.email}&PaymentRequestStreet=${UserAddress}") // missing 'http://' will cause crashed

                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            })

        }

        super.onViewCreated(view, savedInstanceState)
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            HelpAndSupportActivity.RC_PERMISSION_CALL_PHONE -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callPhoneNumber()
                } else {
                    Toast.makeText(requireContext(), "Permission denied to make call", Toast.LENGTH_SHORT)
                        .show()
                }
                return
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun callPhoneNumber() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + Constants.SUPPORT_PHONE_NUMBER)
        requireActivity().startActivity(intent)
    }

}