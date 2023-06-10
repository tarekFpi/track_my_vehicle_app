package com.singularity.trackmyvehicle.view.customview;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * Created by philipp on 02/06/16.
 */
public class DayAxisValueFormatter implements IAxisValueFormatter {
	
	private ArrayList<DateTime> mDateTimes;
	
	
	public DayAxisValueFormatter(ArrayList<DateTime> dateTimes) {
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
		
		return dateTime.toString("dd-MMM");
	}
}
