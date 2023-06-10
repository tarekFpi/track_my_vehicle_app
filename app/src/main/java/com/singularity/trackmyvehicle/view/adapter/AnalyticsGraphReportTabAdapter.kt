package com.singularity.trackmyvehicle.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.singularity.trackmyvehicle.view.activity.GraphFragment
import com.singularity.trackmyvehicle.view.activity.ReportFragment

class AnalyticsGraphReportTabAdapter (fm: FragmentManager?, var mNumOfTabs: Int) :
        FragmentStatePagerAdapter(fm!!) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                ReportFragment()
            }
            1 -> {
                GraphFragment()
            }
            else -> Fragment()
        }
    }

    override fun getCount(): Int {
        return mNumOfTabs
    }
}