package com.shopmy.shopmy.adapter;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.shopmy.shopmy.R;
import com.shopmy.shopmy.ShopListActivity;
import com.shopmy.shopmy.ShopmyApplication;
import com.shopmy.shopmy.format.HourMinuteFormatter;
import com.shopmy.shopmy.model.ShopInfo;
import com.shopmy.shopmy.model.TimeSpan;

import java.util.HashMap;
import java.util.List;

/**
 * Created by stepan on 4. 10. 2015.
 */
public class ShopInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View myContentsView;
    private final ShopListActivity.ShopClusterManager clusterManager;

    public ShopInfoWindowAdapter(LayoutInflater layoutInflater, ShopListActivity.ShopClusterManager manager){
        myContentsView = layoutInflater.inflate(R.layout.shop_info_content, null);
        this.clusterManager = manager;
    }

    @Override
    public View getInfoContents(Marker marker) {
        ShopInfo info = clusterManager.markerToInfo(marker);

        TextView tvTitle = (TextView)myContentsView.findViewById(R.id.title);
        tvTitle.setText(info.getName());
        TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
        tvSnippet.setText(Html.fromHtml(buildSnippet(info)));

        return myContentsView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    private String buildSnippet(ShopInfo shopInfo) {
        StringBuilder sb = new StringBuilder();

        HashMap<String, List<TimeSpan>> openingHours = shopInfo.getOpeningHours();

        for (ShopInfo.DAYS day : ShopInfo.DAYS.values()) {
            sb.append("<b>");
            sb.append(myContentsView.getResources().getString(
                    myContentsView.getResources()
                            .getIdentifier(
                                    day.toString(), "string", ShopmyApplication.getInstance().getPackageName())));
            sb.append("</b>: ");
            List<TimeSpan> spans = openingHours.get(day.toString());
            if (spans == null || spans.isEmpty()) {
                sb.append(myContentsView.getResources().getString(R.string.closed));
            } else {
                for (TimeSpan span : spans) {
                    sb.append(HourMinuteFormatter.formatTimeSpan(span) + ", ");
                }
            }
            sb.append("<br/>");
        }
        return sb.toString();
    }
}
