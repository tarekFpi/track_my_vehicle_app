
package com.singularity.trackmyvehicle.view.customview;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.singularity.trackmyvehicle.R;

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
public class SpeedMarkerView extends MarkerView {
	
	private final IAxisValueFormatter yAxisValueFormatter;
	private       TextView            tvContent;
	private       IAxisValueFormatter xAxisValueFormatter;
	
	
	private String mXAxisLabel = "";
	private String mYAxisLabel = "";
	
	public SpeedMarkerView(Context context,
						   IAxisValueFormatter xAxisValueFormatter,
						   IAxisValueFormatter yAxisValueFormatter,
						   String xAxixLabel,
						   String yAxixLabel
	) {
		super(context, R.layout.layout_marker_view);
		
		this.xAxisValueFormatter = xAxisValueFormatter;
		this.yAxisValueFormatter = yAxisValueFormatter;
		tvContent = findViewById(R.id.tvContent);
		mXAxisLabel = xAxixLabel;
		mYAxisLabel = yAxixLabel;
	}
	
	
	@Override
	public void refreshContent(Entry e, Highlight highlight) {
		
		tvContent.setText(
				String.format("%s: %s, %s: %s", mXAxisLabel, xAxisValueFormatter.getFormattedValue(e.getX(), null), mYAxisLabel, yAxisValueFormatter.getFormattedValue(e.getY(), null))
		);
		
		super.refreshContent(e, highlight);
	}
	
	@Override
	public MPPointF getOffset() {
		return new MPPointF(-(getWidth() / 2), -getHeight());
	}
}
