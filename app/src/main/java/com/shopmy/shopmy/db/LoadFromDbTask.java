package com.shopmy.shopmy.db;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.shopmy.shopmy.ShopmyApplication;
import com.shopmy.shopmy.model.ShopInfo;
import com.shopmy.shopmy.model.TimeSpan;

import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalTime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stepan on 18. 10. 2015.
 */
public class LoadFromDbTask extends AsyncTask<LatLngBounds, Void, List<ShopInfo>> {

    @Override
    protected List<ShopInfo> doInBackground(LatLngBounds... params) {

        LatLngBounds bounds = null;
        if (params.length > 0){
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

            for (ShopInfo si : shops){
                pst = con.prepareStatement("SELECT DAY, MINUTES_FROM, MINUTES_TO FROM OPENING_HOURS WHERE SHOP_ID = ?");
                pst.setLong(1, si.getId());
                ResultSet hoursSet = pst.executeQuery();

                while (hoursSet.next()){
                    String day = hoursSet.getString("DAY");
                    LocalTime start = LocalTime.fromMillisOfDay(hoursSet.getInt("MINUTES_FROM") * 60 * 1000);
                    LocalTime end = LocalTime.fromMillisOfDay(hoursSet.getInt("MINUTES_TO") * 60 * 1000);
                    if (si.getOpeningHours().containsKey(day)){
                        si.getOpeningHours().put(day, new ArrayList<TimeSpan>());
                    }
                    si.getOpeningHours().get(day).add(new TimeSpan(start, end));
                }
            }
            return shops;
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Unable to retrieve list",e);
            if (con != null){
                try {
                    con.rollback();
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        }

    }

    private void updateShopInfo(ShopInfo shopInfo, Connection con) throws SQLException{
        PreparedStatement pst = con.prepareStatement(
                "UPDATE SHOP SET NAME = ?, LATITUDE = ?, LONGITUDE = ?, ADDRESS = ?, URL = ?, ACTIVE = ?" +
                        " WHERE ID = ?");
        pst.setString(1, shopInfo.getName());
        pst.setDouble(2, shopInfo.getPosition().latitude);
        pst.setDouble(3, shopInfo.getPosition().longitude);
        pst.setString(4, shopInfo.getAddress());
        pst.setString(5, shopInfo.getUrl());
        pst.setBoolean(6, shopInfo.isActive());
        pst.setLong(7, shopInfo.getId());
        pst.executeUpdate();
        pst.close();

        pst = con.prepareStatement("DELETE FROM OPENING_HOURS WHERE SHOP_ID = ?");
        pst.setLong(1, shopInfo.getId());
        pst.executeUpdate();
        pst.close();

        pst = con.prepareStatement(
                "INSERT INTO OPENING_HOURS (SHOP_ID, DAY, MINUTES_FROM, MINUTES_TO) " +
                        " VALUES (?,?,?,?)");

        int i = 0;
        for (List<TimeSpan> spans : shopInfo.getOpeningHours().values()) {
            if (spans != null){
                for (TimeSpan span : spans) {
                    pst.setLong(1, shopInfo.getId());
                    pst.setString(2, ShopInfo.DAYS.values()[i].toString());
                    pst.setInt(3, span.getStart().get(DateTimeFieldType.minuteOfDay()));
                    pst.setInt(4, span.getEnd().get(DateTimeFieldType.minuteOfDay()));
                    pst.executeUpdate();
                }
            }
            i++;
        }
        pst.close();
    }

    private Long createShopInfo(ShopInfo shopInfo, Connection con) throws SQLException{
        PreparedStatement pst = con.prepareStatement(
                "INSERT INTO SHOP (NAME, LATITUDE, LONGITUDE, ADDRESS, URL, ACTIVE)" +
                        " VALUES (?,?,?,?,?,?)");
        pst.setString(1, shopInfo.getName());
        pst.setDouble(2, shopInfo.getPosition().latitude);
        pst.setDouble(3, shopInfo.getPosition().longitude);
        pst.setString(4, shopInfo.getAddress());
        pst.setString(5, shopInfo.getUrl());
        pst.setBoolean(6, shopInfo.isActive());
        pst.executeUpdate();
        pst.close();

        Statement statement = con.createStatement();
        ResultSet generatedKeys = statement.executeQuery("SELECT last_insert_rowid()");
        Long shopId = -1l;

        if (generatedKeys.next()) {
            shopId = generatedKeys.getLong(1);
        } else {
            throw new SQLException("Unable to retrieve shopId");
        }
        pst.close();

        pst = con.prepareStatement(
                "INSERT INTO OPENING_HOURS (SHOP_ID, DAY, MINUTES_FROM, MINUTES_TO) " +
                        " VALUES (?,?,?,?)");

        int i = 0;
        for (List<TimeSpan> spans : shopInfo.getOpeningHours().values()) {
            if (spans != null){
                for (TimeSpan span : spans) {
                    pst.setLong(1, shopId);
                    pst.setString(2, ShopInfo.DAYS.values()[i].toString());
                    pst.setInt(3, span.getStart().get(DateTimeFieldType.minuteOfDay()));
                    pst.setInt(4, span.getEnd().get(DateTimeFieldType.minuteOfDay()));
                    pst.executeUpdate();
                }
            }
            i++;
        }
        pst.close();
        
        return shopId;
    }

}
