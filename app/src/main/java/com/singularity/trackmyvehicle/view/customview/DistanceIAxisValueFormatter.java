package com.singularity.trackmyvehicle.view.customview;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

public class DistanceIAxisValueFormatter implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (value < 1000) {
            return String.format("%.1f M", value);
        } else {
            return String.format("%.1f Km", value / 1000);
        }
    }
}



