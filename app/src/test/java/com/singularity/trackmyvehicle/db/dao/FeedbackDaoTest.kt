package com.singularity.trackmyvehicle.db.dao

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Created by Imran Chowdhury on 8/8/2018.
 */
@RunWith(RobolectricTestRunner::class)
class FeedbackDaoTest : BaseDbTest() {

    private lateinit var mFeedbackDao: FeedbackDao

    @Before
    fun initiateDao() {
        mFeedbackDao = mAppDb.feedbackDao()
    }

    @Test
    fun testNotNullObject() {
        assertNotNull(mAppDb)
        assertNotNull(mFaker)
        assertNotNull(mFeedbackFaker)
        assertNotNull(mFeedbackHeaderFaker)
        assertNotNull(mFeedbackDao)
        assertNotNull(mFeedbackRemarkFaker)
    }

    @Test
    fun test_saveFeedbackHeaderToDbReturnsSameCount() {
        val feedBackHeaderList = mFeedbackHeaderFaker.getList(5)
        mFeedbackDao.saveFeedbackHeader(feedBackHeaderList)
        val feedBackHeaderListFromDb = mFeedbackDao.feedbackHeaderAll
        assertEquals(feedBackHeaderList.size, feedBackHeaderListFromDb.size)
    }

    @Test
    fun test_saveFeedbackHeaderToDbReturnsSameObject() {
        val feedBackHeaderList = mFeedbackHeaderFaker.getSingleItem()
        mFeedbackDao.saveFeedbackHeader(feedBackHeaderList)
        val feedBackHeaderListFromDb = mFeedbackDao.feedbackHeaderAll[0]
        assertEquals(feedBackHeaderList, feedBackHeaderListFromDb)
    }

    @Test
    fun test_saveFeedbackHeaderToDbReturnsSameCountAsync() {
        val feedBackHeaderList = mFeedbackHeaderFaker.getList(5).sortedBy { it.name }.asReversed()
        mFeedbackDao.saveFeedbackHeader(feedBackHeaderList)
        val feedBackHeaderListFromDb = mFeedbackDao.feedbackHeaderAllAsync
        assertEquals(feedBackHeaderList.size, getBlockingValue(feedBackHeaderListFromDb).size)
        assertEquals(feedBackHeaderList, getBlockingValue(feedBackHeaderListFromDb))
    }

    @Test
    fun test_saveFeedbackToDbReturnsSame() {
        val feedBackList = mFeedbackFaker.getList(5)
        mFeedbackDao.saveFeedback(feedBackList)
        val feedBackListFromDb = mFeedbackDao.feedbackAll
        assertEquals(feedBackList.size, feedBackListFromDb.size)
        assertEquals(feedBackList, feedBackListFromDb)
        val liveData = mFeedbackDao.feedbackAllAsync
        feedBackList.sortByDescending { it.sl }
        assertEquals(feedBackList, getBlockingValue(liveData))
    }

    @Test
    fun test_saveSingleFeedbackToDbReturnsSame() {
        val feedBack = mFeedbackFaker.getSingleItem()
        mFeedbackDao.saveFeedback(feedBack)
        val feedBackFromDb = mFeedbackDao.feedbackAll[0]
        assertEquals(feedBack, feedBackFromDb)
    }

    @Test
    fun test_saveFeedbackToDbDeleteReturnsEmptyList() {
        val feedBackList = mFeedbackFaker.getList(10)
        mFeedbackDao.saveFeedback(feedBackList)
        mFeedbackDao.deleteAllFeedbacks()
        val feedBackListFromDb = mFeedbackDao.feedbackAll
        assertEquals(0, feedBackListFromDb.size)
    }

    @Test
    fun test_saveFeedbackRemarkToDbReturnsSame() {
        val feedBackRemark = mFeedbackRemarkFaker.getList(5)
        mFeedbackDao.saveFeedbackRemark(feedBackRemark)
        val feedBackRemarkListFromDb = mFeedbackDao.getFeedbackRemarkAll(feedBackRemark[3].feedbackId)
        assertEquals(1, feedBackRemarkListFromDb.size)
        assertEquals(feedBackRemark[3], feedBackRemarkListFromDb[0])
    }

    @Test
    fun test_saveSingleFeedbackRemarkToDbReturnsSameThenDelete() {
        val feedBackRemark = mFeedbackRemarkFaker.getSingleItem()
        mFeedbackDao.saveFeedbackRemark(feedBackRemark)
        var feedBackRemarkListFromDb = mFeedbackDao.getFeedbackRemarkAll(feedBackRemark.feedbackId)
        assertEquals(1, feedBackRemarkListFromDb.size)
        assertEquals(feedBackRemark, feedBackRemarkListFromDb[0])
        mFeedbackDao.deleteAllFeedbackRemarks()
        feedBackRemarkListFromDb = mFeedbackDao.getFeedbackRemarkAll(feedBackRemark.feedbackId)
        assertEquals(0, feedBackRemarkListFromDb.size)
        mFeedbackDao.saveFeedbackRemark(feedBackRemark)
        val liveData = mFeedbackDao.getFeedbackRemarkAllAsync(feedBackRemark.feedbackId)
        val feedBackAsLiveData = getBlockingValue(liveData)[0]
        assertEquals(feedBackRemark, feedBackAsLiveData)
    }
}