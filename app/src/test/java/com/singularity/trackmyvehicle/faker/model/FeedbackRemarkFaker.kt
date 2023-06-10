package com.singularity.trackmyvehicle.faker.model

import com.singularity.trackmyvehicle.faker.provider.DateStringProvider
import com.singularity.trackmyvehicle.model.entity.FeedbackHeader
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.basic.DateProvider
import one.equinox.fritterfactory.providers.lorem.WordProvider
import one.equinox.fritterfactory.providers.primitives.IntegerProvider

/**
 * Created by Imran Chowdhury on 8/8/2018.
 */
class FeedbackRemarkFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<FeedbackRemark> {

        return factory.buildList(FeedbackRemark::class.java, count)
    }

    fun getSingleItem() : FeedbackRemark {
        return factory.build(FeedbackRemark::class.java)
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<FeedbackRemark> {
            val outletMold = MapMold()
            outletMold.put("updateOn", DateStringProvider())
            outletMold.put("updateBy", WordProvider(5))
            outletMold.put("remarks", WordProvider(7))
            outletMold.put("remarksId", WordProvider(5, 10))
            outletMold.put("feedbackId", WordProvider(10, 20))
            return ModelProvider<FeedbackRemark>(factory, FeedbackRemark::class.java, outletMold)
        }
    }
}