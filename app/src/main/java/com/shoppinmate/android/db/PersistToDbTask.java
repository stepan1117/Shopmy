package com.shoppinmate.android.db;

import android.os.AsyncTask;
import android.util.Log;

import com.shoppinmate.android.ShoppinmateApplication;
import com.shoppinmate.android.model.ShopInfo;
import com.shoppinmate.android.model.TimeSpan;

import org.joda.time.DateTimeFieldType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Created by stepan on 18. 10. 2015.
 */
public class PersistToDbTask extends AsyncTask<ShopInfo, Void, Long> {

    @Override
    protected Long doInBackground(ShopInfo... params) {
        ShopInfo shopInfo = params[0];

        Connection con = null;
        try {
            con = DriverManager.getConnection(ShoppinmateApplication.getConnectionString(), "", "");
            con.setAutoCommit(false);

            Long shopId = shopInfo.getId();
            
            if (shopId != -1){
                updateShopInfo(shopInfo, con);
            } else {
                shopId = createShopInfo(shopInfo, con);
            }

            con.commit();
            shopInfo.setId(shopId);
            return shopId;
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Unable to persist",e);
            if (con != null){
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        } finally {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

        for (Map.Entry<String, List<TimeSpan>> entry : shopInfo.getOpeningHours().entrySet()){
            if (entry.getValue() != null){
                for (TimeSpan span : entry.getValue()) {
                    pst.setLong(1, shopInfo.getId());
                    pst.setString(2, entry.getKey());
                    pst.setInt(3, span.getStart().get(DateTimeFieldType.minuteOfDay()));
                    pst.setInt(4, span.getEnd().get(DateTimeFieldType.minuteOfDay()));
                    pst.executeUpdate();
                }
            }
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

        for (Map.Entry<String, List<TimeSpan>> entry : shopInfo.getOpeningHours().entrySet()){
            if (entry.getValue() != null){
                for (TimeSpan span : entry.getValue()) {
                    pst.setLong(1, shopId);
                    pst.setString(2, entry.getKey());
                    pst.setInt(3, span.getStart().get(DateTimeFieldType.minuteOfDay()));
                    pst.setInt(4, span.getEnd().get(DateTimeFieldType.minuteOfDay()));
                    pst.executeUpdate();
                }
            }
        }

        pst.close();
        
        return shopId;
    }

}
