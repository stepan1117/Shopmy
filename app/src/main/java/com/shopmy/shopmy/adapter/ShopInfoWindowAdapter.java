package com.shopmy.shopmy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.shopmy.shopmy.R;

/**
 * Created by stepan on 4. 10. 2015.
 */
public class ShopInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View myContentsView;

    public ShopInfoWindowAdapter(LayoutInflater layoutInflater){
        myContentsView = layoutInflater.inflate(R.layout.shop_info_content, null);
    }

    @Override
    public View getInfoContents(Marker marker) {

        TextView tvTitle = (TextView)myContentsView.findViewById(R.id.title);
        tvTitle.setText(marker.getTitle());
        TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
        tvSnippet.setText(marker.getSnippet());

        return myContentsView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

}
