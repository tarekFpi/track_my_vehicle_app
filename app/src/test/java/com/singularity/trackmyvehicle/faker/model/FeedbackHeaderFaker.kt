package com.singularity.trackmyvehicle.faker.model

import com.singularity.trackmyvehicle.model.entity.FeedbackHeader
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.lorem.WordProvider

/**
 * Created by Imran Chowdhury on 8/8/2018.
 */
class FeedbackHeaderFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<FeedbackHeader> {

        return factory.buildList(FeedbackHeader::class.java, count)
    }

    fun getSingleItem() : FeedbackHeader {
        return factory.build(FeedbackHeader::class.java)
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<FeedbackHeader> {
            val outletMold = MapMold()
            outletMold.put("name", WordProvider(5, 10))
            return ModelProvider<FeedbackHeader>(factory, FeedbackHeader::class.java, outletMold)
        }
    }
}