package com.singularity.trackmyvehicle.view.activity

import android.Manifest
import com.singularity.trackmyvehicle.BuildConfig
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.singularity.trackmyvehicle.Constants
import com.singularity.trackmyvehicle.R
import kotlinx.android.synthetic.main.activity_help_and_support.*


class HelpAndSupportActivity : AppCompatActivity() {
    companion object {
        const val RC_PERMISSION_CALL_PHONE = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_and_support)
        setSupportActionBar(toolbar2)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnCreateSupportRequest.setOnClickListener {
            FeedbackActivity.intent(this, FeedbackActivity.TAG_FEEDBACK_ADD)
        }
        btnViewSupportRequest.setOnClickListener {
            FeedbackActivity.intent(this, FeedbackActivity.TAG_FEEDBACK_LIST)
        }



        btnTmvLite.setOnClickListener {

            val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.bondstein.tmv.lite") // missing 'http://' will cause crashed

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        if(BuildConfig.APPLICATION_ID.equals("com.bondstein.trackmyvehicle.trackmyvehicle")){
            btnTmvLite.visibility=View.VISIBLE
        }

        btnCallUs.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@HelpAndSupportActivity,
                            Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@HelpAndSupportActivity,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        RC_PERMISSION_CALL_PHONE)
                return@setOnClickListener
            }
            callPhoneNumber()
        }

        txtPhoneNumber.text = "Reach us at ${Constants.SUPPORT_PHONE_NUMBER}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            return when (it.itemId) {
                android.R.id.home -> {
                    finish()
                    true
                }
                else -> {
                    false
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RC_PERMISSION_CALL_PHONE -> {
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callPhoneNumber()
                } else {
                    Toast.makeText(this, "Permission denied to make call", Toast.LENGTH_SHORT)
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
        this@HelpAndSupportActivity.startActivity(intent)
    }
}
