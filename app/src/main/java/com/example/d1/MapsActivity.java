package com.example.d1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int LOCATION_REQUEST_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get permission to access fine location
        setupPermissions();

/*
        // TODO: 8/20/19   Test that location access permission is granted.
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission.
            // See the documentation for Activity#requestPermissions for more details.
            return;
        }
*/
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location lastLocation) {
                        // Got last known location. In rare situations this could be null.
                        if (lastLocation != null) {
                            mLastLocation = lastLocation;
                        }
                    }
                });
    }

    private void setupPermissions() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to access location is denied");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(this);
                builder.setMessage("Permission to access location info is required for this app.")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                Log.i(TAG, "Clicked");
                                makeRequest();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                makeRequest();
            }
        }
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {

                if (grantResults.length == 0
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "User Denies Location Permission");

                } else {
                    Log.i(TAG, "User Grants Location Permission");
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this case, we just add a GC marker near Columbia, MD.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.i(TAG, "Map is ready");

        // Add a marker in Columbia MD, and move the camera
        LatLng columbiaGC = new LatLng(39.225618, -76.9001157);
        mMap.addMarker(new MarkerOptions()
                .position(columbiaGC)
                .title("Columbia GC")
                .draggable(false)
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(columbiaGC));

        // Get current device location
        GPSTracker mGpsTracker = new GPSTracker(getApplicationContext());
        mCurrentLocation = mGpsTracker.getLocation();
        if (mCurrentLocation == null) {
            mCurrentLocation = mLastLocation;
            if (mCurrentLocation == null) {
                Log.i(TAG, "mCurrentLocation cannot be found");
            }
        }
        Log.i(TAG, "mCurrentLocation: "
                +"lat= "+mCurrentLocation.getLatitude()
                +", lng= "+mCurrentLocation.getLongitude());

        // TODO: 8/19/19 display current location marker on map 
    }

}
