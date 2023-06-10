package com.singularity.trackmyvehicle.repository.implementation

import androidx.lifecycle.MutableLiveData
import com.singularity.trackmyvehicle.mock.AppMockCall
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v2.PaginatedWrapper
import com.singularity.trackmyvehicle.model.entity.Feedback
import com.singularity.trackmyvehicle.model.entity.FeedbackHeader
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark
import com.singularity.trackmyvehicle.network.AppExecutors
import com.singularity.trackmyvehicle.network.PaginatedFeedbackFetcher
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.repository.interfaces.FeedbackRepository
import com.singularity.trackmyvehicle.testhelper.SameThreadExecutorService
import junit.framework.Assert.*
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException


/**
 * Created by Imran Chowdhury on 8/11/2018.
 */
@RunWith(MockitoJUnitRunner::class)
class FeedbackRepositoryTest : BaseRepositoryTest() {

    private lateinit var mFeedbackRepo: FeedbackRepository

    @Before
    fun setUpmFeedbackRepo() {
        val executor = SameThreadExecutorService()

        val appExecutor = object : AppExecutors {
            override fun ioThread(f: () -> Unit) {
                executor.execute(f)
            }

            override fun networkThread(f: () -> Unit) {
                executor.execute(f)
            }

            override fun mainThread(f: () -> Unit) {
                executor.execute(f)
            }
        }

       /* mFeedbackRepo = mAppModule.providFeedbackRepository(
                mFeedbackDao,
                mWebService,
                mPrefRepository,
                PaginatedFeedbackFetcher(
                        mWebService,
                        mPrefRepository
                ),
                appExecutor
        )*/
    }

    @Test
    fun testNotNullObject() {
        assertNotNull(mWebService)
        assertNotNull(mFeedbackDao)
        assertNotNull(mFeedbackRepo)
        assertNotNull(mFeedbackFaker)
        assertNotNull(mPrefRepository)
        assertNotNull(mExpenseHeaderFaker)
        assertNotNull(mFeedbackRemarkFaker)
        assertNotNull(mFeedbackHeaderFaker)
    }

    @Test
    fun test_fetchExpenseHeaderApiCallReturnsSuccesFullRespone() {
        `when`(mPrefRepository.apiToken()).thenReturn("apitoken")
        `when`(mFeedbackDao.feedbackHeaderAll).thenReturn(mFeedbackHeaderFaker.getList())
        `when`(mWebService.fetchFeedbackHeaders(mPrefRepository.apiToken()))
                .thenReturn(object : Call<GenericApiResponse<List<FeedbackHeader>>> {
                    override fun enqueue(callback: Callback<GenericApiResponse<List<FeedbackHeader>>>?) {
                        val response = GenericApiResponse<List<FeedbackHeader>>()
                        response.code = "200"
                        response.data = mFeedbackHeaderFaker.getList()
                        callback?.onResponse(this, Response.success(response))
                    }

                    override fun isExecuted(): Boolean {
                        return true
                    }

                    override fun clone(): Call<GenericApiResponse<List<FeedbackHeader>>> {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun isCanceled(): Boolean {
                        return false
                    }

                    override fun cancel() {
                        this.cancel()
                    }

                    override fun execute(): Response<GenericApiResponse<List<FeedbackHeader>>> {
                        val response = GenericApiResponse<List<FeedbackHeader>>()
                        response.code = "200"
                        response.data = mFeedbackHeaderFaker.getList()
                        return Response.success(response)
                    }

                    override fun request(): Request {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })
        val data = mFeedbackRepo.fetchExpenseHeader()
        val resource = getBlockingValue(data)
        val expenseHeaderListFromDB = resource.data
        assertEquals(5, expenseHeaderListFromDB?.size)
        assertEquals(Status.SUCCESS, resource.status)
    }

    @Test
    fun test_fetchExpenseHeaderApiCallReturnsFailedRespone() {
        `when`(mPrefRepository.apiToken()).thenReturn("apitoken")
        `when`(mFeedbackDao.feedbackHeaderAll).thenReturn(mFeedbackHeaderFaker.getList())
        `when`(mWebService.fetchFeedbackHeaders(mPrefRepository.apiToken()))
                .thenReturn(object : Call<GenericApiResponse<List<FeedbackHeader>>> {
                    override fun enqueue(callback: Callback<GenericApiResponse<List<FeedbackHeader>>>?) {
                        val response = GenericApiResponse<List<FeedbackHeader>>()
                        response.code = "401"
                        callback?.onFailure(this, null)
                    }

                    override fun isExecuted(): Boolean {
                        return true
                    }

                    override fun clone(): Call<GenericApiResponse<List<FeedbackHeader>>> {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun isCanceled(): Boolean {
                        return false
                    }

                    override fun cancel() {
                        this.cancel()
                    }

                    override fun execute(): Response<GenericApiResponse<List<FeedbackHeader>>> {
                        val response = GenericApiResponse<List<FeedbackHeader>>()
                        response.code = "401"
                        response.data = null
                        return Response.error(401, ResponseBody.create(null, "failed"))
                    }

                    override fun request(): Request {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })


        val data = mFeedbackRepo.fetchExpenseHeader()
        val resource = getBlockingValue(data)
        assertEquals("Something went wrong", resource.message)
        assertEquals(Status.ERROR, resource.status)
    }

    @Test
    fun test_CreateFeedbackAndSendToNetworkSuccess() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.createFeedback("suc", "success", "message", mPrefRepository.apiToken()))
                .thenReturn(object : Call<GenericApiResponse<String>> {
                    override fun enqueue(callback: Callback<GenericApiResponse<String>>?) {
                        val response = GenericApiResponse<String>()
                        response.code = "201"
                        response.data = "Success"
                        callback?.onResponse(this, Response.success(response))
                    }

                    override fun isExecuted(): Boolean {
                        return true
                    }

                    override fun clone(): Call<GenericApiResponse<String>> {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun isCanceled(): Boolean {
                        return false
                    }

                    override fun cancel() {
                        this.cancel()
                    }

                    override fun execute(): Response<GenericApiResponse<String>> {
                        val response = GenericApiResponse<String>()
                        response.code = "201"
                        response.data = "Success"
                        return Response.success(response)
                    }

                    override fun request(): Request {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })

        val response = mFeedbackRepo
                .createFeedback("suc", "success", "message")
        val resource = getBlockingValue(response)
        assertEquals(Status.SUCCESS, resource.status)
        val data = resource.data
        assertEquals("Success", data?.data)
        assertEquals("201", data?.code)
    }

    @Test
    fun test_createFeedbackAndSendToNetworkFailure() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.createFeedback("fail", "failure", "some-other-message", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<String>>() {
                    override fun execute(): Response<GenericApiResponse<String>> {
                        return Response.error(401, ResponseBody.create(null, "failed"))
                    }
                })

        val responseError = mFeedbackRepo
                .createFeedback("fail", "failure", "some-other-message")
        val resourceError = getBlockingValue(responseError)
        assertEquals("Something went wrong", resourceError.message)
        assertEquals(Status.ERROR, resourceError.status)
    }

    @Test
    fun test_fetchFeedbackFromNetworkSuccess() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mFeedbackDao.feedbackAll).thenReturn(mFeedbackFaker.getList())
        `when`(mWebService.fetchFeedback("1", "30", mPrefRepository.apiToken()))
                .thenReturn(object :AppMockCall<PaginatedWrapper<List<Feedback>>>() {
                    override fun execute(): Response<PaginatedWrapper<List<Feedback>>> {
                        val response = PaginatedWrapper<List<Feedback>>()
                        val feedBackList = mFeedbackFaker.getList()
                        response.data = feedBackList
                        response.code = "200"
                        response.userMessage = "Feedback List"
                        return Response.success(response)
                    }
                })

        val response = mFeedbackRepo.fetchFeedback()
        val resource = getBlockingValue(response)

        assertEquals(Status.SUCCESS, resource.status)

        val feedbackList = resource.data

        assertEquals(5, feedbackList?.size)
    }

    @Test
    fun test_fetchFeedbackFromNetworkSuccessFailedOnlyFeedBackListFromDb() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mFeedbackDao.feedbackAll).thenReturn(mFeedbackFaker.getList())
        `when`(mWebService.fetchFeedback("1", "30", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<PaginatedWrapper<List<Feedback>>>() {
                    override fun execute(): Response<PaginatedWrapper<List<Feedback>>> {
                        return Response.error(401, ResponseBody.create(
                                MediaType.parse(""),
                                "laskjdflaskf"
                        ))
                    }
                })
        val response = mFeedbackRepo.fetchFeedback()
        val resource = getBlockingValue(response)
        val data = resource.data
        assertEquals(Status.ERROR, resource.status)
        assertEquals(5, data?.size)

    }

    @Test
    fun test_fetchFeedbackFromNetworkCausesExceptionDataFromDb() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mFeedbackDao.feedbackAll).thenReturn(mFeedbackFaker.getList())
        `when`(mWebService.fetchFeedback("1", "30", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<PaginatedWrapper<List<Feedback>>>() {
                    override fun execute(): Response<PaginatedWrapper<List<Feedback>>> {
                        throw SocketTimeoutException()
                    }
                })
        val response = mFeedbackRepo.fetchFeedback()
        val resource = getBlockingValue(response)
        val data = resource.data
        assertEquals(Status.ERROR, resource.status)
        assertEquals(5, data?.size)

    }

    @Test
    fun test_fetchExpenseHeaderApiCallThrowsTimeOutException() {
        `when`(mPrefRepository.apiToken()).thenReturn("apitoken")
        `when`(mFeedbackDao.feedbackHeaderAll).thenReturn(mFeedbackHeaderFaker.getList())
        `when`(mWebService.fetchFeedbackHeaders(mPrefRepository.apiToken()))
                .thenReturn(object: AppMockCall<GenericApiResponse<List<FeedbackHeader>>>() {
                    override fun execute(): Response<GenericApiResponse<List<FeedbackHeader>>> {
                        throw SocketTimeoutException()
                    }
                })

        val data = mFeedbackRepo.fetchExpenseHeader()
        val resource = getBlockingValue(data)
        val expenseHeaderListFromDB = resource.data
        assertEquals(5, expenseHeaderListFromDB?.size)
        assertEquals(Status.ERROR, resource.status)
    }

    @Test
    fun test_createFeedbackAndThrowsException() {
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.createFeedback("fail", "failure", "some-other-message", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<String>>() {
                    override fun execute(): Response<GenericApiResponse<String>> {
                        throw SocketTimeoutException()
                    }
                })

        val responseError = mFeedbackRepo
                .createFeedback("fail", "failure", "some-other-message")
        val resourceError = getBlockingValue(responseError)
        assertEquals("Something went wrong", resourceError.message)
        assertEquals(Status.ERROR, resourceError.status)
    }

    @Test
    fun test_fetchFeedbackRemarksFromNetworkSuccess(){
        `when`(mFeedbackDao.getFeedbackRemarkAll("123456")).thenReturn(mFeedbackRemarkFaker.getList())
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchFeedbackRemarks("123456", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<List<FeedbackRemark>>>() {
                    override fun execute(): Response<GenericApiResponse<List<FeedbackRemark>>> {
                        val response = GenericApiResponse<List<FeedbackRemark>>()
                        response.data = mFeedbackRemarkFaker.getList()
                        response.code = "200"
                        response.userMessage = "Success"
                        return Response.success(response)
                    }
                })

        val response = mFeedbackRepo.fetchFeedbackRemarks("123456")
        val resource =  getBlockingValue(response)

        assertEquals(Status.SUCCESS, resource.status)
        assertEquals(5, resource.data?.size)
    }

    @Test
    fun test_fetchFeedbackRemarksFromNetworkFailure(){
        `when`(mFeedbackDao.getFeedbackRemarkAll("123456")).thenReturn(mFeedbackRemarkFaker.getList())
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchFeedbackRemarks("123456", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<List<FeedbackRemark>>>() {
                    override fun execute(): Response<GenericApiResponse<List<FeedbackRemark>>> {

                        return Response.error(404, ResponseBody.create(
                                MediaType.parse("ERROR"),
                                "Error Occured"
                        ))
                    }
                })

        val response = mFeedbackRepo.fetchFeedbackRemarks("123456")
        val resource =  getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
    }

    @Test
    fun test_fetchFeedbackRemarksFromNetworkThrowsException(){
        `when`(mFeedbackDao.getFeedbackRemarkAll("123456")).thenReturn(mFeedbackRemarkFaker.getList())
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mWebService.fetchFeedbackRemarks("123456", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<GenericApiResponse<List<FeedbackRemark>>>() {
                    override fun execute(): Response<GenericApiResponse<List<FeedbackRemark>>> {
                        throw SocketTimeoutException()
                    }
                })

        val response = mFeedbackRepo.fetchFeedbackRemarks("123456")
        val resource =  getBlockingValue(response)

        assertEquals(Status.ERROR, resource.status)
    }

    @Test
    fun test_testAllOtherSimpleMethods() {
        val li = MutableLiveData<List<Feedback>>()
        val feedbackList = mFeedbackFaker.getList()
        li.postValue(feedbackList)
        `when`(mPrefRepository.apiToken()).thenReturn("apiToken")
        `when`(mFeedbackDao.feedbackAllAsync).thenReturn(li)
        `when`(mWebService.fetchFeedback("1", "30", mPrefRepository.apiToken()))
                .thenReturn(object : AppMockCall<PaginatedWrapper<List<Feedback>>>(){
                    override fun enqueue(callback: Callback<PaginatedWrapper<List<Feedback>>>?) {
                        val response = PaginatedWrapper<List<Feedback>>()
                        val feedBackList = mFeedbackFaker.getList()
                        response.data = feedBackList
                        response.code = "200"
                        response.userMessage = "Feedback List"
                        callback?.onResponse(this, Response.success(response))
                    }
                })
        mFeedbackRepo.fetchPaginatedFeedback()
        assertTrue(true)

        val liveData = mFeedbackRepo.getFeedbacksAsync()
        val list = getBlockingValue(liveData)
        assertEquals(5, list.size)
    }
}