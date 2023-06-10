package com.singularity.trackmyvehicle.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.singularity.trackmyvehicle.view.fragment.DistanceReportFragment
import com.singularity.trackmyvehicle.view.fragment.SpeedReportFragment
import com.singularity.trackmyvehicle.view.fragment.SpeedViolationReportFragment

/**
 * Created by Sadman Sarar on 4/24/18.
 */
class ReportPagerAdapter(val fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private var distanceReport: DistanceReportFragment? = null

    private var speedReport: SpeedReportFragment? = null

    private var speedViolationReportFragment: SpeedViolationReportFragment? = null

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0    -> {
                if (distanceReport == null) {
                    distanceReport = DistanceReportFragment.newInstance()
                }
                distanceReport ?: DistanceReportFragment.newInstance()
            }
            1    -> {
                if (speedReport == null) {
                    speedReport = SpeedReportFragment.newInstance()
                }
                speedReport ?: SpeedReportFragment.newInstance()
            }
            2    -> {
                if (speedViolationReportFragment == null) {
                    speedViolationReportFragment = SpeedViolationReportFragment.newInstance()
                }
                speedViolationReportFragment ?: SpeedViolationReportFragment.newInstance()
            }
            else -> SpeedReportFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0    -> "Distance Report"
            1    -> "Speed Report"
            2    -> "Speed Violations"
            else -> ""
        }
    }
}