package com.shoppinmate.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.android.R;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.shoppinmate.android.adapter.ShopInfoWindowAdapter;
import com.shoppinmate.android.db.DeleteFromDbTask;
import com.shoppinmate.android.db.LoadFromDbTask;
import com.shoppinmate.android.db.PersistToDbTask;
import com.shoppinmate.android.maps.ShopInfoWrapper;
import com.shoppinmate.android.maps.algo.NonHierarchicalDistanceBasedShopItemAlgorithm;
import com.shoppinmate.android.maps.renderer.ShopClusterRenderer;
import com.shoppinmate.android.model.ShopInfo;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ShopListActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        ClusterManager.OnClusterItemInfoWindowClickListener<ShopInfoWrapper>,
        Toolbar.OnMenuItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private ShopClusterManager mClusterManager;

    private GoogleMap mMap;

    private LocationManager locationManager;

    public static final int RESULT_DELETE = -2;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);

        Toolbar t = (Toolbar)findViewById(R.id.map_toolbar);
        t.setTitleTextColor(Color.WHITE);
        t.setTitle(R.string.shops_map);
        t.inflateMenu(R.menu.map_menu);
        t.setOnMenuItemClickListener(this);

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
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.map_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

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
        readPreferences();

        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);

        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new ShopInfoWindowAdapter(getLayoutInflater()));
//        mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        mMap.setOnCameraChangeListener(mClusterManager);

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

        refreshFromDb();
        scheduleStatusUpdates();

        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showcase_displayed",false)) {

            new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(R.id.map, this))
                    .setContentTitle(getString(R.string.welcomeOverlayTitle))
                    .setContentText(getString(R.string.welcomeOverlayText))
                    .setStyle(R.style.ShoppinMateShowcaseTheme)
                    .hideOnTouchOutside()
                    .build();

//            try {
//                SVG svg = SVG.getFromResource(this, R.raw.hand);
//
//                SVGImageView svgImageView = new SVGImageView(this);
//                svgImageView.setSVG(svg);
//
//
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
//                        (RelativeLayout.LayoutParams.WRAP_CONTENT,
//                                RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//                params.addRule(RelativeLayout.CENTER_VERTICAL);
//                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                svgImageView.setLayoutParams(params);
//
//                v.addView(svgImageView);
//
//
//            } catch (SVGParseException e) {
//                e.printStackTrace();
//            }

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("showcase_displayed", true).commit();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            locationManager.removeUpdates(locationListener);
            timer.cancel();
            PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        } catch (SecurityException se){
            ;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            try {
                locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
            } catch (SecurityException se){
                se.printStackTrace();
            }
        }
        scheduleStatusUpdates();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
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

    private void scheduleStatusUpdates(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshStatuses();
            }
        }, DateTime.now().withSecondOfMinute(1).plusMinutes(1).toDate(), 1000*60);
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
                    persist(si);
                    refreshFromDb();
                } else if (requestCode == 2) {
                    persist(si);
                    refreshFromDb();
                }
            } else if (resultCode == RESULT_DELETE) {
                ShopInfo si = data.getParcelableExtra("shopInfo");
                delete(si);
            }
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }


    @Override
    public void onMarkerDragStart(Marker marker) {}

    @Override
    public void onMarkerDrag(Marker marker) {}

    @Override
    public void onMarkerDragEnd(Marker marker) {
//        ShopInfo info = mClusterManager.markerToInfo(marker);
//        info.setPosition(marker.getPosition());
//        persist(info);
    }

    private void delete(final ShopInfo info) {
        Log.d(this.getClass().getName(), "About to delete info.");
        DeleteFromDbTask task = new DeleteFromDbTask() {
            @Override
            protected void onPostExecute(Long shopId) {
                refreshFromDb();
            }
        };
        task.execute(info);

    }

    private void persist(ShopInfo info) {
        Log.d(this.getClass().getName(), "About to persist info.");
        new PersistToDbTask(){
            @Override
            protected void onPostExecute(Long aLong) {
                refreshFromDb();
            }
        }.execute(info);
    }

    private void refreshStatuses(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (ShopInfoWrapper info : mClusterManager.getItems()){
                    ShopClusterRenderer renderer = ((ShopClusterRenderer)mClusterManager.renderer);
                    Marker m = renderer.getMarker(info);
                    if (m != null){
                        m.setIcon(renderer.getShopStatusIcon(info));
                    }
                }
            }
        });
    }

    private void refreshFromDb(){
        LoadFromDbTask task = new LoadFromDbTask() {
            @Override
            protected void onPostExecute(List<ShopInfo> shops) {
                updateDisplayedShops(shops);
                mClusterManager.cluster();
            }
        };
        task.execute((LatLngBounds)null);
    }

    private void updateDisplayedShops(List<ShopInfo> shops) {

        List<ShopInfoWrapper> wrappers = new ArrayList<>();
        for (ShopInfo si : shops){
            wrappers.add(new ShopInfoWrapper(si));
        }

//        ShopClusterRenderer renderer = ((ShopClusterRenderer)mClusterManager.renderer);
//        for (ShopInfo shop : shops){
//            Marker m = renderer.getMarker(shop);
//            if (m == null)
//                continue;
//
//            renderer.getClusterItem(m).getOpeningHours().clear();
//            renderer.getClusterItem(m).getOpeningHours().putAll(shop.getOpeningHours());
//            renderer.getClusterItem(m).setName(shop.getName());
//            renderer.getClusterItem(m).setAddress(shop.getAddress());
//            renderer.getClusterItem(m).setUrl(shop.getUrl());
//            renderer.getClusterItem(m).setActive(shop.isActive());
//            renderer.getClusterItem(m).setPosition(shop.getPosition());
//
//        }
//        mClusterManager.addItems(shops);
//        mClusterManager.cluster();

        List<ShopInfoWrapper> toBeRemoved = new ArrayList<>();
        for (ShopInfoWrapper si : mClusterManager.getItems()) {
            if (!wrappers.contains(si)) {
                toBeRemoved.add(si);
            } else {
                shops.remove(si);
            }
        }

        for (ShopInfoWrapper si : toBeRemoved) {
            mClusterManager.removeItem(si);
        }

        for (ShopInfoWrapper shop : wrappers) {
            mClusterManager.addItem(shop);
        }

    }

//    @Override
//    public void onCameraChange(final CameraPosition cameraPosition) {
//        LoadFromDbTask task = new LoadFromDbTask() {
//            @Override
//            protected void onPostExecute(List<ShopInfo> shops) {
//                updateDisplayedShops(shops);
//                mClusterManager.onCameraChange(cameraPosition);
//
//            }
//        };
//        task.execute(mMap.getProjection().getVisibleRegion().latLngBounds);
//    }


    @Override
    public void onClusterItemInfoWindowClick(ShopInfoWrapper info) {
        Intent intent = new Intent(this, EditShopActivity.class);
        intent.putExtra("shopInfo", info.getInfo());
        startActivityForResult(intent, 2);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
//            case R.id.action_search:
//                Toast.makeText(getApplicationContext(), "Search not implemented yet", Toast.LENGTH_SHORT).show();
//                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    private void readPreferences(){
        if (mClusterManager != null) {
            boolean shopsClustering = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("enable_shops_clustering", true);
            mClusterManager.enableClustering(shopsClustering);

            int clusterSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("minimum_cluster_size", "7"));
            mClusterManager.setClusterSize(clusterSize);

            int minutesBeforeClosing = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("closing_soon_list", "15"));
            mClusterManager.setMinutesBeforeClosing(minutesBeforeClosing);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case "enable_shops_clustering":
                boolean shopsClustering = sharedPreferences.getBoolean("enable_shops_clustering", true);
                if (mClusterManager != null){
                    mClusterManager.enableClustering(shopsClustering);
                    mClusterManager.renderer.onAdd();
                }
                break;
            case "minimum_cluster_size":
                int clusterSize = Integer.parseInt(sharedPreferences.getString("minimum_cluster_size", "7"));
                if (mClusterManager != null){
                    mClusterManager.setClusterSize(clusterSize);
                }
                break;
            case "closing_soon_list":
                int minutesBeforeClosing = Integer.parseInt(sharedPreferences.getString("closing_soon_list", "15"));
                if (mClusterManager != null){
                    mClusterManager.setMinutesBeforeClosing(minutesBeforeClosing);
                    refreshStatuses();
                }
                break;
        }
    }

    public class ShopClusterManager extends ClusterManager<ShopInfoWrapper> {
        public Algorithm<ShopInfoWrapper> algorithm;
        public ClusterRenderer<ShopInfoWrapper> renderer;

        public ShopClusterManager(Context context, GoogleMap map) {
            super(context, map);
            renderer = new ShopClusterRenderer(context, map, this);
            algorithm = new PreCachingAlgorithmDecorator<>(new NonHierarchicalDistanceBasedShopItemAlgorithm<ShopInfoWrapper>());

            setRenderer(renderer);
            setAlgorithm(algorithm);
        }

        @Override
        public void setRenderer(ClusterRenderer<ShopInfoWrapper> view) {
            this.renderer = view;

            super.setRenderer(view);
        }

        public void enableClustering(boolean enable){
            ((ShopClusterRenderer)renderer).enableClustering(enable);
        }

        public void setClusterSize(int size){
            ((ShopClusterRenderer)renderer).setClusterSize(size);
        }

        public void setMinutesBeforeClosing(int minutes){
            ((ShopClusterRenderer)renderer).setMinutesBeforeClosing(minutes);
        }

//        public ShopInfo markerToInfo(Marker m) {
//            if (renderer instanceof DefaultClusterRenderer) {
//                return (ShopInfo) ((DefaultClusterRenderer) renderer).getClusterItem(m);
//            } else {
//                return null;
//            }
//        }

        public Collection<ShopInfoWrapper> getItems(){
            return algorithm.getItems();
        }

        @Override
        public void onCameraChange(final CameraPosition cameraPosition) {
            ShopClusterManager.super.onCameraChange(cameraPosition);

//            LoadFromDbTask task = new LoadFromDbTask() {
//                @Override
//                protected void onPostExecute(List<ShopInfo> shops) {
//                    updateDisplayedShops(shops);
//                    cluster();
//                    ShopClusterManager.super.onCameraChange(cameraPosition);
//                }
//            };
//            task.execute(mMap.getProjection().getVisibleRegion().latLngBounds);

        }
    }

}
