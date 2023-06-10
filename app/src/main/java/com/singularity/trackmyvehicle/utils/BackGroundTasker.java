package com.singularity.trackmyvehicle.utils;

import android.os.AsyncTask;

/**
 * Created by Sadman Sarar on 3/12/18.
 */

public class BackGroundTasker<Result> extends AsyncTask<Boolean, Boolean, Result> {
	
	Helper<Result> mResultHelper = null;
	
	@Override
	protected Result doInBackground(Boolean... booleans) {
		return mResultHelper.onBackground();
	}
	
	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		mResultHelper.onForegound(result);
	}
	
	private void execute() {
		this.execute(false);
	}
	
	public interface Helper<Result> {
		Result onBackground();
		
		void onForegound(Result result);
	}
	
	public BackGroundTasker(Helper<Result> resultHelper) {
		mResultHelper = resultHelper;
		execute();
	}
}
