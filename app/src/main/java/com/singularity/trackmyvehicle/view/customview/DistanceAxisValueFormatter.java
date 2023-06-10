package com.singularity.trackmyvehicle.view.customview;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

public class DistanceAxisValueFormatter implements IValueFormatter {

    private final ArrayList<Float> mDistanceInMeteres;

    public DistanceAxisValueFormatter(ArrayList<Float> data) {
        this.mDistanceInMeteres = data;
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex,
            ViewPortHandler viewPortHandler) {
        if (value < 1000) {
            return String.format("%.1f M", value);
        } else {
            return String.format("%.1f Km", value / 1000);
        }
    }

}



