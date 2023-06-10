package com.singularity.trackmyvehicle.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.data.UserSource
import com.singularity.trackmyvehicle.model.entity.Feedback
import com.singularity.trackmyvehicle.model.entity.FeedbackRemark
import com.singularity.trackmyvehicle.model.event.CurrentVehicleChangeEvent
import com.singularity.trackmyvehicle.repository.interfaces.PrefRepository
import com.singularity.trackmyvehicle.view.fragment.*
import kotlinx.android.synthetic.main.activity_feedback.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class FeedbackActivity : BaseActivity(), FeedbackListFragment.FeedbackListCallback,
        FeedbackFormFragment.Callback, FeedbackViewFragment.Callback {

    @Inject
    lateinit var mPrefRepository: PrefRepository
    @Inject
    lateinit var userSource: UserSource

    private var mIntentType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        updateHeader()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);

        mIntentType = intent.extras?.getString(TAG_TYPE, TAG_FEEDBACK_LIST) ?: TAG_FEEDBACK_LIST
        if (mIntentType == TAG_FEEDBACK_LIST) {
            transaction.addToBackStack(TAG_FEEDBACK_LIST)
            transaction.replace(R.id.fragment, FeedbackListFragment.newInstance(),
                    TAG_FEEDBACK_LIST)
        } else {
            transaction.addToBackStack(TAG_FEEDBACK_ADD)
            transaction.replace(R.id.fragment, FeedbackFormFragment.newInstance(), TAG_FEEDBACK_ADD)
        }
        transaction.commit()

        vehicleSelectionToolbarView.disableVehicleSelectWithTitle("Support Ticket")
        vehicleSelectionToolbarView.setImgBackClickListener(
                View.OnClickListener { this.onBackPressed() })

        supportFragmentManager.addOnBackStackChangedListener {
            val fm = supportFragmentManager.fragments.lastOrNull()
            if (fm is SupportFormFragment) {
                if (fm.tag == TAG_FEEDBACK_ADD) {
                    txtBanner.text = "Support Ticket"
                } else {
                    txtBanner.text = "Replies"
                }
            } else if (fm is FeedbackViewFragment) {
                txtBanner.text = "Replies"
            } else {
                txtBanner.text = "Support Ticket"
            }
        }

    }

    private fun showVehicleSelectFragment() {
        val dialogFrag = BottomNavFragment.newInstance();
        dialogFrag.show(supportFragmentManager, dialogFrag.tag)
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

    }

    override fun onPause() {
        super.onPause()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

    }

    private fun updateHeader() {
        vehicleSelectionToolbarView.setCurrentVehicleId(mPrefRepository.currentVehicle())
    }

    @Subscribe
    fun onVehicleChange(event: CurrentVehicleChangeEvent) {
        updateHeader()
    }

    companion object {
        val TAG_FEEDBACK_LIST: String = "TAG_FEEDBACK_LIST"
        val TAG_FEEDBACK_ADD: String = "TAG_FEEDBACK_ADD"
        val TAG_FEEDBACK_REPLY: String = "TAG_FEEDBACK_REPLY"
        val TAG_FEEDBACK_VIEW: String = "TAG_FEEDBACK_VIEW"
        val TAG_TYPE: String = "TAG_TYPE"
        fun intent(context: Context, type: String) {
            val intent = Intent(context, FeedbackActivity::class.java)
            intent.putExtra(TAG_TYPE, type)
            context.startActivity(intent)
        }

    }

    override fun feedbackItemClicked(feedback: Feedback) {
        txtBanner.text = "Replies"
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
        transaction.addToBackStack(TAG_FEEDBACK_VIEW)
        val newInstance = FeedbackViewFragment.newInstance(feedback.feedbackId)
        newInstance.callback = this
        transaction.replace(R.id.fragment, newInstance,
                TAG_FEEDBACK_VIEW)
        transaction.commit()

    }

    override fun feedbackAddClicked() {
        txtBanner.text = "Support Ticket"
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
        transaction.addToBackStack(TAG_FEEDBACK_ADD)
        transaction.replace(R.id.fragment,
                if (userSource.identifier == UserSource.VERSION_2.identifier)
                    FeedbackFormFragment.newInstance()
                else SupportFormFragment.newInstance(), TAG_FEEDBACK_ADD
        )
        transaction.commit()
    }

    override fun onReplyClicked(requestId: String) {
        if (userSource.identifier == UserSource.VERSION_2.identifier) {
            return
        }
        txtBanner.text = "Replies"
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
        transaction.addToBackStack(TAG_FEEDBACK_REPLY)
        transaction.replace(R.id.fragment, SupportFormFragment.newInstance(requestId),
                TAG_FEEDBACK_REPLY)
        transaction.commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            this.finish()
            return
        }
        super.onBackPressed()
    }

    override fun feedbackAdded() {
        when (mIntentType) {
            TAG_FEEDBACK_ADD -> finish()
            TAG_FEEDBACK_LIST -> supportFragmentManager.popBackStack()
        }
    }
}

