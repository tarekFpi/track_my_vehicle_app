package com.singularity.trackmyvehicle.faker.model

import com.singularity.trackmyvehicle.faker.provider.DateStringProvider
import com.singularity.trackmyvehicle.faker.provider.DoubleStringProvider
import com.singularity.trackmyvehicle.model.entity.Vehicle
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.lorem.WordProvider

/**
 * Created by Imran Chowdhury on 8/9/2018.
 */

class VehicleFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<Vehicle> {

        val list = factory.buildList(Vehicle::class.java, count)

        list.forEach {
        }

        return list
    }

    fun getSingleItem(): Vehicle {
        val model = factory.build(Vehicle::class.java)
        return model
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<Vehicle> {
            val outletMold = MapMold()
            outletMold.put("bid", WordProvider(3))
            outletMold.put("bstid", WordProvider(5))
            outletMold.put("vrn", WordProvider(5))
            outletMold.put("sim", WordProvider(5))
            outletMold.put("expiryDate", DateStringProvider())
            outletMold.put("dueAmount", DoubleStringProvider())
            return ModelProvider<Vehicle>(factory, Vehicle::class.java, outletMold)
        }
    }
}