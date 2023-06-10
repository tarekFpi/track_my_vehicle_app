package com.singularity.trackmyvehicle.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.viewmodel.ProfileUpdateViewModel
import com.singularity.trackmyvehicle.viewmodel.VehicleRouteAnalyticsViewModel
import kotlinx.android.synthetic.main.activity_update_name.*
import javax.inject.Inject

class UpdateNameActivity : AppCompatActivity() {

    private lateinit var profileUpdateViewModel : ProfileUpdateViewModel
    var name : String? = ""

    var cookie : String = ""

    private var mDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_name)

        profileUpdateViewModel = ViewModelProvider(this).get(ProfileUpdateViewModel::class.java)

        setName()

        backPressed()

        setUpdateNameData()

        saveUpdatedName()
    }

    private fun saveUpdatedName() {
        button_saveName.setOnClickListener {
            profileUpdateViewModel.loadProfileData(this@UpdateNameActivity, cookie,editText_Name.text.toString(), " ", " ")
                    .observe(this, object : Observer<Profile>{
                        override fun onChanged(data: Profile?) {
                            mDialog = DialogHelper.getMessageDialog(this@UpdateNameActivity, "Success", "Name successfully changed")
                                    ?.positiveText("OK")
                                    ?.onPositive { dialog, which ->
                                        dialog.dismiss()
                                        this@UpdateNameActivity.finish()
                                    }
                                    ?.show()

                        }

                    })
        }
    }

    private fun setName() {
        name = this.intent.extras?.getString("name")?.trim()
        editText_Name.setText(name)
    }

    private fun setUpdateNameData() {
        cookie = this.intent.extras?.getString("cookie").toString()
    }

    private fun backPressed() {
        imageView_backButton.setOnClickListener {
            onBackPressed()
        }
    }
}