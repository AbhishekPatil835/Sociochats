package com.example.sociochat;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private double longitude;
    private double latitude;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();


        locationCallback = new LocationCallback();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initializing googleApiClient
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // googleMapOptions.mapType(googleMap.MAP_TYPE_HYBRID)
        //    .compassEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng india = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(india).title("Marker in India"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(india));
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    //Getting current location
    private void getCurrentLocation() {
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }




        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(200);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



        mFusedLocationClient.requestLocationUpdates(locationRequest,new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();

                if(location != null)
                {

                    longitude = location.getLongitude();
                    latitude = location.getLatitude();

                    senddatatoserver( longitude, latitude);

                    //moving the map to location
                    moveMap();

                }
            }
        },
        Looper.myLooper());




      /*  mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations, this can be null.
                        if (location != null) {
                            // Logic to handle location object

                            longitude = location.getLongitude();
                            latitude = location.getLatitude();

                            senddatatoserver( longitude, latitude);

                            //moving the map to location
                            moveMap();
                        }
                    }
                });*/
    }

    private void moveMap() {
        /**
         * Creating the latlng object to store lat, long coordinates
         * adding marker to map
         * move the camera with animation
         */
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Longitude " + longitude + "Latitude " + latitude));


        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(true);


    }

    @Override
    public void onClick(View view) {
        Log.v(TAG,"view click event");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Toast.makeText(MapsActivity.this, "onMarkerDragStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Toast.makeText(MapsActivity.this, "onMarkerDrag", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // getting the Co-ordinates
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        //move to current position
        moveMap();
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(MapsActivity.this, "onMarkerClick", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void senddatatoserver(double longitude, double latitude)
    {
        String longitude1 = String.valueOf(longitude);
        String latitude1 = String.valueOf(latitude);

        HashMap<String,Object> locationMap = new HashMap<>();
        locationMap.put("Longitude",longitude1);
        locationMap.put("Latitude",latitude1);

        RootRef.child("Users").child(currentUserID).child("Location")
                .updateChildren(locationMap);

    }





}