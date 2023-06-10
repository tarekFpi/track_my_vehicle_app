package com.singularity.trackmyvehicle.db.dao

import com.singularity.trackmyvehicle.testhelper.TestHelper
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Created by Imran Chowdhury on 8/8/2018.
 */
@RunWith(RobolectricTestRunner::class)
class ReportsDaoTest : BaseDbTest() {
    private lateinit var mReportsDao: ReportsDao

    @Before
    fun initiateDao() {
        mReportsDao = mAppDb.reportDao()
    }

    @Test
    fun testNothingNull() {
        Assert.assertNotNull(mAppDb)
        Assert.assertNotNull(mFaker)
        assertNotNull(mReportsDao)
        assertNotNull(mDistanceFaker)
        assertNotNull(mSpeedAlertReportFaker)
        assertNotNull(mExpenseHeaderFaker)
        assertNotNull(mExpenseFaker)
    }

    @Test
    fun testSaveDistanceReportListAndRetrive() {
        val distanceReportList = mDistanceFaker.getList(5)
        mReportsDao.saveDistanceReport(distanceReportList)
        val distanceReportListFromDb = mReportsDao
                .getDistanceReport(distanceReportList[3].bstId, distanceReportList[3].date)
        assertEquals(1, distanceReportListFromDb.size)
        mReportsDao.deleteAllDistanceReport()
        val distanceReport = mDistanceFaker.getSingleItem()
        mReportsDao.saveDistanceReport(distanceReport)
        val distanceReportFromDb = mReportsDao
                .getDistanceReport(distanceReport.bstId, distanceReport.date)
        assertEquals(1, distanceReportFromDb.size)
        assertEquals(distanceReport, distanceReportFromDb[0])
        val liveData = mReportsDao
                .getDistanceReportAsnyc(distanceReport.bstId, distanceReport.date)
        assertEquals(distanceReport, getBlockingValue(liveData)[0])
        val date = distanceReport.date
        val distanceLiveData = mReportsDao.getTotalDistance(distanceReport.bstId, "%$date%")
        assertEquals(2.0, getBlockingValue(distanceLiveData))
    }

    @Test
    fun testSaveSpeedReportAndRetriveSameValue() {
        val speedAlertReportList = mSpeedAlertReportFaker.getList(10)
        mReportsDao.saveSpeedReport(speedAlertReportList)
        val speedAlertReportListFromDb = mReportsDao.getSpeedReport(speedAlertReportList[3].bstId,
                speedAlertReportList[3].date)
        assertEquals(1, speedAlertReportListFromDb.size)
        val speedAlertReport = mSpeedAlertReportFaker.getSingleItem()
        mReportsDao.saveSpeedReport(speedAlertReport)
        val speedAlertReportListFromDb2 = mReportsDao.getSpeedReport(speedAlertReport.bstId,
                speedAlertReport.date)
        assertEquals(1, speedAlertReportListFromDb2.size)
        val date = speedAlertReport.date
        val liveData = mReportsDao.getSpeedReportAsnyc(speedAlertReport.bstId, "%$date%")
        val speedAlertReportListFromDb3 = getBlockingValue(liveData)
        assertEquals(1, speedAlertReportListFromDb3.size)

        //SpeedViolation test
        val speedViolationList = mReportsDao.getSpeedViolation(speedAlertReport.bstId, date, date)
        assertEquals(1, speedViolationList.size)

        mReportsDao.deleteAllSpeedAlert()

        val speedAlertReportListFromDb4 = mReportsDao.getSpeedReport(speedAlertReport.bstId,
                speedAlertReport.date)

        assertEquals(0, speedAlertReportListFromDb4.size)
    }

    @Test
    fun testTotalSpeedCountByLatitude() {
        val speedAlertReportList = mSpeedAlertReportFaker.getList(20)
        mReportsDao.saveSpeedReport(speedAlertReportList)
        val date = speedAlertReportList[4].date
        val liveData = mReportsDao.getTotalSpeed(speedAlertReportList[4].bstId, "%$date%")
        val value = getBlockingValue(liveData)
        assertEquals(1, value)
    }

    @Test
    fun testExpenseHeaderDB() {
        val expenseHeaderList = mExpenseHeaderFaker.getList(10)
        mReportsDao.saveExpenseHeader(expenseHeaderList)
        val expenseHeaderListFromDb = mReportsDao.expenseHeader
        assertEquals(expenseHeaderList.size, expenseHeaderListFromDb.size)
        assertEquals(expenseHeaderList, expenseHeaderListFromDb)

        val expenseHeaderLivedata = mReportsDao.expenseHeaderAsync
        val expenseHeaderListFromDb2 = getBlockingValue(expenseHeaderLivedata)
        assertEquals(expenseHeaderList.size, expenseHeaderListFromDb2.size)
        assertEquals(expenseHeaderList, expenseHeaderListFromDb2)
    }

    @Test
    fun testSpeedViolationReport() {
        val speedAlertReportList = mSpeedAlertReportFaker.getList(10)
        mReportsDao.saveSpeedReport(speedAlertReportList)

        val date1 = TestHelper.getPreviousDate()
        val date2 = TestHelper.getDate()
        val speedAlertReportListFromDb = mReportsDao.getSpeedViolationReports(
                speedAlertReportList[3].bstId,
                date1,
                date2
        )
        assertEquals(1, speedAlertReportListFromDb.size)
    }

    @Test
    fun testExpenseRelatedDaoMetods() {
        val expenseList = mExpenseFaker.getList(20)
        mReportsDao.saveExpense(expenseList)
        val expense = mExpenseFaker.getSingleItem()
        mReportsDao.saveExpense(expense)

        val date = expense.date

        val expenseListFromDb = mReportsDao.getExpense(
                expense.bstid,
                "%$date%"
        )
        assertEquals(1, expenseListFromDb.size)
        assertEquals(expense, expenseListFromDb[0])

        val liveData = mReportsDao.getExpenseAsync(
                expense.bstid,
                "%$date%"
        )

        val expenseListFromDb2 = getBlockingValue(liveData)

        assertEquals(1, expenseListFromDb2.size)
        assertEquals(expense, expenseListFromDb2[0])

        mReportsDao.deleteAllExpense()
        val expenseListFromDb3 = mReportsDao.getExpense(
                expense.bstid,
                "%$date%"
        )
        assertEquals(0, expenseListFromDb3.size)
    }
}