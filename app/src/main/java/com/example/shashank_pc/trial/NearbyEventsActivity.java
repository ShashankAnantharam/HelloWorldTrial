package com.example.shashank_pc.trial;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_events);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        nearbyEvents= new HashMap<>();
        getNearbyPlaces();
    }


    private void getNearbyPlaces()
    {
        firestoreNearbyEvents= firestore.collection("advertisedEvents");
        Query query = firestoreNearbyEvents.whereGreaterThan("lat",17.442139);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                for(DocumentSnapshot documentSnapshot: documentSnapshots)
                {
                    Map <String,Object> map = documentSnapshot.getData();
                    for(Map.Entry<String,Object> entry: map.entrySet())
                    {
                        if(entry.getKey().equals("ID"))
                        {
                            String val = (String) entry.getValue();
                            Toast.makeText(getApplicationContext(),val,Toast.LENGTH_SHORT).show();
                        }
                    }
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


    }
}
