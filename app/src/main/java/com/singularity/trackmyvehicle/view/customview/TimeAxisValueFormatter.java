package com.singularity.trackmyvehicle.view.customview;


import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class TimeAxisValueFormatter implements IAxisValueFormatter {

    private ArrayList<DateTime> mDateTimes;


    public TimeAxisValueFormatter(ArrayList<DateTime> dateTimes) {
        this.mDateTimes = dateTimes;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        int days = (int) value;

        if(mDateTimes.size() == 0){
            return "";
        }

        DateTime dateTime = mDateTimes.get(mDateTimes.size() - 1);

        if (days < mDateTimes.size()) {
            dateTime = mDateTimes.get(days);
        }

        return dateTime.withMinuteOfHour(0).toString("hh a").toUpperCase() + "-" + dateTime.plusHours(1).toString("hh a").toUpperCase();
    }
}



