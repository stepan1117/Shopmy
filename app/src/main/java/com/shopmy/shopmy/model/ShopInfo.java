package com.shopmy.shopmy.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;

/**
 * Created by stepan on 4. 10. 2015.
 */

public class ShopInfo implements Parcelable{
    public enum DAYS {monday, tuesday, wednesday, thursday, friday, saturday, sunday, holidays};

    private long id = -1;
    private String name;
    private String address;
    private String url;
    private boolean active;
    private HashMap<String, List<TimeSpan>> openingHours = new HashMap<>();
    private LatLng position;


    public ShopInfo(){

    }

    protected ShopInfo(Parcel in) {
        id = in.readLong();
        name = in.readString();
        address = in.readString();
        url = in.readString();
        active = in.readByte() != 0;
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        position = new LatLng(latitude, longitude);
        openingHours = (HashMap<String, List<TimeSpan>>)in.readSerializable();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public HashMap<String, List<TimeSpan>> getOpeningHours() {
        return openingHours;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public static final Creator<ShopInfo> CREATOR = new Creator<ShopInfo>() {
        @Override
        public ShopInfo createFromParcel(Parcel in) {
            return new ShopInfo(in);
        }

        @Override
        public ShopInfo[] newArray(int size) {
            return new ShopInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(url);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeDouble(position.latitude);
        dest.writeDouble(position.longitude);
        dest.writeSerializable(openingHours);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopInfo shopInfo = (ShopInfo) o;

        return position.equals(shopInfo.position);
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }
}
