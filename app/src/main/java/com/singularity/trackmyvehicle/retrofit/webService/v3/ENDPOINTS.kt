package com.singularity.trackmyvehicle.retrofit.webService.v3

object ENDPOINTS {

    const val LOGOUT = "API/V1/User/SignOut"
    const val TERMINAL_LIST = "API/V1/Terminal/List"
    const val LOGIN_SCRIPT = "API/V1/User/SignIn"
    const val PROFILE_SCRIPT = "API/V1/User/Profile"
    const val TERMINAL_DATA_MINUTELY_AGGREGATE = "API/V1/TerminalDataMinutely/Aggregate"
    const val TERMINAL_DATA_MINUTELY_SCRIPT = "API/V1/TerminalDataMinutely/List"
    const val PASSWORD_CHANGE = "API/V1/User/Password_Change"
    const val REQUEST_OTP = "API/V1/User/Password_Reset_Initiate"
    const val RESET_PASSWORD = "API/V1/User/Password_Reset_Set"
    const val SUPPORT_REQUEST_RESPONSE_LIST = "API/V1/SupportRequestResponse/List"
    const val SUPPORT_REQUEST_CATEGORY_LIST = "API/V1/SupportRequestCategory/List"
    const val SEND_FCM_TOKEN = "API/V1/User/FCM_Identifier_Register"
    const val SUPPORT_REQUEST_CREATE = "API/V1/SupportRequestResponse/Input"
    const val MESSAGE_LIST = "API/V1/Message/List"
    const val MESSAGE_READ = "API/V1/Message/Read"
    const val FETCH_ROUTE_VEHICLE = "API/V2/TerminalData/List"
    const val PROFILE_INFO_SCRIPT = "API/System/V2/Gateway"
    const val PROFILE_INFO_SUBJECT = "User"
    const val PROFILE_INFO_ACTION = "Profile"
    const val TRIP_REPORT = "API/V2/Report/Trip"




}