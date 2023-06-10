package com.singularity.trackmyvehicle.repository.implementation

import com.singularity.trackmyvehicle.model.apiResponse.v2.LoginResponse
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Created by Imran Chowdhury on 8/11/2018.
 */
@RunWith(RobolectricTestRunner::class)
class PrefRepositoryTest: BaseRepositoryTest() {

    private lateinit var mPrefRepo: PrefRepository

    @Before
    fun setUpPrefRepo() {
        mPrefRepo = mAppModule.providePreferenceRepository(RuntimeEnvironment.application)
    }

    @Test
    fun testNotNull() {
        assertNotNull(mPrefRepo)
        assertNotNull(mProfileFaker)
    }

    @Test
    fun testSaveApiTokenRetriveSameApiToken() {
        val apiToken = "thisisatoken"
        mPrefRepo.saveApiToken(apiToken)
        val apiTokenFromPrefRepo = mPrefRepo.apiToken()
        assertEquals(apiToken, apiTokenFromPrefRepo)
    }

    @Test
    fun testSaveOtpTokenRetriveSameOtpToken() {
        val otpToken = "thisisaotptoken"
        mPrefRepo.saveOtpToken(otpToken)
        val otpTokenFromPrefRepo = mPrefRepo.otpToken()
        assertEquals(otpToken, otpTokenFromPrefRepo)
    }

    @Test
    fun testSaveFCMTokenRetriveSameFCMToken() {
        val fcmToken = "thisisafcmtoken"
        mPrefRepo.saveUnsetFCMToken(fcmToken)
        val fcmTokenFromPrefRepo = mPrefRepo.unsentFCMToken()
        assertEquals(fcmToken, fcmTokenFromPrefRepo)
    }

    @Test
    fun testSaveCurrentVehicleRetriveSameCurrentVehicle() {
        val bstId = "ThisIsACurrentVehicleBstId"
        val vrn = "ThisIsACurrentVehicleVrn"
        mPrefRepo.changeCurrentVehicle(bstId, vrn, "")

        assertEquals(bstId, mPrefRepo.currentVehicle())
        assertEquals(vrn, mPrefRepo.currentVehicleVrn())
    }

    @Test
    fun testSaveProfileRetriveSameProfile() {
        val profile = mProfileFaker.getSingleItem()
        mPrefRepo.saveProfile(profile)
        assertEquals(profile, mPrefRepo.profile())

        val user = LoginResponse.User()
        user.email = "xyz@user.com"
        user.name = "AAAA"
        user.phone = "1238374733"

        mPrefRepo.saveUser(user)
    }
}