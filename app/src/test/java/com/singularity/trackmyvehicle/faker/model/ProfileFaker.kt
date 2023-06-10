package com.singularity.trackmyvehicle.faker.model

import com.singularity.trackmyvehicle.faker.provider.IntegerStringProvider
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import one.equinox.fritterfactory.FritterFactory
import one.equinox.fritterfactory.mold.MapMold
import one.equinox.fritterfactory.providers.ModelProvider
import one.equinox.fritterfactory.providers.lorem.WordProvider

/**
 * Created by Imran Chowdhury on 8/11/2018.
 */

class ProfileFaker(private val factory: FritterFactory) {

    fun getList(count: Int = 5, data: HashMap<String, Any>? = hashMapOf()): MutableList<Profile> {

        val list = factory.buildList(Profile::class.java, count)

        list.forEach {
        }

        return list
    }

    fun getSingleItem(): Profile {
        val model = factory.build(Profile::class.java)
        return model
    }

    companion object {
        fun provider(factory: FritterFactory): ModelProvider<Profile> {
            val outletMold = MapMold()
            outletMold.put("address", WordProvider(10))
            outletMold.put("mobile", IntegerStringProvider())
            outletMold.put("email", WordProvider(7))
            outletMold.put("name", WordProvider(3))
            outletMold.put("role", WordProvider(3))
            outletMold.put("power", WordProvider(5))
            return ModelProvider<Profile>(factory, Profile::class.java, outletMold)
        }
    }
}