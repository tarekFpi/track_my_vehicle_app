package com.singularity.trackmyvehicle.model.dataModel;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.singularity.trackmyvehicle.utils.TimestampConverter;

import org.joda.time.DateTime;

/**
 * Created by Sadman Sarar on 3/14/18.
 */

public class SpeedViolationModel {
	
	public String date;
	public int    violations;
	
	public DateTime getDate() {
		try {
			return DateTime.parse(date, TimestampConverter.Companion.getDf());
		} catch (Exception e) {
			FirebaseCrashlytics.getInstance().recordException(e);
			return DateTime.now();
		}
	}
	
}
