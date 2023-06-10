package com.singularity.trackmyvehicle.view.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.entity.Expense
import com.singularity.trackmyvehicle.model.event.CurrentDateChangeEvent
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.view.adapter.ExpenseListAdapter
import com.singularity.trackmyvehicle.view.viewCallback.ExpenseListFragmentCallback
import com.singularity.trackmyvehicle.view.viewCallback.OnItemClickCallback
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.ReportsViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.empty_expense.*
import kotlinx.android.synthetic.main.fragment_expense_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTime
import javax.inject.Inject

/**
 */
class ExpenseListFragment : Fragment(), OnItemClickCallback<Expense> {

    @Inject
    lateinit var mReportsViewModel: ReportsViewModel

    @Inject
    lateinit var mAnalytics: AnalyticsViewModel

    @Inject
    lateinit var mAdapter: ExpenseListAdapter

    private var mCallback: ExpenseListFragmentCallback? = null


    private val mCurrentVehicleExpenseObserver = Observer<Resource<List<Expense>>> { data ->
        if (data == null)
            return@Observer
        update(data.data)

        when (data.status) {
            Status.SUCCESS -> {
                swipeRefreshLayout.isRefreshing = false
                if (data.data?.size == 0) {
                    Toasty.success(this.context!!, "Expenses loaded").show()
                }
            }
            Status.LOADING -> {
                swipeRefreshLayout.isRefreshing = true
                if (data.data?.size == 0) {
                    Toasty.normal(this.context!!, "Loading Expenses...").show()
                }
            }
            Status.ERROR -> {
                swipeRefreshLayout.isRefreshing = false
                Toasty.error(this.context!!, "Error Loading Expenses").show()

            }
        }
    }

    private fun update(data: List<Expense>?) {
        if (data?.isEmpty() == true) {
            containerEmpty.visibility = View.VISIBLE
            listExpenses.visibility = View.INVISIBLE
        } else {
            containerEmpty.visibility = View.INVISIBLE
            listExpenses.visibility = View.VISIBLE
        }
        mAdapter.setData(data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VehicleTrackApplication.appComponent?.inject(this)
    }

    private var mCurrentVehicleExpenseLiveData: MutableLiveData<Resource<List<Expense>>>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_expense_list, container, false)

        val expenses: RecyclerView = rootView.findViewById(R.id.listExpenses)
        expenses.adapter = mAdapter
        expenses.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        mAdapter.callback = this

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchCurrentVehicleExpenseData(getSelectedDate())
        fabAddExpense.setOnClickListener {
            mCallback?.addExpenseClicked()
        }

        swipeRefreshLayout.setOnRefreshListener {
            fetchCurrentVehicleExpenseData(getSelectedDate())
        }
    }

    private fun fetchCurrentVehicleExpenseData(date: DateTime) {
        mCurrentVehicleExpenseLiveData?.removeObserver(mCurrentVehicleExpenseObserver)
        mCurrentVehicleExpenseLiveData = mReportsViewModel.fetchCurrentVehicleExpense(date.toString("yyyy-MM-dd"))
        mCurrentVehicleExpenseLiveData?.observe(this, mCurrentVehicleExpenseObserver)
    }

    override fun onClick(model: Expense) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ExpenseListFragmentCallback) {
            mCallback = context
            return
        }
        Log.w(
                this.javaClass.name,
                "ExpenseListFragmentCallback is not implemented by the calling activity"
        )
    }

    companion object {
        fun newInstance(): ExpenseListFragment {
            return ExpenseListFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        mAnalytics.expenseViewed(getSelectedDate())
    }

    private fun getSelectedDate(): DateTime {
        return mCallback?.getSelectedDate() ?: DateTime.now()
    }

    override fun onPause() {
        super.onPause()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        mCurrentVehicleExpenseLiveData?.removeObserver(mCurrentVehicleExpenseObserver)

    }

    @Subscribe
    fun onDateChange(event: CurrentDateChangeEvent) {
        fetchCurrentVehicleExpenseData(event.date)
    }
}
