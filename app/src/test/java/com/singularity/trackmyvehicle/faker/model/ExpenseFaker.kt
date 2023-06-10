package com.singularity.trackmyvehicle.faker.model

import com.singularity.trackmyvehicle.faker.provider.DateStringProvider
import com.singularity.trackmyvehicle.faker.provider.IntegerStringProvider
import com.singularity.trackmyvehicle.model.entity.Expense
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.lorem.WordProvider

/**
 * Created by Imran Chowdhury on 8/9/2018.
 */
class ExpenseFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<Expense> {

        val list = factory.buildList(Expense::class.java, count)

        list.forEach {
        }

        return list
    }

    fun getSingleItem(): Expense {
        val model = factory.build(Expense::class.java)
        return model
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<Expense> {
            val outletMold = MapMold()
            outletMold.put("id", WordProvider(3))
            outletMold.put("date", DateStringProvider())
            outletMold.put("bstid", WordProvider(5))
            outletMold.put("expenseHeader", WordProvider(7))
            outletMold.put("amount", IntegerStringProvider())
            outletMold.put("description", WordProvider(10))
            return ModelProvider<Expense>(factory, Expense::class.java, outletMold)
        }
    }
}
