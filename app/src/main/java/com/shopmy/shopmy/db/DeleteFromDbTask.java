package com.shopmy.shopmy.db;

import android.os.AsyncTask;
import android.util.Log;

import com.shopmy.shopmy.ShopmyApplication;
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
 * Created by stepan on 30. 10. 2015.
 */
public class DeleteFromDbTask extends AsyncTask<ShopInfo, Void, Long> {

    @Override
    protected Long doInBackground(ShopInfo... params) {
        ShopInfo shopInfo = params[0];

        Connection con = null;
        try {
            con = DriverManager.getConnection(ShopmyApplication.getConnectionString(), "", "");
            con.setAutoCommit(false);

            deleteShopInfo(shopInfo, con);

            con.commit();
            con.setAutoCommit(true);
            return shopInfo.getId();
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Unable to delete",e);
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

    private void deleteShopInfo(ShopInfo shopInfo, Connection con) throws SQLException{
        PreparedStatement pst = con.prepareStatement("DELETE FROM OPENING_HOURS WHERE SHOP_ID = ?");
        pst.setLong(1, shopInfo.getId());
        pst.executeUpdate();
        pst.close();

        pst = con.prepareStatement(
                "DELETE FROM SHOP WHERE ID = ?");
        pst.setLong(1, shopInfo.getId());
        pst.executeUpdate();
        pst.close();
    }



}