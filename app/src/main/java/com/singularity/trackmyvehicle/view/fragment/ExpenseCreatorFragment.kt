package com.singularity.trackmyvehicle.view.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.VehicleTrackApplication
import com.singularity.trackmyvehicle.model.apiResponse.v2.GenericApiResponse
import com.singularity.trackmyvehicle.model.apiResponse.v3.Terminal
import com.singularity.trackmyvehicle.model.entity.ExpenseHeader
import com.singularity.trackmyvehicle.network.Resource
import com.singularity.trackmyvehicle.network.Status
import com.singularity.trackmyvehicle.view.dialog.DialogHelper
import com.singularity.trackmyvehicle.view.viewCallback.ExpenseCreatorFragmentCallback
import com.singularity.trackmyvehicle.viewmodel.AnalyticsViewModel
import com.singularity.trackmyvehicle.viewmodel.ReportsViewModel
import com.singularity.trackmyvehicle.viewmodel.VehiclesViewModel
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_expense_creator.*
import javax.inject.Inject


class ExpenseCreatorFragment : Fragment() {

    @Inject
    lateinit var mReportsViewModel: ReportsViewModel
    @Inject
    lateinit var mAnalytics: AnalyticsViewModel
    @Inject
    lateinit var mVehicleViewModel: VehiclesViewModel

    private var mExpenseHeader: List<ExpenseHeader>? = ArrayList()
    private var mExpenseHeaderString: MutableList<String> = ArrayList()
    private var mVehicles: List<Terminal> = ArrayList()
    private var mSelectedVehicle: Terminal? = null

    private var mSelectedExpenseHeader: String = ""

    private val mExpenseHeaderObserver = Observer<Resource<List<ExpenseHeader>>> { data ->
        if (data == null)
            return@Observer
        mExpenseHeader = data.data

        mExpenseHeader?.forEach { header ->
            mExpenseHeaderString.add(header.name)
        }

        if (mExpenseHeader?.size ?: 0 > 0) {
            txtExpenseHeader.text = mExpenseHeader?.get(0)?.name ?: "Select Expense Header"
            mSelectedExpenseHeader = mExpenseHeader?.get(0)?.name ?: ""
        }


    }
    private var mDialog: MaterialDialog? = null
    private lateinit var mCallback: ExpenseCreatorFragmentCallback

    private val mAddExpenseObserver = Observer<Resource<GenericApiResponse<String>>> { data ->
        if (data == null)
            return@Observer
        when (data.status) {
            Status.LOADING -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getLoadingDailog(this.context!!, "Adding Expense",
                        "Hold on...")?.show()
            }
            Status.SUCCESS -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this.context!!, "Success",
                        "Expense Added Successfully")
                        ?.positiveText("Ok")
                        ?.onPositive(object : MaterialDialog.SingleButtonCallback {
                            override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                                dialog.dismiss()
                                mCallback?.expenseAdded()
                            }
                        })
                        ?.show()
                mAnalytics.expenseAdded()
            }
            Status.ERROR -> {
                mDialog?.dismiss()
                mDialog = DialogHelper.getMessageDialog(this.context!!, "Error",
                        data?.message ?: "Expense could not be added")
                        ?.positiveText("Ok")
                        ?.onPositive(object : MaterialDialog.SingleButtonCallback {
                            override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                                dialog.dismiss()
                            }
                        })
                        ?.show()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VehicleTrackApplication.appComponent?.inject(this)
        mReportsViewModel.fetchExpenseHeader().observe(this, mExpenseHeaderObserver)

        mVehicleViewModel.getVehicles().observe(this, Observer { vehicles ->
            mVehicles = vehicles ?: listOf()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_expense_creator, container, false);
        return rootView
    }

    private var mAddExpenseLiveData: LiveData<Resource<GenericApiResponse<String>>>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtExpenseHeader.setOnClickListener {
            MaterialDialog.Builder(this.context!!)
                    .title("Choose Expense Header")
                    .items(mExpenseHeaderString)
                    .itemsCallbackSingleChoice(-1) { _, _, _, text: CharSequence? ->
                        mSelectedExpenseHeader = text?.toString() ?: ""
                        txtExpenseHeader.setText(mSelectedExpenseHeader)
                        true
                    }
                    .positiveText(R.string.action_choose)
                    .show()
        }

        fabSaveExpense.setOnClickListener {
            if (validate()) {
                mAddExpenseLiveData?.removeObserver(mAddExpenseObserver)
                mAddExpenseLiveData = mReportsViewModel.postCreateExpense(
                        mSelectedVehicle?.bstId ?: "",
                        mCallback.getSelectedDate().toString("yyyy-MM-dd"),
                        mSelectedExpenseHeader,
                        editAmount.text.toString().toFloatOrNull()?.toInt() ?: 0,
                        editDescription.text.toString()
                )
                mAddExpenseLiveData?.observe(this, mAddExpenseObserver)
            }
        }

        btnSelectVehicle.setOnClickListener {
            mDialog = MaterialDialog.Builder(this.context!!)
                    .title("Choose Vehicle")
                    .items(mVehicles)
                    .itemsCallbackSingleChoice(mVehicles.indexOf(mSelectedVehicle)
                    ) { dialog, itemView, which, text ->
                        if (which < mVehicles.size) {
                            mSelectedVehicle = mVehicles[which]
                            updateSelectedVehicleText()
                        }
                        true
                    }
                    .positiveText(R.string.action_choose)
                    .show()
        }
    }

    private fun updateSelectedVehicleText() {
        btnSelectVehicle.text = mSelectedVehicle?.toString() ?: "No vehicle selected"
    }

    private fun validate(): Boolean {
        if (editAmount.text.toString().isEmpty()) {
            Toasty.error(this?.context!!, "Please enter amount").show()
            return false
        }
        if (mSelectedExpenseHeader.isEmpty()) {
            Toasty.error(this?.context!!, "Please select expense type").show()
            return false
        }
        if (mSelectedVehicle == null) {
            Toasty.error(this?.context!!, "Please select a vehicle").show()
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    companion object {
        fun newInstance(): ExpenseCreatorFragment {
            val fragment = ExpenseCreatorFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

    }


    override fun onAttach(context: Context) {
        context?.let { super.onAttach(it) }
        if (context is ExpenseCreatorFragmentCallback) {
            mCallback = context
            return
        }

        Log.w(
                this.javaClass.name,
                "ExpenseCreatorFragmentCallback is not implemented by the calling activity"
        )
    }

    override fun onResume() {
        super.onResume()
        mAnalytics.expenseAddScreen()
    }
}