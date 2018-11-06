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

import com.example.shashank_pc.trial.Helper.BasicHelper;
import com.example.shashank_pc.trial.classes.Alert;
import com.example.shashank_pc.trial.classes.BackupLocationRetriever;
import com.example.shashank_pc.trial.classes.Lookout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicMarkableReference;

import static com.example.shashank_pc.trial.Helper.AlertHelper.shouldCheckAlert;
import static com.example.shashank_pc.trial.Helper.BasicHelper.isAppInForeground;
import static com.example.shashank_pc.trial.Helper.BasicHelper.populateAlerts;
import static com.example.shashank_pc.trial.Helper.BasicHelper.turnOffFirebaseDatabases;
import static com.example.shashank_pc.trial.Helper.BasicHelper.turnOnFirebaseDatabases;
import static com.example.shashank_pc.trial.classes.Algorithm.shouldTriggerAlert;

/*
    GeoLocationService extends Service -
    onStart - service specific lifecycle method.
*/
public class GeoLocationService extends Service {
    public static final String FOREGROUND = "com.facemap.location.FOREGROUND";
    private static int GEOLOCATION_NOTIFICATION_ID = 12345689;
    public static List<String> lookOutsList;
    public static List<String> tasksList;

    private DocumentReference contactRef;

    private BackupLocationRetriever backupLocationRetriever;

    Map<String,Alert> alertMap= new ConcurrentHashMap<>();
    Map<String,Long> userSet = new ConcurrentHashMap<>();
    Map<String,String> contactStatus = new ConcurrentHashMap<>();


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
    private int alertFlag = 0;

    private boolean isNotificationReady = false;

    private long prevTimeStamp = -1; // to handle service stopping

    LocationManager locationManager = null;

    private Location prevLoc= null;


    private class LocalBinder extends Binder {
        public GeoLocationService getService() {
            return GeoLocationService.this;
        }
    }

    public Boolean getErrorFlagStatus(){
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("ERROR_FLAG", Context.MODE_PRIVATE);
        return preferences.getBoolean("ERROR_FLAG", true);
    }

    private void getContactsFromDb()
    {
        contactRef = FirebaseFirestore.getInstance().collection("Users").document("+919701420818")
                .collection("activities").document("contacts");

        contactRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                List<Map<String,Object>> list = (List) documentSnapshot.get("Lists");

                for(Map<String,Object> listItem: list)
                {
                    String status = "";
                    String id = (String)listItem.get("id");

                    if((Boolean)listItem.get("freezeMode"))
                    {
                        status="F";
                    }
                    else if((Boolean) listItem.get("isBroadcasting"))
                    {
                        status = "Y";
                    }
                    else
                    {
                        status = "N";
                    }
                    contactStatus.put(id,status);
                    FirebaseDatabase.getInstance().getReference("Testing/Contacts/"+id).setValue(status);
                }
            }
        });
    }

    private void getAlertsFromDb()
    {
        FirebaseFirestore.getInstance().collection("Users").document("+919701420818")
                .collection("Lookout(Others)").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                for(DocumentSnapshot documentSnapshot: documentSnapshots)
                {
                    Lookout lookout = documentSnapshot.toObject(Lookout.class);
                    String lId =  documentSnapshot.getId();
                    lookout.setId(lId);
//                    Toast.makeText(getApplicationContext(), new Gson().toJson(lookout),Toast.LENGTH_LONG).show();
                    alertMap.put(lId,lookout);
                    FirebaseDatabase.getInstance().getReference("Testing/Alerts/"+lId).setValue(System.currentTimeMillis());
                }
            }
        });


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
            if (prevLoc==null)
            {
                prevLoc=location;
            }
            handler.removeCallbacks(sendData);
            if(wakeLock != null && wakeLock.isHeld()){
                wakeLock.release();
            }
            List<Alert> alerts = populateAlerts(alertMap);
            mainAlgo(alerts,location.getLatitude(),location.getLongitude(),
                    prevLoc.getLatitude(),prevLoc.getLongitude());

            prevLoc = location;
            handler.postDelayed(sendData, 2000);

         //   Toast.makeText(getApplicationContext(),"loc",Toast.LENGTH_SHORT).show();
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

                turnOnFirebaseDatabases(getApplicationContext());

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
                turnOffFirebaseDatabases(getApplicationContext(),isAppInForeground(getApplicationContext()));
                current_gps_status = false;
                userSet.clear();
                if(wakeLock != null && wakeLock.isHeld()){
                    wakeLock.release();
                }
                handler.removeCallbacks(sendData);
            }
        }
    };

    private void updateAlertInMap(String id)
    {
        Alert alert = alertMap.get(id);
        for(int i=0;i<alert.getSelectedContacts().size();i++)
        {
            if(alert.getSelectedContacts().get(i).getId().equals(getUserPhoneNumber()))
            {
                alert.getSelectedContacts().get(i).setTimeStamp(System.currentTimeMillis());
                break;
            }
        }
        alertMap.remove(id);
        alertMap.put(id,alert);
    }


    private void mainAlgo(List<Alert> alerts, double x_curr, double y_curr, double x_prev, double y_prev)
    {
        for(Alert alert: alerts)
        {
            if(shouldCheckAlert(alert,contactStatus,getUserPhoneNumber()))
            {
                FirebaseDatabase.getInstance().getReference("testingBigTime/"+alert.getId()).setValue("check");
                if(shouldTriggerAlert(x_curr, y_curr, x_prev, y_prev, alert))
                {
                    //Trigger alert
                    FirebaseDatabase.getInstance().getReference("testingBigTime/"+alert.getId()).setValue("pp");

                    updateAlertInMap(alert.getId());

                    //TODO Calculate distance here
                }
            }
        }
    }

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
        lookOutsList = new ArrayList<>();
        tasksList = new ArrayList<>();
        backupLocationRetriever.init(getApplicationContext());

        //Variable to get status of the GPS.
        current_gps_status = true;

        String userPhoneNumber = getUserPhoneNumber();

        getAlertsFromDb();
        getContactsFromDb();

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
                    userSet.put(key, timerString);
                }
                else
                {
                    alertFlag=1;
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String key = dataSnapshot.getKey();

                Timer newPost = dataSnapshot.getValue(Timer.class);
                System.out.println("newPost: " + newPost);
                long timerString = newPost.time;

                userSet.put(key, timerString);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                userSet.remove(dataSnapshot.getKey());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if(d != null && ch != null){
            turnOnFirebaseDatabases(getApplicationContext());
            d.addChildEventListener(ch);
        }

        handler = new Handler();


        sendData = new Runnable(){


            public void run(){


                if(getFlag() == 0){
                   // Toast.makeText(getApplicationContext(),"0", Toast.LENGTH_SHORT).show();
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

                         //   Toast.makeText(getApplicationContext(),"1", Toast.LENGTH_SHORT).show();
                            if(( (getAlarmDuration() - System.currentTimeMillis()) <= 0 || alertFlag != 0 || userSet.size()>0)){
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
                                if(alertFlag == 0){
                                    handler.postDelayed(this, 35000);
                                }else{
                                    alertFlag = 0;
                                    handler.postDelayed(this,20000);
                                }
                            }else{
                                handler.postDelayed(this,3000);
                            }

                        } else if(getFlag() == 2){

                        //    Toast.makeText(getApplicationContext(),"2", Toast.LENGTH_SHORT).show();
                            // location listener not yet triggerd
                            if(locationListener != null && locationManager!= null){
                                locationManager.removeUpdates(locationListener);
                            }

                            Location lastLocation = new Location("service Provider");
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

                         //   Toast.makeText(getApplicationContext(),"3", Toast.LENGTH_SHORT).show();
                            writeCrashLogs("flag 3" + hCount);


                            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FLAG", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = sharedPreferences.edit();
                            edit.putInt("FLAG",0);
                            edit.commit();

                            setAlarmDuration(3000L);

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
        };

        //Toast.makeText(getApplicationContext(),"START",Toast.LENGTH_SHORT).show();
        handler.postDelayed(sendData, 100);
    }



    private long getAlarmDuration(){
        SharedPreferences duration = getApplication().getSharedPreferences("ALARM_TIME", MODE_PRIVATE);
        return duration.getLong("ALARM_TIME",10);
    }

    private void setAlarmDuration(Long milliseconds){
        SharedPreferences duration = getApplication().getSharedPreferences("ALARM_TIME", MODE_PRIVATE);
        SharedPreferences.Editor edit = duration.edit();
        edit.putLong("ALARM_TIME",System.currentTimeMillis()+milliseconds);
        edit.commit();
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
//        return duration.getString("USERNUMBER","");
        return "+919701420818";
    }

    private void sendMessage(Location location) {
        try {
            Intent intent = new Intent("GeoLocationUpdate");
            intent.putExtra("message", location);


            String value = "";
            String deletedAlerts = "";


            for(Map.Entry<String, Long> e: userSet.entrySet()){

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
            Iterator it = userSet.entrySet().iterator();
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




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(isAppInForeground(getApplicationContext())){
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
            if ((intent.getAction()!=null) && intent.getAction().equals(STOP_ACTION)) {
                BasicHelper.setServiceStatus(getApplicationContext(),false);
                if(d != null && ch !=null){
                    d.removeEventListener(ch);
                    turnOffFirebaseDatabases(getApplicationContext(),isAppInForeground(getApplicationContext()));
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
