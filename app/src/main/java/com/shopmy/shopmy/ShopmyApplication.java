package com.shopmy.shopmy;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import net.danlew.android.joda.JodaTimeAndroid;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.android.ContextHolder;


/**
 * Created by Stepan on 11. 10. 2015.
 */
public class ShopmyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);

        SQLiteDatabase db = openOrCreateDatabase("shops", 0, null);
        ContextHolder.setContext(this);
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:sqlite:" + db.getPath(), "", "");
        flyway.migrate();
    }
}
