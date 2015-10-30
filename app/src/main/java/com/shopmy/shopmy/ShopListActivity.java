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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.shopmy.shopmy.adapter.ShopInfoWindowAdapter;
import com.shopmy.shopmy.db.DeleteFromDbTask;
import com.shopmy.shopmy.db.LoadFromDbTask;
import com.shopmy.shopmy.db.PersistToDbTask;
import com.shopmy.shopmy.format.HourMinuteFormatter;
import com.shopmy.shopmy.model.ShopInfo;
import com.shopmy.shopmy.model.TimeSpan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShopListActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraChangeListener, ClusterManager.OnClusterItemClickListener<ShopInfo>,
        ClusterManager.OnClusterItemInfoWindowClickListener<ShopInfo>,
        GoogleMap.OnInfoWindowClickListener {

    private ShopClusterManager mClusterManager;

    private GoogleMap mMap;

    private LocationManager locationManager;

    public static final int RESULT_DELETE = -2;

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
        mClusterManager = new ShopClusterManager(this, mMap);

        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
//        mMap.setInfoWindowAdapter(new ShopInfoWindowAdapter(getLayoutInflater()));
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mClusterManager.setRenderer(new DefaultClusterRenderer<ShopInfo>(this, mMap, mClusterManager));
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new ShopInfoWindowAdapter(getLayoutInflater(), mClusterManager));
        mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(this);

        mMap.setOnCameraChangeListener(mClusterManager);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

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

                if (requestCode == 1) {
                    mClusterManager.addItem(si);
                    persist(si);
                } else if (requestCode == 2) {
                    mClusterManager.removeItem(si);
                    mClusterManager.addItem(si);
                    persist(si);
                }
            } else if (resultCode == RESULT_DELETE){
                ShopInfo si = data.getParcelableExtra("shopInfo");
                delete(si);
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

    private Marker buildMarker(ShopInfo shopInfo){
        StringBuilder sb = new StringBuilder();

        HashMap<String, List<TimeSpan>> openingHours = shopInfo.getOpeningHours();

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

        return mMap.addMarker(new MarkerOptions()
                .position(shopInfo.getPosition())
                .title(shopInfo.getName())
                .snippet(sb.toString())
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shopping_cart_24px)));
    }


//    @Override
//    public void onMarkerDragStart(Marker marker) {}
//
//    @Override
//    public void onMarkerDrag(Marker marker) {}

//    @Override
//    public void onMarkerDragEnd(Marker marker) {
//        ShopInfo info = markers.get(marker);
//        info.setPosition(marker.getPosition());
//        persist(info);
//    }

    private void delete(final ShopInfo info){
        Log.d(this.getClass().getName(), "About to delete info.");
        DeleteFromDbTask task = new DeleteFromDbTask(){
            @Override
            protected void onPostExecute(Long shopId) {
                mClusterManager.removeItem(info);
            }
        };
        task.execute(info);

    }

    private void persist(ShopInfo info){
        Log.d(this.getClass().getName(), "About to persist info.");
        new PersistToDbTask().execute(info);
    }


    private void updateDisplayedShops(List<ShopInfo> shops){
        mClusterManager.clearItems();
        if (shops != null){
            mClusterManager.addItems(shops);
        }

//        List<ShopInfo> toBeRemoved = new ArrayList<>();
//        for (ShopInfo si : mClusterManager.getItems()){
//            if (!shops.contains(si)){
//                toBeRemoved.add(si);
//            } else {
//                shops.remove(si);
//            }
//        }
//
//        for (ShopInfo si : toBeRemoved){
//            mClusterManager.removeItem(si);
//        }
//
//        for (ShopInfo shop : shops) {
//            mClusterManager.addItem(shop);
//        }

    }

    @Override
    public void onCameraChange(final CameraPosition cameraPosition) {
        LoadFromDbTask task = new LoadFromDbTask(){
            @Override
            protected void onPostExecute(List<ShopInfo> shops) {
                updateDisplayedShops(shops);
                mClusterManager.onCameraChange(cameraPosition);;
            }
        };
        task.execute(mMap.getProjection().getVisibleRegion().latLngBounds);
    }

    @Override
    public boolean onClusterItemClick(ShopInfo info) {
        Intent intent = new Intent(this, EditShopActivity.class);
        intent.putExtra("shopInfo", info);
        startActivityForResult(intent, 2);
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(ShopInfo info) {
        Intent intent = new Intent(this, EditShopActivity.class);
        intent.putExtra("shopInfo", info);
        startActivityForResult(intent, 2);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        ShopInfo info = mClusterManager.markerToInfo(marker);
        Intent intent = new Intent(this, EditShopActivity.class);
        intent.putExtra("shopInfo", info);
        startActivityForResult(intent, 2);
    }

    public class ShopClusterManager extends ClusterManager<ShopInfo>{
        private Algorithm<ShopInfo> algorithm = new PreCachingAlgorithmDecorator<>(new NonHierarchicalDistanceBasedAlgorithm());

        private ClusterRenderer<ShopInfo> renderer;

        public ShopClusterManager(Context context, GoogleMap map) {
            super(context, map);
            renderer = new DefaultClusterRenderer<>(context, map, this);
            setRenderer(renderer);
            setAlgorithm(algorithm);
        }

        @Override
        public void setRenderer(ClusterRenderer<ShopInfo> view) {
            this.renderer = view;
            super.setRenderer(view);
        }

        public ShopInfo markerToInfo(Marker m){
            if (renderer instanceof DefaultClusterRenderer){
                return (ShopInfo)((DefaultClusterRenderer)renderer).getClusterItem(m);
            } else {
                return null;
            }
        }

        public Collection<ShopInfo> getItems(){
            return algorithm.getItems();
        }

        @Override
        public void onCameraChange(final CameraPosition cameraPosition) {

            LoadFromDbTask task = new LoadFromDbTask(){
                @Override
                protected void onPostExecute(List<ShopInfo> shops) {
                    updateDisplayedShops(shops);
                    cluster();
//                    ShopClusterManager.super.onCameraChange(cameraPosition);
                }
            };
            task.execute(mMap.getProjection().getVisibleRegion().latLngBounds);

        }
    }

}
