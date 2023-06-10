package com.singularity.trackmyvehicle.faker.model

import com.singularity.trackmyvehicle.faker.provider.DateStringProvider
import com.singularity.trackmyvehicle.model.dataModel.SpeedViolationModel
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.primitives.IntegerProvider

/**
 * Created by Imran Chowdhury on 8/13/2018.
 */

class SpeedViolationFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<SpeedViolationModel> {

        val list = factory.buildList(SpeedViolationModel::class.java, count)

        list.forEach {
        }

        return list
    }

    fun getSingleItem(): SpeedViolationModel {
        val model = factory.build(SpeedViolationModel::class.java)
        return model
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<SpeedViolationModel> {
            val outletMold = MapMold()
            outletMold.put("violations", IntegerProvider(10, 15))
            outletMold.put("date", DateStringProvider())
            return ModelProvider<SpeedViolationModel>(factory, SpeedViolationModel::class.java, outletMold)
        }
    }
}