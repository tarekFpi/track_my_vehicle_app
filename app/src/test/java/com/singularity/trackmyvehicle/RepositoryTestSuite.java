package com.singularity.trackmyvehicle;

import com.singularity.trackmyvehicle.repository.implementation.FeedbackRepositoryTest;
import com.singularity.trackmyvehicle.repository.implementation.PrefRepositoryTest;
import com.singularity.trackmyvehicle.repository.implementation.ReportsRepositoryTest;
import com.singularity.trackmyvehicle.repository.implementation.VehicleRepositoryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Imran Chowdhury on 8/11/2018.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    PrefRepositoryTest.class,
    FeedbackRepositoryTest.class,
    ReportsRepositoryTest.class,
    VehicleRepositoryTest.class
})
public class RepositoryTestSuite {
}