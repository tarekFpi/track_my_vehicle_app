package com.singularity.trackmyvehicle.faker.model

import com.singularity.trackmyvehicle.faker.provider.BooleanStringProvider
import com.singularity.trackmyvehicle.faker.provider.DateStringProvider
import com.singularity.trackmyvehicle.faker.provider.DoubleStringProvider
import com.singularity.trackmyvehicle.faker.provider.IntegerStringProvider
import com.singularity.trackmyvehicle.model.entity.Vehicle
import com.singularity.trackmyvehicle.model.entity.VehicleRoute
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.lorem.WordProvider
import one.equinox.fritterfactory.providers.primitives.BooleanProvider
import one.equinox.fritterfactory.providers.primitives.IntegerProvider

/**
 * Created by Imran Chowdhury on 8/9/2018.
 */

class VehicleRouteFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<VehicleRoute> {

        val list = factory.buildList(VehicleRoute::class.java, count)

        list.forEach {
        }

        return list
    }

    fun getSingleItem(): VehicleRoute {
        val model = factory.build(VehicleRoute::class.java)
        return model
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<VehicleRoute> {
            val outletMold = MapMold()
            outletMold.put("bstid", WordProvider(5))
            outletMold.put("speed", DoubleStringProvider())
            outletMold.put("engineStatus", BooleanStringProvider())
            outletMold.put("updatedAt", DateStringProvider())
            outletMold.put("sl", IntegerProvider())
            return ModelProvider<VehicleRoute>(factory, VehicleRoute::class.java, outletMold)
        }
    }
}
