package com.singularity.trackmyvehicle.di.v3

import com.singularity.trackmyvehicle.repository.implementation.v3.VehicleRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
class SubModuleV3 {
    @Singleton
    @Provides
    fun dummyV3(): DummyV3 {
        val dummyV3 = DummyV3()
        dummyV3.identity = "V#3"
        return dummyV3
    }

}


interface Dummy {
    var identity: String
}

class DummyV2 @Inject constructor() : Dummy {
    override var identity: String = "v2"
}

class DummyV3 @Inject constructor() : Dummy {
    override var identity: String = "v3"
}