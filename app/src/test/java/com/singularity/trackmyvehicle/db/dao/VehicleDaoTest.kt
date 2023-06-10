package com.singularity.trackmyvehicle.db.dao

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Created by Imran Chowdhury on 8/9/2018.
 */

@RunWith(RobolectricTestRunner::class)
class VehicleDaoTest : BaseDbTest() {
    private lateinit var mVehicleDao: VehicleDao

    @Before
    fun initiateDao() {
        mVehicleDao = mAppDb.vehicleDao()
    }

    @Test
    fun testNothingNull() {
        assertNotNull(mAppDb)
        assertNotNull(mFaker)
        assertNotNull(mVehicleDao)
        assertNotNull(mVehicleFaker)
        assertNotNull(mVehicleRouteFaker)
    }

    @Test
    fun testVehicleRelatedTestMethods() {
        val vehicleList = mVehicleFaker.getList(10)
        mVehicleDao.save(vehicleList)
        val vehicle = mVehicleFaker.getSingleItem()
        mVehicleDao.save(vehicle)
        vehicleList.add(vehicle)

        val liveData = mVehicleDao.vehicle
        val vehicleListFromDb = getBlockingValue(liveData)
        assertEquals(vehicleList.size, vehicleListFromDb.size)

        val liveData2 = mVehicleDao.getVehicleWithMaxCount(10)
        val vehicleListFromDb2 = getBlockingValue(liveData2)
        assertEquals(vehicleList.size-1, vehicleListFromDb2.size)

        val vehicleFromDb = mVehicleDao.getById(vehicle.bstid)
        assertEquals(vehicle, vehicleFromDb)

        val liveData3 = mVehicleDao.getByIdAsync(vehicle.bstid)
        val vehicleFromDb2 = getBlockingValue(liveData3)
        assertEquals(vehicle, vehicleFromDb2)

        mVehicleDao.deleteAllVehicle()
        val liveData4 = mVehicleDao.vehicle
        val vehicleListFromDb3 = getBlockingValue(liveData4)
        assertEquals(0, vehicleListFromDb3.size)

    }

    @Test
    fun testVehicleRouteRelatedTestMethods() {
        val vehicleRouteList = mVehicleRouteFaker.getList(10)
        mVehicleDao.saveRoute(vehicleRouteList)

        val vehicleRoute = mVehicleRouteFaker.getSingleItem()
        mVehicleDao.saveRoute(vehicleRoute)

        val vehicleRouteListFromDb = mVehicleDao.getRoutes(vehicleRoute.bstId, vehicleRoute.updatedAt)
        assertEquals(1, vehicleRouteListFromDb.size)
        assertEquals(vehicleRoute, vehicleRouteListFromDb[0])

        val liveData = mVehicleDao.getRoutesAsnyc(vehicleRoute.bstId, vehicleRoute.updatedAt)
        val vehicleRouteListFromDb2 = getBlockingValue(liveData)

        assertEquals(1, vehicleRouteListFromDb2.size)
        assertEquals(vehicleRoute, vehicleRouteListFromDb2[0])

        mVehicleDao.deleteAllRoute()
        val vehicleRouteListFromDb3 = mVehicleDao.getRoutes(vehicleRoute.bstId, vehicleRoute.updatedAt)
        assertEquals(0, vehicleRouteListFromDb3.size)
    }
}