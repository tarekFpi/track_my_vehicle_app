package com.singularity.trackmyvehicle.db.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.singularity.trackmyvehicle.db.AppDb
import com.singularity.trackmyvehicle.faker.FakerFaktory
import com.singularity.trackmyvehicle.faker.model.*
import junit.framework.Assert.assertNotNull
import one.equinox.fritterfactory.FritterFactory
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by Imran Chowdhury on 8/8/2018.
 */
@RunWith(RobolectricTestRunner::class)
abstract class BaseDbTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    protected lateinit var mAppDb: AppDb
    protected lateinit var mFaker: FritterFactory
    protected lateinit var mFeedbackFaker: FeedbackFaker
    protected lateinit var mFeedbackHeaderFaker: FeedbackHeaderFaker
    protected lateinit var mFeedbackRemarkFaker: FeedbackRemarkFaker
    protected lateinit var mDistanceFaker: DistanceReportFaker
    protected lateinit var mSpeedAlertReportFaker: SpeedAlertReportFaker
    protected lateinit var mExpenseHeaderFaker: ExpenseHeaderFaker
    protected lateinit var mExpenseFaker: ExpenseFaker
    protected lateinit var mVehicleFaker: VehicleFaker
    protected lateinit var mVehicleRouteFaker: VehicleRouteFaker

    @Before
    fun setUpDb() {
        mAppDb = Room
                .inMemoryDatabaseBuilder(RuntimeEnvironment.application, AppDb::class.java)
                .allowMainThreadQueries()
                .build()

        mFaker = FakerFaktory().create()
        mFeedbackFaker = FeedbackFaker(mFaker)
        mFeedbackHeaderFaker = FeedbackHeaderFaker(mFaker)
        mFeedbackRemarkFaker = FeedbackRemarkFaker(mFaker)
        mDistanceFaker = DistanceReportFaker(mFaker)
        mSpeedAlertReportFaker = SpeedAlertReportFaker(mFaker)
        mExpenseHeaderFaker = ExpenseHeaderFaker(mFaker)
        mExpenseFaker = ExpenseFaker(mFaker)
        mVehicleFaker = VehicleFaker(mFaker)
        mVehicleRouteFaker = VehicleRouteFaker(mFaker)
    }

    @Test
    fun testNotNull() {
        assertNotNull(mAppDb)
        assertNotNull(mFaker)
        assertNotNull(mFeedbackFaker)
        assertNotNull(mFeedbackHeaderFaker)
        assertNotNull(mFeedbackRemarkFaker)
        assertNotNull(mSpeedAlertReportFaker)
        assertNotNull(mExpenseHeaderFaker)
        assertNotNull(mExpenseFaker)
        assertNotNull(mVehicleFaker)
        assertNotNull(mVehicleRouteFaker)
    }

    protected fun <T> getBlockingValue(liveData: LiveData<T>): T {
        val data = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(t: T?) {
                data[0] = t
                latch.countDown()
                liveData.removeObserver(this) // To change body of created functions use File | Settings | File Templates.
            }
        }
        liveData.observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)

        return data[0] as T
    }

    @After
    fun closeDb() {
        mAppDb.close()
    }
}