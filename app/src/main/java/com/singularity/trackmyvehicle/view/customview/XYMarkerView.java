
package com.singularity.trackmyvehicle.view.customview;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.singularity.trackmyvehicle.R;

import java.text.DecimalFormat;

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
public class XYMarkerView extends MarkerView {
	
	private TextView            tvContent;
	private IAxisValueFormatter xAxisValueFormatter;
	
	private DecimalFormat format;
	private DecimalFormat meterFormat;

	private String mXAxisLabel = "";
	private String mYAxisLabel = "";
	private boolean unitInM;

	public XYMarkerView(Context context,
						IAxisValueFormatter xAxisValueFormatter,
						String xAxixLabel,
						String yAxixLabel,
			boolean unitInM
	) {
		super(context, R.layout.layout_marker_view);
		this.unitInM = unitInM;
		this.xAxisValueFormatter = xAxisValueFormatter;
		tvContent = findViewById(R.id.tvContent);
		format = new DecimalFormat("###.# Km");
		meterFormat = new DecimalFormat("###.# m");
		mXAxisLabel = xAxixLabel;
		mYAxisLabel = yAxixLabel;
	}
	
	
	@Override
	public void refreshContent(Entry e, Highlight highlight) {
		
		tvContent.setText(
				mXAxisLabel
						+ xAxisValueFormatter.getFormattedValue(e.getX(), null)
						+ ", "
						+ mYAxisLabel
						+ (unitInM ? (e.getY()<1000 ? meterFormat.format(e.getY()) : format.format(e.getY()/1000)  ) : format.format(e.getY()) )
		);
		
		super.refreshContent(e, highlight);
	}
	
	@Override
	public MPPointF getOffset() {
		return new MPPointF(-(getWidth() / 2), -getHeight());
	}
}
