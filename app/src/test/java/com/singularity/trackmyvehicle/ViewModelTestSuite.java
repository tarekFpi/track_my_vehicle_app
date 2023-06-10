package com.singularity.trackmyvehicle;

import com.singularity.trackmyvehicle.viewmodel.LoginViewModelTest;
import com.singularity.trackmyvehicle.viewmodel.ProfileViewModelTest;
import com.singularity.trackmyvehicle.viewmodel.ReportsViewModelTest;
import com.singularity.trackmyvehicle.viewmodel.SecureModeViewModelTest;
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModelTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Imran Chowdhury on 8/16/2018.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
    VehiclesViewModelTest.class,
    ReportsViewModelTest.class,
    ProfileViewModelTest.class,
    LoginViewModelTest.class,
    SecureModeViewModelTest.class
})
public class ViewModelTestSuite {
}