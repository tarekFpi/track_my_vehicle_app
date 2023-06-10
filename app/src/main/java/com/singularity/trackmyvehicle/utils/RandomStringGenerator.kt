package com.singularity.trackmyvehicle.utils

import java.util.*

/**
 * Created by Imran Chowdhury on 8/27/2018.
 */

interface RandomStringGenerator{
    fun getString(): String
}

class RandomStringGeneratorImpl: RandomStringGenerator{
    override fun getString(): String {
        return UUID.randomUUID().toString()
    }

}