package com.singularity.trackmyvehicle

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.apiResponse.v3.GenericResponse
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
import com.singularity.trackmyvehicle.view.activity.HomeActivity2
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_switch_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class SwitchUserActivity : AppCompatActivity() {

    private var snackbar: Snackbar? = null
    @Inject
    lateinit var webService: WebService
    @Inject
    lateinit var prefRepository: PrefRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_switch_user)
        AndroidInjection.inject(this)
        btnSwitchUser.setOnClickListener {
            if (validate()) {
                switchUser()
            }
        }
        btnBack.setOnClickListener {
            startActivity(Intent(this@SwitchUserActivity, HomeActivity2::class.java))
            this@SwitchUserActivity.finish()
        }
    }

    private fun switchUser() {
        showErrorSnackbar("Please wait", true)

        webService.switchUser(userEmail = etEmail.text.toString())
                .enqueue(object : Callback<GenericResponse<Any>> {
                    override fun onFailure(call: Call<GenericResponse<Any>>, t: Throwable) {
                        showErrorSnackbar("Failed.")
                    }

                    override fun onResponse(call: Call<GenericResponse<Any>>,
                                            response: Response<GenericResponse<Any>>) {
                        if (!response.isSuccessful) {
                            showErrorSnackbar("Failed.")
                            return
                        }
                        if (response.body()?.error?.code != 0) {
                            showErrorSnackbar("Failed. ${response.body()?.error?.description}")
                            return
                        }
                        prefRepository.saveUserSource(UserSource.VERSION_3.identifier)
                        prefRepository.saveUser(null)
                        prefRepository.savePasswordHash(null)
                        prefRepository.saveUserName(null)
                        startActivity(Intent(this@SwitchUserActivity, HomeActivity2::class.java))
                        this@SwitchUserActivity.finish()
                    }
                })
    }

    private fun showErrorSnackbar(text: String,indefinate: Boolean = false) {
        snackbar?.dismiss()
        snackbar = Snackbar.make(etEmail, text,
                if (indefinate) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_SHORT)
        snackbar?.view?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                ?.setTextColor(Color.WHITE)
        snackbar?.show()
        snackbar?.show()
    }

    private fun validate(): Boolean {
        if (etEmail.text.toString().isEmpty()) {
            showErrorSnackbar("Enter email address")
            return false
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@SwitchUserActivity, HomeActivity2::class.java))
    }
}