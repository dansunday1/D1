package com.example.d1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnSuccessListener;


public class MapsActivity extends FragmentActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int LOCATION_REQUEST_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;
    private GPSTracker mGpsTracker;
    private Location mLastLocation;
    private Location mLocation;
    private GoogleMap mMap;
    private CameraPosition mCameraPosition = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // get permission to access fine location
        setupPermissions();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get the last location used by this app (whenever)
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location lastLocation) {
                        // Got last known location. In rare situations this could be null.
                        if (lastLocation != null) {
                            mLastLocation = lastLocation;
                            Log.i(TAG, "mLastLocation: "
                                    +"lat= "+mLastLocation.getLatitude()
                                    +", lng= "+mLastLocation.getLongitude());
                        } else {
                            // maybe there isn't one for this phone
                            Log.i(TAG, "mLastLocation not found");
                        }
                    }
                });

        mGpsTracker = new GPSTracker(getApplicationContext());
        createLocationRequest();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.i(TAG, "Map is ready");

        // Customise the styling of the base map using a JSON object defined
        // in a string resource file. First create a MapStyleOptions object
        // from the JSON styles string, then pass this to the setMapStyle
        // method of the GoogleMap object.
        boolean successStyle = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));
        if (!successStyle) {
            Log.e(TAG, "Map style json parsing failed.");
        }
        // enable displaying my location on the map, including click processing
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        // Get current device location
        mLocation = mGpsTracker.getLocation();
        if (mLocation == null) {
            mLocation = mLastLocation;
            if (mLocation == null) {
                Log.i(TAG, "mLocation cannot be found");
                // TODO: 8/21/19 Give feedback to user re location not available
                return;     // done
            }
        }
        Log.i(TAG, "mLocation: "
                +"lat= "+mLocation.getLatitude() +", lng= "+mLocation.getLongitude());


        // move to initial location
        LatLng here = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(here));

        // TODO: 8/21/19 implement mLocation updates

        // Add a marker for HG GC in Columbia MD
        LatLng columbiaGC = new LatLng(39.225618, -76.9001157);
        mMap.addMarker(new MarkerOptions()
                .position(columbiaGC)
                .title("Hobbits GC")
                .draggable(false)
        );


        // TODO: 8/21/19 Mark ball location
        // TODO: 8/21/19 Mark target pin location
        // TODO: 8/21/19 Mark aimPoint (dragable)
        // TODO: 8/21/19 Show lines and distances: ball->aimPt->pin

        // TODO: 8/21/19 Show distance rings from ball toward target pin

    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    /*
    * Access Location Permission code.
    * TODO: 8/21/19 Consider putting access permission code in separate utility class
    */
    private void setupPermissions() {
        Log.i(TAG, "setupPermissions");
/* orig:
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
*/
        int permission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission not yet granted to access location");
            // Call Activity#requestPermissions here
            // to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission.
            // See the documentation for Activity#requestPermissions for more details.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(this);
                builder.setMessage("Permission to access the location is needed for this app.")
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
        Log.i(TAG, "makeRequest");
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult= "+grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "User Denies Location Permission");
                    // TODO: 8/22/19 Explain that app can't be run without location access. Sorry.

                } else {
                    Log.i(TAG, "User Grants Location Permission");
                }
            }
        }
    }

}
