package com.singularity.trackmyvehicle.faker.model

import com.singularity.trackmyvehicle.faker.provider.DateStringProvider
import com.singularity.trackmyvehicle.faker.provider.DoubleStringProvider
import com.singularity.trackmyvehicle.faker.provider.IntegerStringProvider
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark
import com.singularity.trackmyvehicle.model.entity.SpeedAlertReport
import com.singularity.trackmyvehicle.testhelper.TestHelper
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.lorem.WordProvider
import one.equinox.fritterfactory.providers.primitives.DoubleProvider
import one.equinox.fritterfactory.providers.primitives.IntegerProvider

/**
 * Created by Imran Chowdhury on 8/8/2018.
 */

class SpeedAlertReportFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<SpeedAlertReport> {

        val list = factory.buildList(SpeedAlertReport::class.java, count)

        list.forEach {
            it.date = TestHelper.getDate()
            it.latitude = "2.2"
        }

        return list
    }

    fun getSingleItem(): SpeedAlertReport {
        val model = factory.build(SpeedAlertReport::class.java)
        model.date = TestHelper.getDate()
        return model
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<SpeedAlertReport> {
            val outletMold = MapMold()
            outletMold.put("bstId", WordProvider())
            outletMold.put("date", DateStringProvider())
            outletMold.put("speed", IntegerStringProvider())
            outletMold.put("place", WordProvider(5, 10))
            outletMold.put("longitude", DoubleStringProvider())
            outletMold.put("latitude", DoubleStringProvider())
            return ModelProvider<SpeedAlertReport>(factory, SpeedAlertReport::class.java, outletMold)
        }
    }
}