package com.singularity.trackmyvehicle.repository.implementation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.singularity.trackmyvehicle.db.dao.FeedbackDao
import com.singularity.trackmyvehicle.db.dao.ReportsDao
import com.singularity.trackmyvehicle.db.dao.VehicleDao
import com.singularity.trackmyvehicle.di.AppModule
import com.singularity.trackmyvehicle.faker.FakerFaktory
import com.singularity.trackmyvehicle.faker.model.*
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.retrofit.webService.v2.WebService
import com.singularity.trackmyvehicle.utils.NetworkAvailabilityChecker
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
 * Created by Imran Chowdhury on 8/11/2018.
 */
@RunWith(MockitoJUnitRunner::class)
abstract class BaseRepositoryTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var mFaker: FritterFactory
    protected lateinit var mAppModule: AppModule
    protected lateinit var mProfileFaker: ProfileFaker
    protected lateinit var mFeedbackDao: FeedbackDao
    protected lateinit var mReportDao: ReportsDao
    protected lateinit var mVehicleDao: VehicleDao
    protected lateinit var mWebService: WebService
    protected lateinit var mPrefRepository: PrefRepository
    protected lateinit var mNetChecker : NetworkAvailabilityChecker
    protected lateinit var mExpenseHeaderFaker: ExpenseHeaderFaker
    protected lateinit var mFeedbackHeaderFaker: FeedbackHeaderFaker
    protected lateinit var mFeedbackFaker: FeedbackFaker
    protected lateinit var mFeedbackRemarkFaker: FeedbackRemarkFaker
    protected lateinit var mSpeedAlertReportFaker: SpeedAlertReportFaker
    protected lateinit var mVehicleFaker: VehicleFaker
    protected lateinit var mSpeedViolationFaker: SpeedViolationFaker
    protected lateinit var mDistanceReportFaker: DistanceReportFaker
    protected lateinit var mExpenseFaker: ExpenseFaker
    protected lateinit var mVehicleRouteFaker: VehicleRouteFaker
    protected lateinit var mVehicleStatusFaker: VehicleStatusFaker
    protected lateinit var mV3WebService: com.singularity.trackmyvehicle.retrofit.webService.v3.WebService


    @Before
    fun setUp() {
        mAppModule = AppModule()
        mFaker = FakerFaktory().create()
        mProfileFaker = ProfileFaker(mFaker)
        mNetChecker = mock(NetworkAvailabilityChecker::class.java)
        mFeedbackDao = mock(FeedbackDao::class.java)
        mVehicleDao = mock(VehicleDao::class.java)
        mReportDao = mock(ReportsDao::class.java)
        mV3WebService = mock(com.singularity.trackmyvehicle.retrofit.webService.v3.WebService::class.java)
        mWebService = mock(WebService::class.java)
        mPrefRepository = mock(PrefRepository::class.java)
        mExpenseHeaderFaker = ExpenseHeaderFaker(mFaker)
        mFeedbackHeaderFaker = FeedbackHeaderFaker(mFaker)
        mFeedbackFaker = FeedbackFaker(mFaker)
        mFeedbackRemarkFaker = FeedbackRemarkFaker(mFaker)
        mSpeedAlertReportFaker = SpeedAlertReportFaker(mFaker)
        mVehicleFaker = VehicleFaker(mFaker)
        mSpeedViolationFaker = SpeedViolationFaker(mFaker)
        mDistanceReportFaker = DistanceReportFaker(mFaker)
        mExpenseFaker = ExpenseFaker(mFaker)
        mVehicleStatusFaker = VehicleStatusFaker(mFaker)
        mVehicleRouteFaker = VehicleRouteFaker(mFaker)

    }

    @Test
    fun notNullObjects() {
        assertNotNull(mProfileFaker)
        assertNotNull(mFeedbackDao)
        assertNotNull(mWebService)
        assertNotNull(mPrefRepository)
        assertNotNull(mExpenseHeaderFaker)
        assertNotNull(mFeedbackHeaderFaker)
        assertNotNull(mFeedbackFaker)
        assertNotNull(mFeedbackRemarkFaker)
        assertNotNull(mSpeedAlertReportFaker)
        assertNotNull(mVehicleFaker)
        assertNotNull(mSpeedViolationFaker)
        assertNotNull(mDistanceReportFaker)
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