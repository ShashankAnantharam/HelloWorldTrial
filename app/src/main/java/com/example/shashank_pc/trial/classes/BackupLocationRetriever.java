package com.example.shashank_pc.trial.classes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.shashank_pc.trial.Helper.BasicHelper.setLocationToLocal;

public class BackupLocationRetriever {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private Context context;

    public void init(Context appContext) {
        this.context = appContext;
        initLocationClientRelatedClasses();
        pollLocationOnce();

    }

    public void initLocationClientRelatedClasses()
    {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                //Write logs to db
                FirebaseDatabase.getInstance().getReference("Testing/updatesHere").setValue(locationResult.getLastLocation());

                //Latest known location to be set
                setLocationToLocal(context,locationResult.getLastLocation());

                //Stop recieving more updates
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            };
        };
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null)
                {
                    //Set location to Local
                    setLocationToLocal(context,location);
                }
                else{
                    pollLocationOnce();
                }
            }
        });
    }

    private void pollLocationOnce()
    {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

}
