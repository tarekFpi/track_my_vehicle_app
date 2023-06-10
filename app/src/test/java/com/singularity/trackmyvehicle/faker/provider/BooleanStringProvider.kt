package com.singularity.trackmyvehicle.faker.provider

import one.equinox.fritterfactory.util.RandomFactory
import javax.inject.Provider

/**
 * Created by Imran Chowdhury on 8/9/2018.
 */

class BooleanStringProvider : Provider<String> {

    private var random = RandomFactory().get()
    override fun get(): String {
        val bool = random.nextBoolean()
        if (bool) {
            return "On"
        }
        return "Off"
    }

}