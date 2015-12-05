package com.shoppinmate.android.db;

import android.os.AsyncTask;
import android.util.Log;

import com.shoppinmate.android.ShoppinmateApplication;
import com.shoppinmate.android.model.ShopInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by stepan on 30. 10. 2015.
 */
public class DeleteFromDbTask extends AsyncTask<ShopInfo, Void, Long> {

    @Override
    protected Long doInBackground(ShopInfo... params) {
        ShopInfo shopInfo = params[0];

        Connection con = null;
        try {
            con = DriverManager.getConnection(ShoppinmateApplication.getConnectionString(), "", "");
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
