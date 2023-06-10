package com.singularity.trackmyvehicle.di.v2

import com.singularity.trackmyvehicle.di.v3.DummyV2
import com.singularity.trackmyvehicle.di.v3.DummyV3
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SubModuleV2 {
    @Singleton
    @Provides
    fun dummyv2(): DummyV2 {
        val dummyV3 = DummyV2()
        dummyV3.identity = "V#2"
        return dummyV3
    }
}