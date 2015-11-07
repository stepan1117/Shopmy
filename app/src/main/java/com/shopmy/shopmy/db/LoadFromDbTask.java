package com.shopmy.shopmy.db;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.shopmy.shopmy.ShopmyApplication;
import com.shopmy.shopmy.model.ShopInfo;
import com.shopmy.shopmy.model.TimeSpan;

import org.joda.time.LocalTime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stepan on 18. 10. 2015.
 */
public class LoadFromDbTask extends AsyncTask<LatLngBounds, Void, List<ShopInfo>> {

    @Override
    protected List<ShopInfo> doInBackground(LatLngBounds... params) {

        LatLngBounds bounds = null;
        if (params.length > 0 && params[0] != null){
            bounds = params[0];
        }
        List<ShopInfo> shops = new ArrayList<>();

        Connection con = null;
        try {
            con = DriverManager.getConnection(ShopmyApplication.getConnectionString(), "", "");

            String query = "SELECT ID, NAME, LATITUDE, LONGITUDE, ADDRESS, URL, ACTIVE FROM SHOP ";
            if (bounds != null) {
                query += " WHERE LATITUDE BETWEEN ? AND ?" +
                        " AND LONGITUDE BETWEEN ? AND ?";
            }
            PreparedStatement pst = con.prepareStatement(query);

            if (bounds != null){
                pst.setDouble(1,bounds.southwest.latitude);
                pst.setDouble(2,bounds.northeast.latitude);
                pst.setDouble(3,bounds.southwest.longitude);
                pst.setDouble(4,bounds.northeast.longitude);
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()){
                ShopInfo si = new ShopInfo();
                si.setId(rs.getLong("ID"));
                si.setName(rs.getString("NAME"));
                si.setPosition(new LatLng(rs.getDouble("LATITUDE"), rs.getDouble("LONGITUDE")));
                si.setActive(rs.getBoolean("ACTIVE"));
                si.setAddress(rs.getString("ADDRESS"));
                si.setUrl(rs.getString("URL"));
                shops.add(si);
            }
            rs.close();
            pst.close();

            pst = con.prepareStatement("SELECT DAY, MINUTES_FROM, MINUTES_TO FROM OPENING_HOURS WHERE SHOP_ID = ?");
            for (ShopInfo si : shops){
                pst.setLong(1, si.getId());
                ResultSet hoursSet = pst.executeQuery();

                while (hoursSet.next()){
                    String day = hoursSet.getString("DAY");
                    LocalTime start = LocalTime.fromMillisOfDay(hoursSet.getInt("MINUTES_FROM") * 60 * 1000);
                    LocalTime end = LocalTime.fromMillisOfDay(hoursSet.getInt("MINUTES_TO") * 60 * 1000);
                    if (!si.getOpeningHours().containsKey(day)){
                        si.getOpeningHours().put(day, new ArrayList<TimeSpan>());
                    }
                    si.getOpeningHours().get(day).add(new TimeSpan(start, end));
                }
                hoursSet.close();
            }
            pst.close();

            return shops;
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Unable to retrieve list",e);
            return null;
        } finally {
            if (con != null){
                try {
                    con.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }




}
