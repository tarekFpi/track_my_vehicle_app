package com.singularity.trackmyvehicle.faker.model

import com.singularity.trackmyvehicle.faker.provider.BooleanStringProvider
import com.singularity.trackmyvehicle.faker.provider.DateStringProvider
import com.singularity.trackmyvehicle.faker.provider.IntegerStringProvider
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.lorem.WordProvider
import one.equinox.fritterfactory.providers.primitives.IntegerProvider

/**
 * Created by Imran Chowdhury on 8/14/2018.
 */
class VehicleStatusFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<VehicleStatus> {

        val list = factory.buildList(VehicleStatus::class.java, count)

        list.forEach {
        }

        return list
    }

    fun getSingleItem(): VehicleStatus {
        val model = factory.build(VehicleStatus::class.java)
        return model
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<VehicleStatus> {
            val outletMold = MapMold()
            outletMold.put("speed", IntegerStringProvider())
            outletMold.put("engineStatus", BooleanStringProvider())
            outletMold.put("updatedAt", DateStringProvider())
            outletMold.put("vrn", WordProvider())
            outletMold.put("bstid", WordProvider(5))
            outletMold.put("bid", IntegerProvider())
            return ModelProvider<VehicleStatus>(factory, VehicleStatus::class.java, outletMold)
        }
    }
}