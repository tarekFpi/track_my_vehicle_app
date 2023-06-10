package com.singularity.trackmyvehicle.viewmodel

import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.entity.Vehicle
import com.singularity.trackmyvehicle.model.entity.VehicleRoute
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.testhelper.TestHelper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

/**
 * Created by Imran Chowdhury on 8/16/2018.
 */
class VehiclesViewModelTest: BaseViewModelTest() {

    private lateinit var mVehiclesViewModel: VehiclesViewModel

    @Before
    fun setUp() {
//        mVehiclesViewModel = VehiclesViewModel(mVehicleRepo  mPrefRepo)
    }

    @Test
    fun test_notNull() {
        assertNotNull(mVehiclesViewModel)
    }

    @Test
    fun test_getVehiclesWithMaxCountTen() {
        val liveData = MutableLiveData<List<Terminal>>()
        val list = mVehicleFaker.getList(10).map { Terminal() }
        liveData.value = list

        `when`(mVehicleRepo.getVehicles(10)).thenReturn(liveData)

        mVehiclesViewModel.fetch()
        val vehicleLiveData = mVehiclesViewModel.getVehicles(10)
        val listOfVehicles = getBlockingValue(vehicleLiveData)

        assertEquals(10, listOfVehicles.size)
        assertEquals(list, listOfVehicles)
    }

    @Test
    fun test_getCurrentVehicleStatus() {
        val liveData = MutableLiveData<Resource<VehicleStatus>>()
        val vehicleStatus = mVehicleStatusFaker.getSingleItem()
        liveData.value = Resource.success(vehicleStatus)

        `when`(mPrefRepo.currentVehicle()).thenReturn("BSTID009")
        `when`(mVehicleRepo.getVehicleStatus("BSTID009")).thenReturn(liveData)

        val vehicleStatusLiveData = mVehiclesViewModel.getCurrentVehicleStatus()
        val resource = getBlockingValue(vehicleStatusLiveData)

        assertEquals(Status.SUCCESS, resource.status)
        assertEquals(vehicleStatus, resource?.data)
    }

    @Test
    fun test_getCurrentVehicleRoutes() {
        val liveData = MutableLiveData<Resource<List<VehicleRoute>>>()
        val vehicleRouteList = mVehicleRouteFaker.getList()
        liveData.value = Resource.success(vehicleRouteList)

        `when`(mPrefRepo.currentVehicle()).thenReturn("BSTID0009")
        `when`(mVehicleRepo.fetchVehicleRoutes("BSTID0009", TestHelper.getDate()))
                .thenReturn(liveData)

        mVehiclesViewModel.changeCurrentVehicle("BSTID0009", "vrnString", "")
        val vehicleRouteLiveData = mVehiclesViewModel.getCurrentVehicleRoutes(TestHelper.getDate())
        val vehicleRouteResource = getBlockingValue(vehicleRouteLiveData)

        assertEquals(Status.SUCCESS, vehicleRouteResource.status)
        assertEquals(vehicleRouteList, vehicleRouteResource?.data)
        assertEquals(5, vehicleRouteResource?.data?.size)

        val vehicleRouteLiveDataById = mVehiclesViewModel.getVehicleRoutes("BSTID0009", TestHelper.getDate())
        val vehicleRouteResourceById = getBlockingValue(vehicleRouteLiveDataById)
        assertEquals(Status.SUCCESS, vehicleRouteResourceById.status)
        assertEquals(vehicleRouteList, vehicleRouteResourceById?.data)
        assertEquals(5, vehicleRouteResourceById?.data?.size)
    }

    @Test
    fun test_getCurrentVehicle() {
        val vehicle2 = mVehicleFaker.getSingleItem()
        var vehicle: Terminal? = null
//        `when`(mVehicleRepo.getCurrentVehicle {
//            vehicle = it
//        })

        mVehiclesViewModel.getCurrentVehicle {
            vehicle = it
        }
    }
}