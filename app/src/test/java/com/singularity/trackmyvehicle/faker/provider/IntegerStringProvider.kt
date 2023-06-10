package com.singularity.trackmyvehicle.faker.provider

import one.equinox.fritterfactory.util.RandomFactory
import kotlin.math.absoluteValue

/**
 * Created by Imran Chowdhury on 8/9/2018.
 */
class IntegerStringProvider : javax.inject.Provider<String> {
    private var random = RandomFactory().get()

    override fun get(): String {
        return random.nextInt(10).absoluteValue.toString()
    }
}