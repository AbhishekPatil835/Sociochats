package com.example.sociochat;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.HashMap;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback,
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
    private FusedLocationProviderClient mFusedLocationClient;

    private DatabaseReference RootRef;
    private String messagerecieverID;

    private double latitude1,longitude1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        RootRef = FirebaseDatabase.getInstance().getReference();


        Intent intent = getIntent();


        messagerecieverID = intent.getStringExtra("messagerecieverID");



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
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

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

            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>()
                {
                    @Override
                    public void onSuccess(Location location)
                    {
                        // Got last known location. In some rare situations, this can be null.
                        if (location != null) {
                            // Logic to handle location object

                            //Getting longitude and latitude

                            //moving the map to location
                            moveMap();
                        }
                    }
                });

    }



    private void moveMap() {
        /**
         * Creating the latlng object to store lat, long coordinates
         * adding marker to map
         * move the camera with animation
         */





        Toast.makeText(this, messagerecieverID, Toast.LENGTH_SHORT).show();

        RootRef.child("Users").child(messagerecieverID)
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.child("Location").hasChild("Longitude") && dataSnapshot.child("Location").hasChild("Latitude"))
                        {

                            longitude1 = Double.parseDouble(String.valueOf(dataSnapshot.child("Location").child("Longitude").getValue()));
                             latitude1 = Double.parseDouble(String.valueOf(dataSnapshot.child("Location").child("Latitude").getValue()));

                            Toast.makeText(MapsActivity2.this, "Got location of user" +latitude1+","+longitude1, Toast.LENGTH_LONG).show();


                            LatLng latLng = new LatLng(latitude1,longitude1);
                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .draggable(true)
                                    .title(longitude1+","+latitude1));

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                            mMap.getUiSettings().setZoomControlsEnabled(true);




                        }
                        else
                        {
                            Toast.makeText(MapsActivity2.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });





















    }

    @Override
    public void onClick(View view)
    {
        Log.v(TAG,"view click event");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    @Override
    public void onMapLongClick(LatLng latLng)
    {
        // mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
    }

    @Override
    public void onMarkerDragStart(Marker marker)
    {
        Toast.makeText(MapsActivity2.this, "onMarkerDragStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDrag(Marker marker)
    {
        Toast.makeText(MapsActivity2.this, "onMarkerDrag", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragEnd(Marker marker)
        {
        // getting the Co-ordinates
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        //move to current position
        moveMap();
    }

    @Override
    protected void onStart()
    {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        googleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public boolean onMarkerClick(Marker marker)
    {
        Toast.makeText(MapsActivity2.this, "onMarkerClick", Toast.LENGTH_SHORT).show();
        return true;
    }

   /* public void getlocationofuser(String messageReceiverID)
    {
        Toast.makeText(this, messageReceiverID, Toast.LENGTH_SHORT).show();

        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.child("Location").hasChild("Longitude") && dataSnapshot.child("Location").hasChild("Latitude"))
                        {
                            Double longitude1 = Double.parseDouble(String.valueOf(dataSnapshot.child("Location").child("Longitude").getValue()));
                            Double latitude1 = Double.parseDouble(String.valueOf(dataSnapshot.child("Location").child("Latitude").getValue()));

                            longitude = longitude1;
                            latitude = latitude1;


                        }
                        else
                        {
                            Toast.makeText(MapsActivity2.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });

    }*/




}