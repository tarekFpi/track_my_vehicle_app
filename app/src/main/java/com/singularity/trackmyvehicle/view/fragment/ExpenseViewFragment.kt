package com.singularity.trackmyvehicle.view.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.singularity.trackmyvehicle.R

class ExpenseViewFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_expense_view, container, false)

        return rootView
    }

    companion object {

        fun newInstance(param1: String, param2: String): ExpenseViewFragment {
            val fragment = ExpenseViewFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}