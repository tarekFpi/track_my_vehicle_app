package com.singularity.trackmyvehicle.view.dialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.preference.AppPreference
import com.singularity.trackmyvehicle.preference.AppPreferenceImpl
import com.singularity.trackmyvehicle.view.activity.HelpAndSupportActivity
import kotlinx.android.synthetic.main.fragment_suspend_expired.*

class SuspendExpiredDialog() :  DialogFragment()  {

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        suspendExpiredcloseBtn.setOnClickListener {
            dismiss()
        }

        SuspendedExpired_Btn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CALL_PHONE),
                    HelpAndSupportActivity.RC_PERMISSION_CALL_PHONE
                )
                return@setOnClickListener
            }
            callPhoneNumber()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("MissingPermission")
    private fun callPhoneNumber() {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + Constants.SUPPORT_PHONE_NUMBER)
        requireActivity().startActivity(intent)
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

        return inflater.inflate(R.layout.fragment_suspend_expired, container, false);
    }
}