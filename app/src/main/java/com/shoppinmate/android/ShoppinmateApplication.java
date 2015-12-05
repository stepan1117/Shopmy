package com.shoppinmate.android;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.multidex.MultiDex;

import net.danlew.android.joda.JodaTimeAndroid;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.android.ContextHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by Stepan on 11. 10. 2015.
 */
public class ShoppinmateApplication extends Application {
    private static String connectionString;
    public static ShoppinmateApplication instance;
    private static Map<String, Integer> identifiers = new ConcurrentHashMap<>();
    private static Map<Integer, String> strings = new ConcurrentHashMap<>();

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
//        flyway.clean();
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();
        db.close();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static ShoppinmateApplication getInstance(){
        return instance;
    }

    public static String getConnectionString(){
        return connectionString;
    }

    public int getResourceId(String resourceName){
        if (identifiers.containsKey(resourceName)){
            return identifiers.get(resourceName);
        }

        int id = getResources()
                .getIdentifier(
                        resourceName.toString(), "string", ShoppinmateApplication.getInstance().getPackageName());
        identifiers.put(resourceName, id);
        return id;
    }

    public String getStringFromResource(int resourceId){
        if (strings.containsKey(resourceId)){
            return strings.get(resourceId);
        }

        String str = getResources().getString(resourceId);
        strings.put(resourceId, str);
        return str;
    }

}
