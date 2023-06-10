package com.singularity.trackmyvehicle.faker.model

import com.singularity.trackmyvehicle.model.entity.Feedback
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.lorem.WordProvider
import one.equinox.fritterfactory.providers.primitives.IntegerProvider

/**
 * Created by Imran Chowdhury on 8/8/2018.
 */

class FeedbackFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<Feedback> {

        return factory.buildList(Feedback::class.java, count)
    }

    fun getSingleItem() : Feedback{
        return factory.build(Feedback::class.java)
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<Feedback> {
            val outletMold = MapMold()
            outletMold.put("feedbackId", WordProvider())
            outletMold.put("sl", IntegerProvider(10, 30))
            outletMold.put("remarks", WordProvider())
            outletMold.put("solvedOn", WordProvider(20, 50))
            outletMold.put("raisedOn", WordProvider(10, 20))
            outletMold.put("feedback", WordProvider())
            outletMold.put("vrn", WordProvider(5, 10))
            outletMold.put("bstid", WordProvider(7))
            outletMold.put("feedbackStatus", WordProvider())
            return ModelProvider<Feedback>(factory, Feedback::class.java, outletMold)
        }
    }
}