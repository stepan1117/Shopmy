package com.shopmy.shopmy.db;

import android.os.AsyncTask;
import android.util.Log;

import com.shopmy.shopmy.ShopmyApplication;
import com.shopmy.shopmy.format.HourMinuteFormatter;
import com.shopmy.shopmy.model.OpeningHours;
import com.shopmy.shopmy.model.ShopInfo;
import com.shopmy.shopmy.model.TimeSpan;

import org.joda.time.DateTimeFieldType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by stepan on 18. 10. 2015.
 */
public class PersistToDbTask extends AsyncTask<ShopInfo, Void, Long> {

    @Override
    protected Long doInBackground(ShopInfo... params) {
        ShopInfo shopInfo = params[0];
        Connection con = null;
        try {
            con = DriverManager.getConnection(ShopmyApplication.getConnectionString(), "", "");
            con.setAutoCommit(false);
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

            con.commit();
            con.setAutoCommit(true);
            shopInfo.setId(shopId);
            return shopId;
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Unable to persist",e);
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

}
