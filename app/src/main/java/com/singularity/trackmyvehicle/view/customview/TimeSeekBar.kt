package com.singularity.trackmyvehicle.view.customview

import com.xw.repo.BubbleSeekBar

/**
 * Created by Sadman Sarar on 3/10/18.
 */

class TimeSeekBar(private val mBubbleSeekBar: BubbleSeekBar) {

    fun setup() {
        mBubbleSeekBar.isCustomLabel = true

        var labels = ArrayList<String>()

        for (i in mBubbleSeekBar.min.toInt() .. mBubbleSeekBar.max.toInt()) {
            var hour = i / 60
            var min = i - hour * 60
            var e = "AM"
            if (hour > 12) {
                hour = hour - 12
                e = "PM"
            }

            var str = String.format("%02d:%02d %s", hour, min, e)
            labels.add(str)
        }

        mBubbleSeekBar.setCustomLabel(labels)

        mBubbleSeekBar.setCustomSectionTextArray(BubbleSeekBar.CustomSectionTextArray { sectionCount, array ->
            array.clear()
            array.put(0, "12:00 AM")
            array.put(1, "6:00 AM")
            array.put(2, "12:00 PM")
            array.put(3, "6:00 PM")
            array.put(4, "11:59 PM")
            array
        })

    }
}