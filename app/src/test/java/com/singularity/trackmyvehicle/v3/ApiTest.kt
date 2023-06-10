//package com.singularity.trackmyvehicle.v3
//
//import com.google.gson.Gson
//import com.singularity.trackmyvehicle.data.UserSource
//import com.singularity.trackmyvehicle.fcm.FCMRepository
//import com.singularity.trackmyvehicle.model.apiResponse.v2.LoginResponse
//import com.singularity.trackmyvehicle.model.apiResponse.v2.Profile
//import com.singularity.trackmyvehicle.network.AppExecutors
//import com.singularity.trackmyvehicle.network.Status
//import com.singularity.trackmyvehicle.network.interceptor.CookieInterceptor
//import com.singularity.trackmyvehicle.repository.implementation.BaseRepositoryTest
//import com.singularity.trackmyvehicle.repository.implementation.v3.ILoginRepository
//import com.singularity.trackmyvehicle.repository.interfaces.LoginRepository
//import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
//import com.singularity.trackmyvehicle.retrofit.webService.v3.ENDPOINTS
//import com.singularity.trackmyvehicle.retrofit.webService.v3.WebService
//import com.singularity.trackmyvehicle.testhelper.SameThreadExecutorService
//import okhttp3.*
//import okhttp3.logging.HttpLoggingInterceptor
//import org.joda.time.DateTime
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.util.*
//import java.util.concurrent.TimeUnit
//
//class ApiTest : BaseRepositoryTest() {
//
//    companion object {
//        const val BASE_URL = "https://rvt.bondstein.com"
////        const val USERNAME = "sadman@singularitybd.com"
////        const val PASSWORD = "test123"
//        const val USERNAME = "CNC"
//        const val PASSWORD = "123456"
//    }
//
//    lateinit var api: WebService0
//    lateinit var mILoginrepository: LoginRepository
//
//    var apiCookie: String = ""
//    lateinit var cookieInterceptor: CookieInterceptor
//
//
//    @Before
//    fun setUpoginRepo() {
//
//        val prefRepository = object : PrefRepository {
//            override fun changeCurrentVehicle(bstId: String?, vrn: String?, terminalId: String?) {
//
//            }
//
//            override fun getCurrentVehicleTerminalId(): String {
//                return ""
//            }
//
//            override fun savePasswordHash(passwordHash: String?) {
//
//            }
//
//            override fun passwordHash(): String {
//                return ""
//            }
//
//            override fun saveUserName(userName: String?) {
//
//            }
//
//            override fun userName(): String {
//                return ""
//            }
//
//            override fun saveApiToken(token: String?) {
//
//            }
//
//
//            override fun saveProfile(profile: Profile?) {
//
//            }
//
//            override fun saveUser(user: LoginResponse.User?) {
//
//            }
//
//            override fun getUser(): LoginResponse.User {
//                return LoginResponse.User()
//
//            }
//
//            override fun saveOtpToken(token: String?) {
//
//            }
//
//            override fun saveUnsetFCMToken(token: String?) {
//
//            }
//
//            override fun apiToken(): String {
//                return ""
//            }
//
//            override fun currentVehicle(): String {
//                return ""
//            }
//
//            override fun currentVehicleVrn(): String {
//                return ""
//            }
//
//            override fun otpToken(): String {
//                return ""
//            }
//
//            override fun unsentFCMToken(): String {
//                return ""
//            }
//
//            override fun profile(): Profile {
//                return Profile()
//            }
//
//            override fun saveUserSource(source: String?) {
//
//            }
//
//            override fun getUserSource(): String {
//                return UserSource.VERSION_3.identifier
//            }
//
//            override fun isUserLoggedIn(): Boolean {
//                return true
//            }
//
//            override fun saveCookie(cookie: String?) {
//
//            }
//
//            override fun getCookie(): String {
//                return apiCookie
//            }
//
//            override fun saveUnreadMessageCount(count: Int?) {
//
//            }
//
//            override fun getUnreadMessageCount(): Int {
//                return 0
//            }
//        }
//        cookieInterceptor = CookieInterceptor(prefRepository, Gson())
//
//        api = createService()
//        val executor = SameThreadExecutorService()
//        val appExecutor = object : AppExecutors {
//            override fun ioThread(f: () -> Unit) {
//                executor.execute(f)
//            }
//
//            override fun networkThread(f: () -> Unit) {
//                executor.execute(f)
//            }
//
//            override fun mainThread(f: () -> Unit) {
//                executor.execute(f)
//            }
//        }
//        mILoginrepository = ILoginRepository(
//                api,
//                mNetChecker,
//                mPrefRepository,
//                FCMRepository(
//                        mAppModule.provideGithubService(
//                                mAppModule.provideOkHttpClientV2(HttpLoggingInterceptor())
//                        ),
//                        api,
//                        prefRepository
//                ),
//                appExecutor
//        )
//    }
//
//    private fun performSignIn() {
//
//        val response = api.postLogin(
//                ENDPOINTS.LOGIN_SCRIPT,
//                USERNAME,
//                USERNAME,
//                PASSWORD
//        ).execute()
//
//        apiCookie = response.headers().get("Set-Cookie")?.split(";")?.firstOrNull() ?: ""
//
//        response.assertSuccessful()
//
//    }
//
//    @Test
//    fun test_signInSuccess() {
//
//        val response = mILoginrepository.login(USERNAME, PASSWORD)
//        val resource = response
//        Assert.assertEquals(Status.SUCCESS, resource.status)
//        print(resource)
//
//    }
//
////
//
//    private fun createService(): WebService {
//        val clientBuilder = OkHttpClient.Builder()
//
//        clientBuilder.readTimeout(60, TimeUnit.SECONDS)
//                .connectTimeout(60, TimeUnit.SECONDS)
//
//        val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
//            println(it)
//        })
//
//        interceptor.level = HttpLoggingInterceptor.Level.BODY
//        clientBuilder.addInterceptor(interceptor)
//        clientBuilder.addInterceptor(cookieInterceptor)
//
//        val client = clientBuilder.build()
//
//        return Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
//                .build()
//                .create(WebService::class.java)
//    }
//
//    @Test
//    fun terminaAggeregatedDataFetchTest() {
//        performSignIn()
//        val response = api.getTerminalAggregatedData(
//                ENDPOINTS.TERMINAL_DATA_MINUTELY_AGGREGATE,
//                DateTime.now().withDate(2020, 1, 3).withTimeAtStartOfDay().toString(
//                        "yyyy-MM-dd HH:mm:ss"),
//                DateTime.now().withDate(2020, 1, 3).plusDays(1).withTimeAtStartOfDay().minusSeconds(
//                        1).toString("yyyy-MM-dd HH:mm:ss")
//        ).execute()
//
//        response.assertSuccessful()
//        println("Size; ${response.body()?.response?.data?.size ?: 0}")
//        response.body()?.response?.data?.forEach {
//            println(it)
//        }
//
//    }
//
//
//    @Test
//    fun resetPasswordInitiate(){
//        val response = api.requestOtpPassword(ENDPOINTS.REQUEST_OTP,
//                "sadman@singularitybd.com2",
//                "sadman@singularitybd.com2").execute()
//
//        response.printErrorIfAny()
//        response.assertSuccessful()
//    }
//
//    @Test
//    fun resetPasswordSet(){
//        val response = api.resetPassword(ENDPOINTS.RESET_PASSWORD,
//                "5374",
//                "test123",
//                "sadman@singularitybd.com",
//                "sadman@singularitybd.com",
//                "2A3CADD1CD294290AE10F1ECBD0C66A6"
//                ).execute()
//
//        response.printErrorIfAny()
//        response.assertSuccessful()
//    }
//
//    @Test
//    fun getSupportRequestList(){
//
//        performSignIn()
//
//        val response = api.getSupportRequestList(
//                ENDPOINTS.SUPPORT_REQUEST_RESPONSE_LIST,
//                DateTime.now().minusMonths(2).toString("yyyy-MM-dd HH:mm:ss")
//        ).execute()
//
//        response.printErrorIfAny()
//        response.assertSuccessful()
//    }
//    @Test
//    fun getSupportRequestCategoryList(){
//
//        performSignIn()
//
//        val response = api.getSupportRequestCategoryList(
//                ENDPOINTS.SUPPORT_REQUEST_CATEGORY_LIST
//        ).execute()
//
//        response.printErrorIfAny()
//        response.assertSuccessful()
//    }
//    @Test
//    fun getMessagesList(){
//
//        performSignIn()
//
//        val response = api.getMessages(ENDPOINTS.MESSAGE_LIST,
//                DateTime.now().minusDays(2).toString("yyyy-MM-dd HH:mm:ss")).execute()
//
//        response.printErrorIfAny()
//        response.assertSuccessful()
//    }
//
//    @Test
//    fun sendFcmToken(){
//
//        performSignIn()
//
//        val response = api.sendFCMNotification(ENDPOINTS.SEND_FCM_TOKEN,
//                UUID.randomUUID().toString()
//        ).execute()
//
//        response.printErrorIfAny()
//        response.assertSuccessful()
//    }
//
//    @Test
//    fun markMessageAsRead(){
//        performSignIn()
//
//        val response = api.markMessageAsRead("245536","0").execute()
//
//        response.printErrorIfAny()
//        response.assertSuccessful()
//    }
//}
//
//fun <T> retrofit2.Response<T>.printErrorIfAny() {
//    if (!this.isSuccessful) {
//        println(this.code())
//        println(this.errorBody()?.string())
//    }
//}
//
//fun <T> retrofit2.Response<T>.assertSuccessful() {
//    Assert.assertTrue(this.isSuccessful)
//
//}
//
//fun <T> retrofit2.Response<T>.assertError() {
//    Assert.assertFalse(this.isSuccessful)
//
//}
