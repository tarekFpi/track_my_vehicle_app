package com.singularity.trackmyvehicle.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.singularity.trackmyvehicle.faker.FakerFaktory
import com.singularity.trackmyvehicle.faker.model.*
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.repository.interfaces.ReportsRepository
import com.singularity.trackmyvehicle.repository.interfaces.VehicleRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import com.singularity.trackmyvehicle.utils.NetworkAvailabilityChecker
import com.singularity.trackmyvehicle.utils.RandomStringGenerator
import com.singularity.trackmyvehicle.utils.firebase.FirebaseCredProvider
import junit.framework.Assert.assertNotNull
import one.equinox.fritterfactory.FritterFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by Imran Chowdhury on 8/16/2018.
 */
@RunWith(MockitoJUnitRunner::class)
abstract class BaseViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var mFaker: FritterFactory
    protected lateinit var mWebService: WebService
    protected lateinit var mNetworkAvailabilityChecker: NetworkAvailabilityChecker
    protected lateinit var mVehicleRepo: VehicleRepository
    protected lateinit var mReportRepo: ReportsRepository
    protected lateinit var mPrefRepo: PrefRepository
    protected lateinit var mVehicleFaker: VehicleFaker
    protected lateinit var mVehicleStatusFaker: VehicleStatusFaker
    protected lateinit var mVehicleRouteFaker: VehicleRouteFaker
    protected lateinit var mDistanceReportFaker: DistanceReportFaker
    protected lateinit var mSpeedViolationFaker: SpeedViolationFaker
    protected lateinit var mExpenseFaker: ExpenseFaker
    protected lateinit var mSpeedAlertReportFaker: SpeedAlertReportFaker
    protected lateinit var mExpenseHeaderFaker: ExpenseHeaderFaker
    protected lateinit var mProfileFaker: ProfileFaker
    protected lateinit var firebaseCredProvider: FirebaseCredProvider
    protected lateinit var mRandomStringGenerator: RandomStringGenerator

    @Before
    fun setUpTestObjects() {
        mFaker = FakerFaktory().create()
        mNetworkAvailabilityChecker = mock(NetworkAvailabilityChecker::class.java)
        mWebService = mock(WebService::class.java)
        mVehicleRepo = mock(VehicleRepository::class.java)
        mReportRepo = mock(ReportsRepository::class.java)
        mPrefRepo = mock(PrefRepository::class.java)
        mVehicleFaker = VehicleFaker(mFaker)
        mVehicleStatusFaker = VehicleStatusFaker(mFaker)
        mVehicleRouteFaker = VehicleRouteFaker(mFaker)
        mDistanceReportFaker = DistanceReportFaker(mFaker)
        mSpeedViolationFaker = SpeedViolationFaker(mFaker)
        mExpenseFaker = ExpenseFaker(mFaker)
        mSpeedAlertReportFaker = SpeedAlertReportFaker(mFaker)
        mExpenseHeaderFaker = ExpenseHeaderFaker(mFaker)
        mProfileFaker = ProfileFaker(mFaker)
        firebaseCredProvider = mock(FirebaseCredProvider::class.java)
        mRandomStringGenerator = mock(RandomStringGenerator::class.java)
    }

    @Test
    fun test_notNullObjects() {
        assertNotNull(mFaker)
        assertNotNull(mVehicleRepo)
        assertNotNull(mWebService)
        assertNotNull(mPrefRepo)
        assertNotNull(mVehicleFaker)
        assertNotNull(mVehicleStatusFaker)
        assertNotNull(mVehicleRouteFaker)
        assertNotNull(mDistanceReportFaker)
        assertNotNull(mSpeedViolationFaker)
        assertNotNull(mSpeedAlertReportFaker)
        assertNotNull(mExpenseHeaderFaker)
        assertNotNull(mProfileFaker)
    }

    protected fun <T> getBlockingValue(liveData: LiveData<T>?): T {
        val data = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(t: T?) {
                data[0] = t
                latch.countDown()
                liveData?.removeObserver(this) // To change body of created functions use File | Settings | File Templates.
            }
        }
        liveData?.observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)

        return data[0] as T
    }

}