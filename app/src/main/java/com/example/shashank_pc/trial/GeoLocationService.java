package com.example.shashank_pc.trial;;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Binder;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.Manifest;
import android.widget.Toast;
import android.os.PowerManager.WakeLock;
import android.os.PowerManager;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicMarkableReference;

/*
    GeoLocationService extends Service -
    onStart - service specific lifecycle method.
*/
public class GeoLocationService extends Service {
    public static final String FOREGROUND = "com.facemap.location.FOREGROUND";
    private static int GEOLOCATION_NOTIFICATION_ID = 12345689;
    public static List<String> requestList;
    public static List<String> lookOutsList;
    public static List<String> tasksList;

    HashMap<String, Long> locReqData;

    private int hCount = 0;

    public static String STOP_ACTION = "com.lockquick.foregroundservice.action.stopforeground";


    private static final int TEN_MINUTES = 10 * 60 * 1000;
    private Handler handler;
    private Runnable sendData;
    private boolean current_gps_status;
    private DatabaseReference d;
    private  ChildEventListener ch;
    //   LocationListener locationListener;
    private WakeLock wakeLock = null;
    private int lookoutFlag = 0;
    private int requestsCount = 0;

    private boolean isNotificationReady = false;

    private long prevTimeStamp = -1; // to handle service stopping

    LocationManager locationManager = null;

    private class LocalBinder extends Binder {
        public GeoLocationService getService() {
            return GeoLocationService.this;
        }
    }

    public void setDatabaseConnectionStatus(boolean flag){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("IS_FIREBASE_ONLINE", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("IS_FIREBASE_ONLINE",flag);
        edit.commit();
    }

    public boolean getDatabaseConnectionStatus(){
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("IS_FIREBASE_ONLINE", Context.MODE_PRIVATE);
        return preferences.getBoolean("IS_FIREBASE_ONLINE", true);
    }

    public Boolean getErrorFlagStatus(){
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("ERROR_FLAG", Context.MODE_PRIVATE);
        return preferences.getBoolean("ERROR_FLAG", true);
    }

    public void turnOffFirebaseDatabases(){
        if(getDatabaseConnectionStatus() && !isAppInForeground()){
            FirebaseDatabase.getInstance().goOffline();
            FirebaseFirestore.getInstance().disableNetwork();
            setDatabaseConnectionStatus(false);
        }
    }

    public void turnOnFirebaseDatabases(){
        if(!getDatabaseConnectionStatus()){
            FirebaseDatabase.getInstance().goOnline();
            FirebaseFirestore.getInstance().enableNetwork();
            setDatabaseConnectionStatus(true);
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            locationManager.removeUpdates(this);
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FLAG", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putInt("FLAG",3);
            edit.commit();

//            GeoLocationService.this.sendMessage(location);
            handler.removeCallbacks(sendData);
            if(wakeLock != null && wakeLock.isHeld()){
                wakeLock.release();
            }
            handler.postDelayed(sendData, 2000);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
            if(current_gps_status == false){
                current_gps_status = true;
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FLAG", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putInt("FLAG",1);
                edit.commit();

                turnOnFirebaseDatabases();

                handler.removeCallbacks(sendData);
                if(wakeLock != null && !wakeLock.isHeld()){
                    wakeLock.acquire();
                }
                handler.postDelayed(sendData, 2000);
            }
        }

        @Override
        public void onProviderDisabled(String s) {
            if(current_gps_status == true){
                turnOffFirebaseDatabases();
                current_gps_status = false;
                locReqData.clear();
                if(wakeLock != null && wakeLock.isHeld()){
                    wakeLock.release();
                }
                handler.removeCallbacks(sendData);
            }
        }
    };

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onCreate() {
        super.onCreate();

        /*
            Create wakeLock.
        */
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyWakelockTag");
        wakeLock.setReferenceCounted(false);
        requestList = new ArrayList<>();
        lookOutsList = new ArrayList<>();
        tasksList = new ArrayList<>();
        locReqData = new HashMap<String, Long>();

        //Variable to get status of the GPS.
        current_gps_status = true;

        String userPhoneNumber = getUserPhoneNumber();


        // FirebaseDatabase database = FirebaseDatabase.getInstance();
        d =  FirebaseDatabase.getInstance().getReference("/broadcasting/" + userPhoneNumber + "/LocRequests/");

        ch =  new ChildEventListener() {


            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //Toast.makeText(getApplicationContext(), "child listener added", Toast.LENGTH_SHORT).show();

                String key = dataSnapshot.getKey();

                Timer newPost = dataSnapshot.getValue(Timer.class);
                long timerString = newPost.time;

                if(key.startsWith("+")){
                    locReqData.put(key, timerString);
                    requestList.add(key);
                    requestsCount = requestsCount + 1;
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String key = dataSnapshot.getKey();

                Timer newPost = dataSnapshot.getValue(Timer.class);
                System.out.println("newPost: " + newPost);
                long timerString = newPost.time;

                locReqData.put(key, timerString);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                requestList.remove(dataSnapshot.getKey());

                locReqData.remove(dataSnapshot.getKey());

                if(dataSnapshot.getKey().startsWith("+")){
                    requestsCount = requestsCount - 1;
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if(d != null && ch != null){
            turnOnFirebaseDatabases();
            d.addChildEventListener(ch);
        }

        handler = new Handler();


        sendData = new Runnable(){


            public void run(){


                if(getFlag() == 0){
                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FLAG", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putInt("FLAG",1);
                    edit.commit();
                    if(locationListener != null && locationManager!= null){
                        locationManager.removeUpdates(locationListener);
                    }
                    if(wakeLock != null && wakeLock.isHeld()){
                        wakeLock.release();
                    }
                    handler.postDelayed(this, 3000);
                }else{
                    try {
                        if(getFlag() == 1){
                            //Toast.makeText(getApplicationContext(), getAlarmDuration() + "  " +  System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
                            Log.d("ALARM_TIME", Long.toString(getAlarmDuration()));
                            Log.d("CURR_TIME", Long.toString(System.currentTimeMillis()));

                            // Toast.makeText(getApplicationContext(), "Alarm Time " + getAlarmDuration(),Toast.LENGTH_SHORT).show();


                            if(( (getAlarmDuration() - System.currentTimeMillis()) <= 0 || lookoutFlag != 0 || requestsCount != 0)){
                                // Toast.makeText(getApplicationContext(), "Alarm Time" + getAlarmDuration() + "Current time" + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();

                                if(wakeLock != null && !wakeLock.isHeld()){
                                    wakeLock.acquire();
                                }

                                if(getAlarmDuration() != prevTimeStamp){
                                    hCount = 0;
                                }

                                prevTimeStamp = getAlarmDuration();


                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FLAG", Context.MODE_PRIVATE);
                                SharedPreferences.Editor edit = sharedPreferences.edit();
                                edit.putInt("FLAG",2);
                                edit.commit();
                                if(locationManager == null) {
                                    locationManager =  (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                                }
                                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.ACCESS_FINE_LOCATION);
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
                                }
                                if(lookoutFlag == 0){
                                    handler.postDelayed(this, 35000);
                                }else{
                                    lookoutFlag = 0;
                                    handler.postDelayed(this,20000);
                                }
                            }else{
                                //Toast.makeText(getApplicationContext(), "false - flag is 1", Toast.LENGTH_SHORT).show();
                                //if(database != null){
                                // FirebaseDatabase.getInstance().getReference("temp").removeValue();
                                // FirebaseDatabase.getInstance().getReference("temp").setValue("Test");
                                handler.postDelayed(this,3000);
                                //}
                            }

                        } else if(getFlag() == 2){

                            // location listener not yet triggerd


                            if(locationListener != null && locationManager!= null){
                                locationManager.removeUpdates(locationListener);
                            }

                            Location lastLocation = new Location("service Provider");
                            lastLocation.setLatitude(2000);
                            lastLocation.setLongitude(2000);
                            GeoLocationService.this.sendMessage(lastLocation);
                            //Toast.makeText(getApplicationContext(), "flag is 2", Toast.LENGTH_SHORT).show();
                            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FLAG", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = sharedPreferences.edit();
                            edit.putInt("FLAG",3);
                            edit.commit();
                            if(wakeLock != null && wakeLock.isHeld()){
                                wakeLock.release();
                            }
                            handler.postDelayed(this, 10000);
                        }else if(getFlag() == 3){

                            writeCrashLogs("flag 3" + hCount);


                            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FLAG", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = sharedPreferences.edit();
                            edit.putInt("FLAG",0);
                            edit.commit();

                            //Toast.makeText(getApplicationContext(), "flag is 3", Toast.LENGTH_SHORT).show();
                            hCount++;
                            if(hCount == 6){
                                // SharedPreferences sharedPreferences1 = getApplicationContext().getSharedPreferences("IS_FOREGROUND", Context.MODE_PRIVATE);
                                // SharedPreferences.Editor edit1 = sharedPreferences1.edit();
                                // edit1.putString("IS_FOREGROUND","0");
                                // edit1.commit();
                                stopForeground(true);
                                stopSelf();
                                handler.removeCallbacks(sendData);
                            }
                            else{
                                handler.postDelayed(this, 4000);
                            }

                        }

//                        GeoLocationBroadcastReceiver sendEvent = new GeoLocationBroadcastReceiver();
//                        sendEvent.sendEvent(getApplicationContext());

                        //   handler.postDelayed(this, 1000);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


            //}
        };

        //Toast.makeText(getApplicationContext(),"START",Toast.LENGTH_SHORT).show();
        handler.postDelayed(sendData, 100);
    }



    private long getAlarmDuration(){
        SharedPreferences duration = getApplication().getSharedPreferences("ALARM_TIME", MODE_PRIVATE);
        return duration.getLong("ALARM_TIME",10);
    }

    private long getDuration(){
        SharedPreferences duration = getApplication().getSharedPreferences("DURATION", MODE_PRIVATE);
        return duration.getLong("DURATION",6000);
    }

    private int getFlag(){
        SharedPreferences duration = getApplication().getSharedPreferences("FLAG", MODE_PRIVATE);
        return duration.getInt("FLAG",1);
    }

    private String getUserPhoneNumber(){
        SharedPreferences duration = getApplication().getSharedPreferences("USERNUMBER", MODE_PRIVATE);
        return duration.getString("USERNUMBER","");
    }

    private void sendMessage(Location location) {
        try {
            Intent intent = new Intent("GeoLocationUpdate");
            intent.putExtra("message", location);


            String value = "";
            String deletedAlerts = "";

            System.out.println("locReqData: " + locReqData);

            for(Map.Entry<String, Long> e: locReqData.entrySet()){

                Date date = new Date();
                long mills = date.getTime() - e.getValue() ;


                if(mills > 600000 && e.getKey().startsWith("+")) {
                    //   delete node
                    DatabaseReference mDatabase =  FirebaseDatabase.getInstance().getReference("/broadcasting/" + getUserPhoneNumber() + "/LocRequests/" + e.getKey() );
                    mDatabase.removeValue();

                }else{
                    if(e.getValue() == 0){
                        deletedAlerts +=  e.getKey() + ",";
                    }else{
                        value += e.getKey() + ",";
                    }
                }

            }

            // Toast.makeText(getApplicationContext(), "request value" + value, Toast.LENGTH_LONG).show();

            Boolean errorFlag = getErrorFlagStatus();
            System.out.println(errorFlag);
            intent.putExtra("errorFlag",String.valueOf(errorFlag));
            intent.putExtra("reqContacts", value);
            intent.putExtra("deletedAlerts", deletedAlerts);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


            // remove lookouts and taks after sending to react native

//            for(int i = requestList.size()-1 ; i >= 0; i--){
//                String reqVal = requestList.get(i);
//                if(reqVal.startsWith("L") || reqVal.startsWith("T")) {
//                    requestList.remove(requestList.get(i));
//                }
//            }


            /**
             * Replacing with iterator due to ConcurrentModificationException
             *Ref - https://stackoverflow.com/questions/602636/concurrentmodificationexception-and-a-hashmap
             */
            // for(Map.Entry<String, Long> e: locReqData.entrySet()) {
            //     if(e.getKey().startsWith("L") || e.getKey().startsWith("T")) {
            //         locReqData.remove(e.getKey());
            //     }
            // }

            Iterator it = locReqData.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry<String, Long> e = (Map.Entry<String, Long>) it.next();
                if(e.getKey().startsWith("L") || e.getKey().startsWith("T")){
                    it.remove();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {

        if(locationListener != null && locationManager!= null) {
            locationManager.removeUpdates(locationListener);
        }
        super.onDestroy();
    }

    private boolean isAppInForeground(){
        SharedPreferences isInForeground = getApplication().getSharedPreferences("IS_FOREGROUND", MODE_PRIVATE);
        if(isInForeground.getString("IS_FOREGROUND","1").equals("0")){
            return false;
        }
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(isAppInForeground()){
            isNotificationReady = true;
            /*
                getCompatNotification() - To build sticky notification.
            */
            startForeground(GEOLOCATION_NOTIFICATION_ID, getCompatNotification());
        }

        /*
            If the user clicks on stop button on notification.
        */
        if(intent != null){
            if (intent.getAction().equals(STOP_ACTION)) {
                if(d != null && ch !=null){
                    d.removeEventListener(ch);
                    turnOffFirebaseDatabases();
                    d = null;
                }
                if(locationListener != null && locationManager!= null){
                    locationManager.removeUpdates(locationListener);
                }
                handler.removeCallbacks(sendData);
                stopForeground(true);
                stopSelf();
            }
        }
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
        //return mBinder;
    }


    public void onTaskRemoved(Intent rootIntent) {

        this.writeCrashLogs("onTaskRemoved");
    }
    void writeCrashLogs(String crashType){

        DatabaseReference mDatabase =  FirebaseDatabase.getInstance().getReference("/broadcasting/" + getUserPhoneNumber() + "/crashlogs/flag3/" + System.currentTimeMillis() );
        mDatabase.setValue(crashType);
    }


    private Notification getCompatNotification() {
        isNotificationReady = true;
        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel();
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            channelId = "";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);


        Intent stopIntent = new Intent(this, GeoLocationService.class);
        stopIntent.setAction(STOP_ACTION);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0,
                stopIntent, 0);

        String str = "Using your location in the background";
        builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Facemap")
                .setContentText(str)
                .setTicker(str)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(android.R.drawable.ic_media_pause, "Stop",
                        pstopIntent)
                .setWhen(System.currentTimeMillis());

        Intent startIntent = new Intent(getApplicationContext(), LandingPageActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1000, startIntent, 0);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 1000, stopIntent, 0);

        builder.setContentIntent(contentIntent);
        return builder.build();
    }




    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(){

        String channelId = "my_service";
        String channelName = "My Background Service";

        NotificationChannel channel = new NotificationChannel(channelId,
                channelName,
                NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});

        //val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationManager mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.createNotificationChannel(channel);


        return channelId;

    }





    public static class Timer {

        public long time;

//        public Timer(String time) {
//            this.time = time;
//        }

        public Timer(){}
    }



}
