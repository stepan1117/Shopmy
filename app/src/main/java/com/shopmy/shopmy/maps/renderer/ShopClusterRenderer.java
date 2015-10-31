package com.shopmy.shopmy.maps.renderer;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.shopmy.shopmy.R;
import com.shopmy.shopmy.model.ShopInfo;

/**
 * Created by Stepan on 31. 10. 2015.
 */
public class ShopClusterRenderer extends DefaultClusterRenderer<ShopInfo> {
    public ShopClusterRenderer(Context context, GoogleMap map, ClusterManager<ShopInfo> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(ShopInfo info, MarkerOptions markerOptions) {
        markerOptions
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shopping_cart_24px));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<ShopInfo> cluster) {
        return cluster.getSize() > 7;
    }
}
