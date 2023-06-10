package com.singularity.trackmyvehicle;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Imran Chowdhury on 8/12/2018.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DbTestSuite.class,
    RepositoryTestSuite.class,
    ViewModelTestSuite.class
})
public class AllUnitTests {
}
