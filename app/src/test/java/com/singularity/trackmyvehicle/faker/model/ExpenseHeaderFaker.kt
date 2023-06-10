package com.singularity.trackmyvehicle.faker.model

import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.model.entity.ExpenseHeader
import com.singularity.trackmyvehicle.model.entity.FeedbackHeader
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.lorem.WordProvider

/**
 * Created by Imran Chowdhury on 8/9/2018.
 */
class ExpenseHeaderFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<ExpenseHeader> {

        return factory.buildList(ExpenseHeader::class.java, count)
    }

    fun getSingleItem() : ExpenseHeader {
        return factory.build(ExpenseHeader::class.java)
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<ExpenseHeader> {
            val outletMold = MapMold()
            outletMold.put("name", WordProvider(5, 10))
            return ModelProvider<ExpenseHeader>(factory, ExpenseHeader::class.java, outletMold)
        }
    }
}