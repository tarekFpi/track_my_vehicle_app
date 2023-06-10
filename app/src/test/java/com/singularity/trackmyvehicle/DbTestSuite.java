package com.singularity.trackmyvehicle;

import com.singularity.trackmyvehicle.db.dao.FeedbackDaoTest;
import com.singularity.trackmyvehicle.db.dao.ReportsDaoTest;

import com.singularity.trackmyvehicle.db.dao.VehicleDaoTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Imran Chowdhury on 8/8/2018.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FeedbackDaoTest.class,
        ReportsDaoTest.class,
        VehicleDaoTest.class
})
public class DbTestSuite {
}
