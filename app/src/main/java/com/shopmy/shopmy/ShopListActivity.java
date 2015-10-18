package com.shopmy.shopmy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.shopmy.shopmy.adapter.ShopInfoWindowAdapter;
import com.shopmy.shopmy.format.HourMinuteFormatter;
import com.shopmy.shopmy.model.ShopInfo;
import com.shopmy.shopmy.model.TimeSpan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopListActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;

    private LocationManager locationManager;

    private Map<Marker, ShopInfo> markers = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
        }
        setUpLocationManager();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(new ShopInfoWindowAdapter(getLayoutInflater()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerDragListener(this);

        if (locationManager != null) {
            try {
                Location myLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
                if (myLocation != null) {
                    LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                    // Show the current location in Google Map
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    // Zoom in the Google Map
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            } catch (SecurityException e) {
            }
        }
    }

    private void locationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        setUpLocationManager();
    }

    private void setUpLocationManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
        }
    }


    LocationListener locationListener = new LocationListener() {

        private boolean moved = false;

        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if (!moved) {
                moved = true;
                locationChanged(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };


    @Override
    public void onMapLongClick(LatLng point) {
        Intent intent = new Intent(this, EditShopActivity.class);
        intent.putExtra("position", point);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1 || requestCode == 2) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                ShopInfo si = data.getParcelableExtra("shopInfo");
                StringBuilder sb = new StringBuilder();

                HashMap<String, List<TimeSpan>> openingHours = si.getOpeningHours();

                for (ShopInfo.DAYS day : ShopInfo.DAYS.values()) {
                    sb.append("<b>");
                    sb.append(getResources().getString(
                            getResources()
                                    .getIdentifier(
                                            day.toString(), "string", this.getPackageName())));
                    sb.append("</b>: ");
                    List<TimeSpan> spans = openingHours.get(day.toString());
                    if (spans == null || spans.isEmpty()) {
                        sb.append(getResources().getString(R.string.closed));
                    } else {
                        for (TimeSpan span : spans) {
                            sb.append(HourMinuteFormatter.formatTimeSpan(span) + ", ");
                        }
                    }
                    sb.append("<br/>");
                }

                if (requestCode == 1) {
                    Marker m = mMap.addMarker(new MarkerOptions()
                            .position(si.getPosition())
                            .title(si.getName())
                            .snippet(sb.toString())
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.shopping_cart_24px)));
                    markers.put(m, si);
                    persist(si);
                } else if (requestCode == 2) {
                    Marker m = null;
                    for (Map.Entry<Marker, ShopInfo> shop : markers.entrySet()) {
                        if (shop.getValue().equals(si)) {
                            m = shop.getKey();
                            m.setTitle(si.getName());
                            m.setSnippet(sb.toString());
                            m.showInfoWindow();
                            persist(si);
                            break;
                        }
                    }
                    markers.put(m, si);
                }
            }
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
//        Intent intent = new Intent(this, EditShopActivity.class);
//        startActivity(intent);
//        return false;
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        ShopInfo si = markers.get(marker);
        Intent intent = new Intent(this, EditShopActivity.class);
        intent.putExtra("shopInfo", si);
        startActivityForResult(intent, 2);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {}

    @Override
    public void onMarkerDrag(Marker marker) {}

    @Override
    public void onMarkerDragEnd(Marker marker) {
        ShopInfo info = markers.get(marker);
        info.setPosition(marker.getPosition());
        persist(info);
    }

    private void persist(ShopInfo info){
        Log.d(this.getClass().getName(), "About to persist info.");
    }

}
