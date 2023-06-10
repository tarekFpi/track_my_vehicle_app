package com.singularity.trackmyvehicle.faker.provider

import one.equinox.fritterfactory.util.RandomFactory
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Provider

/**
 * Created by Imran Chowdhury on 8/8/2018.
 */

class DoubleStringProvider : Provider<String> {
    private var random = RandomFactory().get()

    override fun get(): String {
        return random.nextDouble().toString()
    }
}