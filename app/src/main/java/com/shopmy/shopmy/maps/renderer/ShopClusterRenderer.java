package com.shopmy.shopmy.maps.renderer;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.shopmy.shopmy.R;
import com.shopmy.shopmy.ShopmyApplication;
import com.shopmy.shopmy.format.HourMinuteFormatter;
import com.shopmy.shopmy.maps.ShopInfoWrapper;
import com.shopmy.shopmy.model.ShopInfo;
import com.shopmy.shopmy.model.TimeSpan;
import com.shopmy.shopmy.service.ShopInfoService;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Stepan on 31. 10. 2015.
 */
public class ShopClusterRenderer extends DefaultClusterRenderer<ShopInfoWrapper> {
    public ShopClusterRenderer(Context context, GoogleMap map, ClusterManager<ShopInfoWrapper> clusterManager) {
        super(context, map, clusterManager);
    }


    public BitmapDescriptor getShopStatusIcon(ShopInfoWrapper info){
        BitmapDescriptor icon = null;

        switch (ShopInfoService.decideShopStatus(info.getInfo())){
            case OPEN:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.shopping_cart_green);
                break;
            case CLOSING_SOON:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.shopping_cart_orange);
                break;
            case CLOSED:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.shopping_cart_red);
                break;
        }
        return icon;
    }

    @Override
    public void onBeforeClusterItemRendered(ShopInfoWrapper info, MarkerOptions markerOptions) {

        BitmapDescriptor icon = getShopStatusIcon(info);

        markerOptions
                .title(info.getInfo().getName())
                .snippet(buildSnippet(info.getInfo()))
                .draggable(true)
                .icon(icon);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<ShopInfoWrapper> cluster) {
        return cluster.getSize() > 7;
    }

    private String buildSnippet(ShopInfo shopInfo) {
        StringBuilder sb = new StringBuilder();

        HashMap<String, List<TimeSpan>> openingHours = shopInfo.getOpeningHours();

        for (ShopInfo.DAYS day : ShopInfo.DAYS.values()) {
            sb.append("<b>");
            sb.append(ShopmyApplication.instance.getStringFromResource(
                    ShopmyApplication.instance.getResourceId(day.toString())
            ));
            sb.append("</b>: ");
            List<TimeSpan> spans = openingHours.get(day.toString());
            if (spans == null || spans.isEmpty()) {
                sb.append(ShopmyApplication.instance.getStringFromResource(R.string.closed));
            } else {
                for (TimeSpan span : spans) {
                    sb.append(HourMinuteFormatter.formatTimeSpan(span) + ", ");
                }
                sb.delete(sb.length()-2, sb.length());
            }
            sb.append("<br/>");
        }
        return sb.toString();
    }
}
