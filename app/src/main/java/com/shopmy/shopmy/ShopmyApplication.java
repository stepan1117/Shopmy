package com.shopmy.shopmy;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by Stepan on 11. 10. 2015.
 */
public class ShopmyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}
