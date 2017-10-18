package com.example.shashank_pc.trial;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.example.shashank_pc.trial.Generic.database;
import static com.example.shashank_pc.trial.LandingPageActivity.allButtons;

/**
 * Created by shashank-pc on 9/12/2017.
 */

public class GPS_Service extends Service {

    private LocationListener locationListener;
    private LocationManager locationManager;
    private DatabaseReference writeGPS;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent notificationIntent = new Intent(this, LandingPageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        startForeground(1337,notification);


        String firebaseAddressLat = "Users/"+LandingPageActivity.getUserID()+"/Loc";

        writeGPS= database.getReference(firebaseAddressLat);






        //create locationListener
        locationListener= new LocationListener() {

            private void writeToGroupsEvents(Location location)
            {
                //Write to the references
                for(Map.Entry<String,Boolean> entry: allButtons.entrySet())
                {
                    String writeRef= "Loc/"+entry.getKey()+"/"+LandingPageActivity.getUserID();
                    DatabaseReference reference = database.getReference(writeRef);
                    if(!entry.getValue())
                    {
                        //Delete location if value is false
                        reference.removeValue();
                    }
                    else
                    {
                        //Update location if value true
                        Map <String,Double> locationMap= new HashMap<>();
                        locationMap.put("0",location.getLatitude());
                        locationMap.put("1",location.getLongitude());
                        reference.setValue(locationMap);

                    }
                }
            }


            @Override
            public void onLocationChanged(Location location) {


                Intent locationBackground= new Intent("location_update");
                locationBackground.putExtra("Latitude",location.getLatitude());
                locationBackground.putExtra("Longitude",location.getLongitude());
                sendBroadcast(locationBackground);
                Map<String, Double> gpsWrite= new HashMap<>();
                gpsWrite.put("Lat",location.getLatitude());
                gpsWrite.put("Long",location.getLongitude());
                writeGPS.setValue(gpsWrite);

                writeToGroupsEvents(location);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };



        //create locationManager
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        //if location manager is gps provider
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500, 0, locationListener);
        }
        //else check if network provider is network provider
        else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2500, 0, locationListener);
        }



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        stopForeground(true);
    }
}
