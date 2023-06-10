package com.singularity.trackmyvehicle.viewmodel

import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.dataModel.SpeedViolationModel
import com.singularity.trackmyvehicle.model.entity.DistanceReport
import com.singularity.trackmyvehicle.model.entity.Expense
import com.singularity.trackmyvehicle.model.entity.ExpenseHeader
import com.singularity.trackmyvehicle.model.entity.SpeedAlertReport
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.testhelper.TestHelper
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

/**
 * Created by Imran Chowdhury on 8/16/2018.
 */
class ReportsViewModelTest: BaseViewModelTest() {

    private lateinit var mReportsViewModel: ReportsViewModel

    @Before
    fun testSetUp() {
        mReportsViewModel = ReportsViewModel(mReportRepo, mPrefRepo)
    }

    @Test
    fun test_notNull() {
        assertNotNull(mReportsViewModel)
    }

    @Test
    fun test_fetchCurrentVehicleDistanceReport() {
        val distanceReportList = mDistanceReportFaker.getList()
        val liveData = MutableLiveData<Resource<List<DistanceReport>>>()
        liveData.value = Resource.success(distanceReportList)
        `when`(mPrefRepo.currentVehicle()).thenReturn("BSTID123")
        `when`(mReportRepo.fetchDistanceReport("BSTID123", TestHelper.getDate())).thenReturn(liveData)

        val distanceReportLiveData = mReportsViewModel
                .fetchCurrentVehicleDistanceReport(TestHelper.getDateByDateTime())
        val distanceReportResource = getBlockingValue(distanceReportLiveData)

        assertEquals(Status.SUCCESS, distanceReportResource.status)
        assertEquals(5, distanceReportResource?.data?.size)
        assertEquals(distanceReportList, distanceReportResource?.data)
    }

    @Test
    fun test_fetchCurrentVehicleSpeedReport() {
        val liveData = MutableLiveData<Resource<List<SpeedViolationModel>>>()
        val speedViolationList = mSpeedViolationFaker.getList()
        liveData.value = Resource.success(speedViolationList)
        `when`(mPrefRepo.currentVehicle()).thenReturn("BSTID123")
        `when`(mReportRepo.fetchSpeedReport("BSTID123", TestHelper.getDate()))
                .thenReturn(liveData)

        val speedViolationLiveData = mReportsViewModel
                .fetchCurrentVehicleSpeedReport(TestHelper.getDateByDateTime())
        val speedViolationResource = getBlockingValue(speedViolationLiveData)

        assertEquals(Status.SUCCESS, speedViolationResource.status)
        assertEquals(5, speedViolationResource?.data?.size)
        assertEquals(speedViolationList, speedViolationResource?.data)
    }

    @Test
    fun test_fetchCurrentVehicleExpense() {
        val liveData = MutableLiveData<Resource<List<Expense>>>()
        val expenseList = mExpenseFaker.getList(10)
        liveData.value = Resource.success(expenseList)
        `when`(mPrefRepo.currentVehicle()).thenReturn("BSTID123")
        `when`(mReportRepo.fetchExpense("BSTID123", TestHelper.getDate())).thenReturn(liveData)

        val expenseListLiveData = mReportsViewModel.fetchCurrentVehicleExpense(TestHelper.getDate())
        val expenseListResource = getBlockingValue(expenseListLiveData)

        assertEquals(Status.SUCCESS, expenseListResource.status)
        assertEquals(expenseList, expenseListResource?.data)
        assertEquals(10, expenseListResource?.data?.size)
    }

    @Test
    fun test_fetchCurrentVehicleSpeedViolatoint() {
        val liveData = MutableLiveData<Resource<List<SpeedAlertReport>>>()
        val speedAlertReportList = mSpeedAlertReportFaker.getList(20)
        liveData.value = Resource.error("Failure", speedAlertReportList)
        `when`(mPrefRepo.currentVehicle()).thenReturn("BSTID123")
        `when`(mReportRepo.fetchSpeedViolations("BSTID123", TestHelper.getDate()))
                .thenReturn(liveData)

        val speedAlertReportListLiveData = mReportsViewModel
                .fetchCurrentVehicleSpeedViolatoint(TestHelper.getDateByDateTime())
        val speedAlertListResource = getBlockingValue(speedAlertReportListLiveData)

        assertEquals(Status.ERROR, speedAlertListResource.status)
        assertEquals("Failure", speedAlertListResource.message)
        assertEquals(speedAlertReportList, speedAlertListResource.data)
        assertEquals(20, speedAlertListResource.data?.size)
    }

    @Test
    fun test_fetchExpenseHeader() {
        val liveData = MutableLiveData<Resource<List<ExpenseHeader>>>()
        val expenseHeaderList = mExpenseHeaderFaker.getList(15)
        liveData.value = Resource.success(expenseHeaderList)

        `when`(mReportRepo.fetchExpenseHeader()).thenReturn(liveData)

        val expenseHeaderListLiveData = mReportsViewModel.fetchExpenseHeader()
        val expenseHeaderListResource = getBlockingValue(expenseHeaderListLiveData)

        assertEquals(Status.SUCCESS, expenseHeaderListResource.status)
        assertEquals(15, expenseHeaderListResource.data?.size)
    }

    @Test
    fun test_postCreateExpense() {
        val liveData = MutableLiveData<Resource<GenericApiResponse<String>>>()
        val response = GenericApiResponse<String>()
        response.code = "200"
        response.data = "Success"
        response.userMessage = "Saved"
        liveData.value = Resource.success(response)
        `when`(mReportRepo.postCreateExpense("BSTID007", TestHelper.getDate(), "100", 2, "message"))
                .thenReturn(liveData)
        `when`(mPrefRepo.currentVehicle()).thenReturn("BSTID007")

        val expenseCurentVehicleLiveData = mReportsViewModel
                .postCreateExpenseForCurrentVehicle(TestHelper.getDate(), "100", 2, "message")
        val expenseCurentVehicleResource = getBlockingValue(expenseCurentVehicleLiveData)

        assertEquals(Status.SUCCESS, expenseCurentVehicleResource.status)
        assertEquals("200", expenseCurentVehicleResource.data?.code)
        assertEquals("Success", expenseCurentVehicleResource.data?.data)

        val expenseAnyVehicleLiveData = mReportsViewModel
                .postCreateExpense("BSTID007", TestHelper.getDate(), "100", 2, "message")
        val expenseAnyVehicleResource = getBlockingValue(expenseAnyVehicleLiveData)

        assertEquals(Status.SUCCESS, expenseAnyVehicleResource.status)
        assertEquals("200", expenseAnyVehicleResource.data?.code)
        assertEquals("Success", expenseAnyVehicleResource.data?.data)

    }
}