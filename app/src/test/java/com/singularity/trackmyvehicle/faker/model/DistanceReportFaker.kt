package com.singularity.trackmyvehicle.faker.model

import com.singularity.trackmyvehicle.faker.provider.DateStringProvider
import com.singularity.trackmyvehicle.faker.provider.DoubleStringProvider
import com.singularity.trackmyvehicle.model.entity.DistanceReport
import com.singularity.trackmyvehicle.model.entity.Feedback
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.basic.DateProvider
import one.equinox.fritterfactory.providers.lorem.WordProvider
import one.equinox.fritterfactory.providers.primitives.DoubleProvider
import one.equinox.fritterfactory.providers.primitives.IntegerProvider
import java.text.DateFormat

/**
 * Created by Imran Chowdhury on 8/8/2018.
 */

class DistanceReportFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<DistanceReport> {

        val list = factory.buildList(DistanceReport::class.java, count)

        list.forEach {
            it.km = "2.0"
        }

        return list
    }

    fun getSingleItem(): DistanceReport {
        val model = factory.build(DistanceReport::class.java)
        model.km = "2.0"
        return model
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<DistanceReport> {
            val outletMold = MapMold()
            outletMold.put("bstId", WordProvider())
            outletMold.put("km", DoubleStringProvider())
            outletMold.put("date", DateStringProvider())
            return ModelProvider<DistanceReport>(factory, DistanceReport::class.java, outletMold)
        }
    }
}