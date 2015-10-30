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
    private static String connectionString;
    public static ShopmyApplication instance;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        JodaTimeAndroid.init(this);

        SQLiteDatabase db = openOrCreateDatabase("shops", 0, null);
        ContextHolder.setContext(this);
        Flyway flyway = new Flyway();
        connectionString = "jdbc:sqlite:" + db.getPath();
        flyway.setDataSource(connectionString, "", "");
        // flyway.clean();
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();
        db.close();
    }


    public static ShopmyApplication getInstance(){
        return instance;
    }

    public static String getConnectionString(){
        return connectionString;
    }

}
