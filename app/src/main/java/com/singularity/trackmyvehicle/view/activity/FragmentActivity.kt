package com.singularity.trackmyvehicle.view.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.view.fragment.*
import com.singularity.trackmyvehicle.view.viewCallback.ExpenseCreatorFragmentCallback
import com.singularity.trackmyvehicle.view.viewCallback.ExpenseListFragmentCallback
import kotlinx.android.synthetic.main.toolbar.*
import org.joda.time.DateTime

class FragmentActivity : BaseActivity(), ExpenseListFragmentCallback, ExpenseCreatorFragmentCallback {

    private val mAccountFragment: AccountFragment by lazy { AccountFragment.newInstance() }
    private val mExpenseListFragment: ExpenseListFragment by lazy { ExpenseListFragment.newInstance() }
    private val mExpenseCreatorFragment: ExpenseCreatorFragment by lazy { ExpenseCreatorFragment.newInstance() }
    private val mDistanceReportFragment: DistanceReportFragment by lazy { DistanceReportFragment.newInstance() }
    private val mHourlyDistanceReportFragment: HourlyDistanceReportFragment by lazy { HourlyDistanceReportFragment.newInstance() }
    private val mDailyEngineReportFragment: DailyEngineReportFragment by lazy { DailyEngineReportFragment.newInstance() }
    private val mLocationReportFragment: LocationReportFragment by lazy { LocationReportFragment.newInstance() }
    private val mNotificationListFragment: NotificationListFragment by lazy { NotificationListFragment.newInstance() }
    private val mSpeedReportFragment: SpeedReportFragment by lazy { SpeedReportFragment.newInstance() }
    private val mMonthlyReportFragment: MonthlyReportFragment by lazy { MonthlyReportFragment.newInstance() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        intent.extras?.getString(SHOULD_SHOW_FRAGMENT)?.let {
            when (it) {
                TAG_ACCOUNT -> {
                    supportActionBar?.title = getString(R.string.label_account_information)
                    commitFragmentTransaction(mAccountFragment, it)
                }
                TAG_EXPENSE_LIST -> {
                    supportActionBar?.title = getString(R.string.label_expenses)
                    commitFragmentTransaction(mExpenseListFragment, it)
                }
                TAG_Expense_Creator -> {
                    supportActionBar?.title = getString(R.string.label_expenses_create)
                    commitFragmentTransaction(mExpenseCreatorFragment, it)
                }
                TAG_DISTANCE_REPORT -> {
                    setSupportActionBar(null)
                    toolbar.visibility = View.GONE
//                    supportActionBar?.title = getString(R.string.label_distance_report)
                    commitFragmentTransaction(mDistanceReportFragment, it)
                }
                TAG_HOURLY_DISTANCE_REPORT -> {
                    setSupportActionBar(null)
                    toolbar.visibility = View.GONE
                    commitFragmentTransaction(mHourlyDistanceReportFragment, it)
                }
                TAG_ENGINE_ON_REPORT -> {
                    setSupportActionBar(null)
                    toolbar.visibility = View.GONE
                    commitFragmentTransaction(mDailyEngineReportFragment, it)
                }
                TAG_LOCATION_REPORT -> {
                    setSupportActionBar(null)
                    toolbar.visibility = View.GONE
                    commitFragmentTransaction(mLocationReportFragment, it)
                }
                TAG_NOTIFICATION -> {
//                    setSupportActionBar(null)
//                    toolbar.visibility = View.GONE
                    supportActionBar?.title = getString(R.string.label_notification)
                    commitFragmentTransaction(mNotificationListFragment, it)
                }
                TAG_MONTHLY_DISTANCE_REPORT -> {
                    supportActionBar?.title = getString(R.string.label_notification)
                    toolbar.visibility = View.GONE
                    commitFragmentTransaction(mMonthlyReportFragment, it)
                }
                TAG_SPEED_REPORT -> {
                    setSupportActionBar(null)
                    toolbar.visibility = View.GONE
//                    supportActionBar?.title = getString(R.string.label_notification)
                    commitFragmentTransaction(mSpeedReportFragment, it)
                }
                else -> {
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            return when (it.itemId) {
                android.R.id.home -> {
                    finish()
                    true
                }
                else -> {
                    false
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun commitFragmentTransaction(fragment: Fragment, tag: String) {

        try {
            supportFragmentManager
                    .beginTransaction()
//                    .setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
                    .replace(R.id.containerFragment, fragment, tag)
                    .addToBackStack(null)
                    .commit()
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
        }
    }

    companion object {
        const val SHOULD_SHOW_FRAGMENT: String = "SHOULD_SHOW_FRAGMENT"
        const val TAG_ACCOUNT: String = "AccountFragment"
        const val TAG_NOTIFICATION: String = "TAG_NOTIFICATION"
        const val TAG_EXPENSE_LIST: String = "ExpenseListFragment"
        const val TAG_DISTANCE_REPORT: String = "TAG_DISTANCE_REPORT"
        const val TAG_HOURLY_DISTANCE_REPORT: String = "TAG_HOURLY_DISTANCE_REPORT"
        const val TAG_MONTHLY_DISTANCE_REPORT: String = "TAG_MONTHLY_DISTANCE_REPORT"
        const val TAG_ENGINE_ON_REPORT: String = "TAG_ENGINE_ON_REPORT"
        const val TAG_LOCATION_REPORT: String = "TAG_LOCATION_REPORT"
        const val TAG_SPEED_REPORT: String = "TAG_SPEED_REPORT"
        const val TAG_Expense_Creator: String = "ExpenseCreatorFragment"
        fun startActivity(context: Context, tag: String) {
            val intent = Intent(context, FragmentActivity::class.java)
            intent.putExtra(SHOULD_SHOW_FRAGMENT, tag)
            context.startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun addExpenseClicked() {
        try {
            startActivity(this, TAG_Expense_Creator)
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
        }
    }

    override fun expenseAdded() {
        finish()
    }

    override fun getSelectedDate(): DateTime {
        return DateTime.now()
    }
}
