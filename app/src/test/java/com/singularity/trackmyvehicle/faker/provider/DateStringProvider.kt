package com.singularity.trackmyvehicle.faker.provider

import one.equinox.fritterfactory.util.RandomFactory
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Provider

/**
 * Created by Sadman Sarar on 7/22/18.
 */
class DateStringProvider : Provider<String> {
    private var random = RandomFactory().get()

    override fun get(): String {
        val date = Date(random.nextLong())
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return format.format(date)
    }
}
