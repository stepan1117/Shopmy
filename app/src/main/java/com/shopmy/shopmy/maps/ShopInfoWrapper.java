package com.shopmy.shopmy.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.shopmy.shopmy.model.ShopInfo;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Stepan on 31. 10. 2015.
 */
public class ShopInfoWrapper implements ClusterItem {
    private ShopInfo info;

    public ShopInfoWrapper(ShopInfo info){
        this.info = info;
    }

    public ShopInfo getInfo() {
        return info;
    }

    public void setInfo(ShopInfo info) {
        this.info = info;
    }

    @Override
    public LatLng getPosition() {
        return info.getPosition();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ShopInfoWrapper shopInfoWrapper = (ShopInfoWrapper) o;

        return new EqualsBuilder()
                .append(info.getId(), shopInfoWrapper.getInfo().getId())
                .append(info.isActive(), shopInfoWrapper.getInfo().isActive())
                .append(info.getName(), shopInfoWrapper.getInfo().getName())
                .append(info.getAddress(), shopInfoWrapper.getInfo().getAddress())
                .append(info.getUrl(), shopInfoWrapper.getInfo().getUrl())
                .append(info.getOpeningHours(), shopInfoWrapper.getInfo().getOpeningHours())
                .append(info.getPosition(), shopInfoWrapper.getInfo().getPosition())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(info.getId())
                .append(info.getName())
                .append(info.getAddress())
                .append(info.getUrl())
                .append(info.isActive())
                .append(info.getOpeningHours())
                .append(info.getPosition())
                .toHashCode();
    }

}
