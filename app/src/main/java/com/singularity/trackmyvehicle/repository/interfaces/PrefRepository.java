package com.singularity.trackmyvehicle.repository.interfaces;

import com.singularity.trackmyvehicle.model.apiResponse.v2.LoginResponse;
import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Imran Chowdhury on 8/9/2018.
 */
public interface PrefRepository {

    void saveApiToken(String token);

    void changeCurrentVehicle(String bstId, String vrn, String terminalId);

    void changeCurrentVehicle(String bstId, String vrn, String terminalId, String location);

    void saveProfile(Profile profile);

    void saveUser(LoginResponse.User user);

    LoginResponse.User getUser();

    void saveOtpToken(String token);

    void saveUnsetFCMToken(String token);

    String apiToken();

    String currentVehicle();

    String currentVehicleVrn();

    String currentLocation();

    String otpToken();

    String unsentFCMToken();

    Profile profile();

    void saveUserSource(String source);

    String getUserSource();

    boolean isUserLoggedIn();

    void saveCookie(String cookie);

    String getCookie();

    String getCurrentVehicleTerminalId();

    void savePasswordHash(String passwordHash);

    String passwordHash();

    void saveUserName(String userName);

    String userName();

    void saveUnreadMessageCount(@Nullable Integer count);

    int getUnreadMessageCount();

    void saveDeviceFCM(String token);

    String getDeviceFcm();
}
