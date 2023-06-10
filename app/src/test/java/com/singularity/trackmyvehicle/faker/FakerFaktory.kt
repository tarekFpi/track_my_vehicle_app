package com.singularity.trackmyvehicle.faker

import com.singularity.trackmyvehicle.faker.model.*
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
import com.singularity.trackmyvehicle.model.apiResponse.v2.VehicleStatus
import com.singularity.trackmyvehicle.model.dataModel.SpeedViolationModel
import com.singularity.trackmyvehicle.model.entity.*
import one.equinox.fritterfactory.FritterFactory

class FakerFaktory {

    fun create(): FritterFactory {
        val fritterFactory = FritterFactory()

        fritterFactory.addProvider(Feedback::class.java, FeedbackFaker.provider(fritterFactory))
        fritterFactory.addProvider(FeedbackHeader::class.java, FeedbackHeaderFaker.provider(fritterFactory))
        fritterFactory.addProvider(FeedbackRemark::class.java, FeedbackRemarkFaker.provider(fritterFactory))
        fritterFactory.addProvider(DistanceReport::class.java, DistanceReportFaker.provider(fritterFactory))
        fritterFactory.addProvider(SpeedAlertReport::class.java, SpeedAlertReportFaker.provider(fritterFactory))
        fritterFactory.addProvider(ExpenseHeader::class.java, ExpenseHeaderFaker.provider(fritterFactory))
        fritterFactory.addProvider(Expense::class.java, ExpenseFaker.provider(fritterFactory))
        fritterFactory.addProvider(Vehicle::class.java, VehicleFaker.provider(fritterFactory))
        fritterFactory.addProvider(VehicleRoute::class.java, VehicleRouteFaker.provider(fritterFactory))
        fritterFactory.addProvider(Profile::class.java, ProfileFaker.provider(fritterFactory))
        fritterFactory.addProvider(SpeedViolationModel::class.java, SpeedViolationFaker.provider(fritterFactory))
        fritterFactory.addProvider(VehicleStatus::class.java, VehicleStatusFaker.provider(fritterFactory))
        return fritterFactory
    }
}