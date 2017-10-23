package com.example.shashank_pc.trial;

import android.content.Intent;
import android.location.LocationListener;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.data.DataBufferObserver;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import static com.example.shashank_pc.trial.Generic.firestore;

public class NearbyEventsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Map<String,Marker> nearbyEvents;
    private CollectionReference firestoreNearbyEvents;

    private LocationListener locationListener;
    private Double latitude;
    private Double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_events);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        nearbyEvents= new HashMap<>();


    }


    private void getNearbyPlaces()
    {
        firestoreNearbyEvents= firestore.collection("advertisedEvents");
        Double lat_low= latitude-0.01;
        Double lat_high= latitude+0.01;
        Double long_low= longitude-0.01;
        Double long_high= longitude+0.01;

        Query query = firestoreNearbyEvents;
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {


                for(DocumentSnapshot documentSnapshot: documentSnapshots)
                {
                    double lat=0, lon=0;
                    String val="";

                    Map <String,Object> map = documentSnapshot.getData();
                    for(Map.Entry<String,Object> entry: map.entrySet())
                    {


                        Toast.makeText(getApplicationContext(),"Here",Toast.LENGTH_SHORT).show();

                        if(entry.getKey().equals("ID"))
                        {
                            val = (String) entry.getValue();
                            Toast.makeText(getApplicationContext(),val,Toast.LENGTH_SHORT).show();

                        }
                        else if(entry.getKey().equals("lat"))
                        {
                            lat = (double) entry.getValue();
                        }
                        else if(entry.getKey().equals("long"))
                        {
                            lon = (double) entry.getValue();
                        }



                    }
                    nearbyEvents.put(val,
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lon)))
                    );
                }

            }
        });

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

        Intent getLocationIntent = new Intent("location_update");
        latitude = getLocationIntent.getDoubleExtra("Latitude",0);
        longitude = getLocationIntent.getDoubleExtra("Longitude",0);

        LatLng location = new LatLng(latitude, longitude);

     //   mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,10));

        getNearbyPlaces();


    }
}
