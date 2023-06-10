package com.singularity.trackmyvehicle.view.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.singularity.trackmyvehicle.BuildConfig;
import com.singularity.trackmyvehicle.R;

/**
 * Created by Sadman Sarar on 3/21/18.
 */
public class BaseActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (BuildConfig.APPLICATION_ID == "com.bondstein.trackmyvehicle.trackmyvehicle") {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorAppBackground));
        }
    }
}
