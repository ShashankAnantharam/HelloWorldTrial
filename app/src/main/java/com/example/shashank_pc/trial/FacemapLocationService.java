package com.example.shashank_pc.trial;;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.Manifest;
import android.widget.Toast;
import android.os.PowerManager.WakeLock;
import android.os.PowerManager;

import com.example.shashank_pc.trial.Helper.BasicHelper;
import com.example.shashank_pc.trial.classes.Alert;
import com.example.shashank_pc.trial.classes.Algorithm;
import com.example.shashank_pc.trial.classes.BackupLocationRetriever;
import com.example.shashank_pc.trial.classes.Lookout;
import com.example.shashank_pc.trial.classes.Task;
import com.example.shashank_pc.trial.userStatusClasses.DetectedActivitiesIntentService;
import com.example.shashank_pc.trial.userStatusClasses.DetectedActivityWrappers;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.shashank_pc.trial.Helper.AlertHelper.shouldCheckAlert;
import static com.example.shashank_pc.trial.Helper.AlertHelper.triggerAlert;
import static com.example.shashank_pc.trial.Helper.BasicHelper.isAppInForeground;
import static com.example.shashank_pc.trial.Helper.BasicHelper.populateAlerts;
import static com.example.shashank_pc.trial.Helper.BasicHelper.turnOffFirebaseDatabases;
import static com.example.shashank_pc.trial.Helper.BasicHelper.turnOnFirebaseDatabases;
import static com.example.shashank_pc.trial.classes.Algorithm.shouldTriggerAlert;

/*
    FacemapLocationService extends Service -
    onStart - service specific lifecycle method.
*/
public class FacemapLocationService extends Service {
    public static final String FOREGROUND = "com.facemap.location.FOREGROUND";
    private static int GEOLOCATION_NOTIFICATION_ID = 12345689;
    private static int DETECTION_INTERVAL_IN_MILLISECONDS = 30*1000;
    public static List<String> lookOutsList;
    public static List<String> tasksList;

    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;

    private DocumentReference contactRef;
    private BackupLocationRetriever backupLocationRetriever= new BackupLocationRetriever();;

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
        public FacemapLocationService getService() {
            return FacemapLocationService.this;
        }
    }

    private void getUpdatedAlertFromDb(final String alertId)
    {
        if(alertId.startsWith("L"))
        {
            //Lookout
            FirebaseFirestore.getInstance().collection("Users").document(getUserPhoneNumber())
                    .collection("Lookout(Others)").document(alertId).get().
                    addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(!documentSnapshot.exists())
                    {
                        if(alertMap!=null)
                            alertMap.remove(alertId);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Exists",Toast.LENGTH_SHORT).show();
                        if(alertMap!=null) {
                            if(alertMap.containsKey(alertId))
                                alertMap.remove(alertId);
                            Lookout lookout = documentSnapshot.toObject(Lookout.class);
                            lookout.setId(alertId);
                            alertMap.put(alertId, lookout);
                        }
                        alertFlag=1;
                    }
                    FirebaseDatabase.getInstance().getReference("/broadcasting/" + getUserPhoneNumber() +
                            "/LocRequests/"+alertId).removeValue();
                }
            });
        }
        else if(alertId.startsWith("T"))
        {
            //Task
            FirebaseFirestore.getInstance().collection("Users").document(getUserPhoneNumber())
                    .collection("Task(Others)").document(alertId).get().
                    addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(!documentSnapshot.exists())
                            {
                                if(alertMap!=null)
                                    alertMap.remove(alertId);
                            }
                            else
                            {
                                if(alertMap!=null) {
                                    if(alertMap.containsKey(alertId))
                                        alertMap.remove(alertId);
                                    Task task = documentSnapshot.toObject(Task.class);
                                    task.setId(alertId);
                                    alertMap.put(alertId, task);
                                }
                                alertFlag=1;
                                FirebaseDatabase.getInstance().getReference("/broadcasting/" + getUserPhoneNumber() +
                                        "/LocRequests/"+alertId).removeValue();
                            }
                        }
                    });
        }
        else
        {
            return ;
        }

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

    private  boolean hasAlertJustBeenSetForYou()
    {
        return (alertFlag!=0);
    }

    private boolean isTimeToCheckLocation()
    {
        return (getAlarmDuration() - System.currentTimeMillis() <= 0);
    }

    private boolean isSomeContactLookingAtYou()
    {
        return (userSet.size()>0);
    }

    private void broadcastLocationToAllFriends(double latitude, double longitude)
    {
        Map<String,Object> locVal = new HashMap<>();
        locVal.put("latitude",latitude);
        locVal.put("longitude",longitude);
        locVal.put("time",System.currentTimeMillis());

        for(Map.Entry<String,Long> friend : userSet.entrySet())
        {
            FirebaseDatabase.getInstance().getReference("broadcasting/"+friend.getKey() +"/cts/"+getUserPhoneNumber()).setValue(locVal);
        }
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


        FirebaseFirestore.getInstance().collection("Users").document("+919701420818")
                .collection("Task(Others)").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                for(DocumentSnapshot documentSnapshot: documentSnapshots)
                {
                    Task task = documentSnapshot.toObject(Task.class);
                    String tId =  documentSnapshot.getId();
                    task.setId(tId);
//                    Toast.makeText(getApplicationContext(), new Gson().toJson(lookout),Toast.LENGTH_LONG).show();
                    alertMap.put(tId,task);
                    FirebaseDatabase.getInstance().getReference("Testing/Alerts/"+tId).setValue(System.currentTimeMillis());
                }
            }
        });

    }


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

         //   Toast.makeText(getApplicationContext(),"Location acquired", Toast.LENGTH_SHORT).show();
            //Get location
            locationManager.removeUpdates(this);
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FLAG", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putInt("FLAG",0);
            edit.commit();

//            FacemapLocationService.this.sendMessage(location);
            if (prevLoc==null)
            {
                prevLoc=location;
            }
            handler.removeCallbacks(sendData);
            if(wakeLock != null && wakeLock.isHeld()){
                wakeLock.release();
            }
            List<Alert> alerts = populateAlerts(alertMap);
            mainAlgo(alerts,location,prevLoc);

            prevLoc = location;

            if(isSomeContactLookingAtYou())
            {
                /*
                If someone is looking at you then send location to them
                 */
                broadcastLocationToAllFriends(location.getLatitude(),location.getLongitude());
            }

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
                edit.putInt("FLAG",2);
                edit.commit();

                turnOnFirebaseDatabases(getApplicationContext());

                handler.removeCallbacks(sendData);
                if(wakeLock != null && !wakeLock.isHeld()){
                    wakeLock.acquire(20000);
                }
                handler.postDelayed(sendData, 20000);
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

    private void updateAlertInMap(String id) {

        Alert alert = alertMap.get(id);
        boolean doesAlertContainUser = false;
        if (alert instanceof Lookout) {
            for (int i = 0; i < ((Lookout) alert).getSelectedContacts().size(); i++) {
                if (((Lookout) alert).getSelectedContacts().get(i).getId().equals(getUserPhoneNumber())) {
                    ((Lookout) alert).getSelectedContacts().get(i).setTimeStamp(System.currentTimeMillis());
                    doesAlertContainUser = true;
                    break;
                }
            }
        } else if (alert instanceof Task) {
            if (((Task) alert).getSelectedContacts().containsKey(getUserPhoneNumber())) {
                doesAlertContainUser = true;
                ((Task) alert).getSelectedContacts().get(getUserPhoneNumber()).setTimeStamp(System.currentTimeMillis());
            }
        }

        alertMap.remove(id);
        //If alert does not contain user anymore, then remove it!
        if (doesAlertContainUser)
            alertMap.put(id, alert);

    }


    private void mainAlgo(List<Alert> alerts, Location currLoc, Location prevLoc)
    {
        double x_curr = currLoc.getLatitude();
        double y_curr = currLoc.getLongitude();
        double x_prev = prevLoc.getLatitude();
        double y_prev = prevLoc.getLongitude();

        Float minDist = Float.MAX_VALUE;
        for(Alert alert: alerts)
        {

            if(shouldCheckAlert(alert,contactStatus,getUserPhoneNumber()))
            {

                FirebaseDatabase.getInstance().getReference("testingBigTime/"+alert.getId()).setValue("check");
                if(shouldTriggerAlert(x_curr, y_curr, x_prev, y_prev, alert))
                {
                    //Trigger alert
                    FirebaseDatabase.getInstance().getReference("testingBigTime/"+alert.getId()).setValue("pp");
                    FirebaseDatabase.getInstance().
                            getReference("broadcasting/"+getUserPhoneNumber()+
                                    "/TriggerAlerts/"+alert.getId()).setValue(System.currentTimeMillis());
                    updateAlertInMap(alert.getId());
                    triggerAlert(alert,getApplicationContext(),getUserPhoneNumber(),getUserName());

                    //TODO Calculate distance here
                }
                else
                {
                    minDist=Math.min(minDist,
                            Algorithm.shortestDistanceFromAlert(x_curr, y_curr, x_prev, y_prev, alert));
                }
            }
            else
            {
                FirebaseDatabase.getInstance().getReference("testingBigTime/"+alert.getId()).setValue("noCheck");
            }
        }
     //   Toast.makeText(getApplicationContext(),Float.toString(minDist)+" in meters",Toast.LENGTH_SHORT).show();
        float time = 3f;
        if(userSet.size()==0)
        {
            time = Algorithm.calculateTime(getApplicationContext(),minDist);
        }
        time*=1000;

 //       time = powerSaverAlgo(time,currLoc);
//        Toast.makeText(getApplicationContext(),"Final Time : "+ Float.toString(time),Toast.LENGTH_SHORT ).show();
        FirebaseDatabase.getInstance().getReference("Testing/loc/loclogs/"+Long.toString(System.currentTimeMillis()))
                .setValue(currLoc);
        FirebaseDatabase.getInstance().getReference("Testing/loc/Timelogs/"+Long.toString(System.currentTimeMillis()))
                .setValue(Float.toString(time));
        setAlarmDuration((long) time);


    }

    private boolean shouldTurnDbOff(){
        /*
        If the phone has been still for more than 6 minutes AND app is in background, then turn db off
         */

        //If app was not in foreground, then no need of waiting for 6 min
        if(!BasicHelper.wasAppInForground())
            return true;


        Long currTime = System.currentTimeMillis();
        if(isAppInForeground(getApplicationContext()))
        {
            //If App is in foreground, do nothing except update last still time for now
            BasicHelper.setLastStillTime(getApplicationContext(),currTime);
        }
        else if(currTime - BasicHelper.getLastStillTime(getApplicationContext())> 6*60*1000)
        {
            //If app was in foreground, now is in background AND has been still for more than 6 minutes.
 //           FirebaseDatabase.getInstance().getReference("Debug/dbState/"+
  //                  Long.toString(currTime)).setValue("TurningThisDbOff");
            BasicHelper.setWasAppInForground(false);
            return true;
        }
        return false;
    }

    private boolean isTimeForPeriodicCheck()
    {
        /*
        If phone has been still for last 1 hour, then check once now to see the location
         */
        Long currTime = System.currentTimeMillis();
//        Toast.makeText(getApplicationContext(),"Deficit periodic: "+Long.toString(currTime - BasicHelper.getLastStillTime(getApplicationContext())),Toast.LENGTH_SHORT).show();
        if(currTime - BasicHelper.getLastStillTime(getApplicationContext())> 60*60*1000)
        {
            FirebaseDatabase.getInstance().getReference("Debug/dbState/"+
                    Long.toString(currTime)).setValue("TurningThisDbOnOnce");
            return true;
        }
        return false;
    }



    private boolean shouldContinue(){

        if(isAppInForeground(getApplicationContext()))
            return true;

        DetectedActivityWrappers latestActivity = BasicHelper.getUserMovementState(getApplicationContext());

  //      Toast.makeText(getApplicationContext(),latestActivity.getActivityType(),Toast.LENGTH_SHORT).show();

        if(latestActivity.getActivityType().equals("Still") || latestActivity.getActivityType().equals("Tilting")
                || latestActivity.getActivityType().equals("Unknown"))
        {
            if(isTimeForPeriodicCheck())
            {
                //Only check once, so set the time to 6 minutes previously and turn db on for one cycle
                BasicHelper.setLastStillTime(getApplicationContext(), System.currentTimeMillis()-6*60*1000L);
                turnOnFirebaseDatabases(getApplicationContext());
   //             Toast.makeText(getApplicationContext(),"True",Toast.LENGTH_SHORT).show();
                return true;
            }

            if(shouldTurnDbOff())
                turnOffFirebaseDatabases(getApplicationContext(),BasicHelper.isAppInForeground(getApplicationContext()));
            return false;
        }
        else {

            //Set current time as the last Still time for reference
            BasicHelper.setLastStillTime(getApplicationContext(), System.currentTimeMillis());
        }

        turnOnFirebaseDatabases(getApplicationContext());
        return true;
    }

    public void requestActivityUpdatesButtonHandler() {
        com.google.android.gms.tasks.Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_IN_MILLISECONDS,
                mPendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getApplicationContext(), "Successfully requested activity updates", Toast.LENGTH_SHORT).show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Requesting activity updates failed to start",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }


    @SuppressLint("InvalidWakeLockTag")
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
        if(backupLocationRetriever==null)
            backupLocationRetriever = new BackupLocationRetriever();
        backupLocationRetriever.init(getApplicationContext());
        setAlarmDuration(0L);

        //Variable to get status of the GPS.
        current_gps_status = true;

        String userPhoneNumber = getUserPhoneNumber();

        getAlertsFromDb();
        getContactsFromDb();

        //Get the user activity
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        mIntentService = new Intent(this, DetectedActivitiesIntentService.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
        requestActivityUpdatesButtonHandler();

        // FirebaseDatabase database = FirebaseDatabase.getInstance();
        d =  FirebaseDatabase.getInstance().getReference("/broadcasting/" + userPhoneNumber + "/LocRequests/");

        ch =  new ChildEventListener() {


            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //Toast.makeText(getApplicationContext(), "child listener added", Toast.LENGTH_SHORT).show();

                String key = dataSnapshot.getKey();


                if(key.startsWith("+")){
                    Timer newPost = dataSnapshot.getValue(Timer.class);
                    long timerString = newPost.time;
                    userSet.put(key, timerString);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),key,Toast.LENGTH_SHORT).show();
                    String alertString  = key;
                    getUpdatedAlertFromDb(alertString);
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
            //turnOnFirebaseDatabases(getApplicationContext());
            d.addChildEventListener(ch);
        }

        handler = new Handler();


        sendData = new Runnable(){


            public void run(){


                if(getFlag() == 0){
                //   Toast.makeText(getApplicationContext(),"0", Toast.LENGTH_SHORT).show();
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

                    removeExcessLocationRequests();

                    handler.postDelayed(this, 3000);
                }else{
                    try {
                        if(getFlag() == 1){

                            boolean shouldContinue = shouldContinue();
                    //        Toast.makeText(getApplicationContext(), Boolean.toString(shouldContinue)+" TimeDeficit: " + Long.toString(getAlarmDuration() - System.currentTimeMillis()), Toast.LENGTH_SHORT).show();


                            if( hasAlertJustBeenSetForYou() ||
                                    (( isTimeToCheckLocation() || isSomeContactLookingAtYou())
                                    && shouldContinue)
                                    ){
                    //             Toast.makeText(getApplicationContext(), "Inside", Toast.LENGTH_SHORT).show();

                                if(wakeLock != null && !wakeLock.isHeld()){
                                    wakeLock.acquire(35000);
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


                    //        Toast.makeText(getApplicationContext(),"2: Backup listener fired", Toast.LENGTH_SHORT).show();
                            // Location listener not yet triggered, so use backup listener instead
                            if(locationListener != null && locationManager!= null){
                                locationManager.removeUpdates(locationListener);
                            }

                            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if(location==null)
                                backupLocationRetriever.getLocation();
                            else
                                BasicHelper.setLocationToLocal(getApplicationContext(),location);

                            //Location lastLocation = new Location("service Provider");
                            //FacemapLocationService.this.sendMessage(lastLocation);
                            //Toast.makeText(getApplicationContext(), "flag is 2", Toast.LENGTH_SHORT).show();
                            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FLAG", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = sharedPreferences.edit();
                            edit.putInt("FLAG",3);
                            edit.commit();

                            handler.postDelayed(this, 13000);
                        }else if(getFlag() == 3){

                    //        Toast.makeText(getApplicationContext(),"3: Backup listener output", Toast.LENGTH_SHORT).show();
                            //Backup listener gives output
                            Location location = BasicHelper.getLocationFromLocal(getApplicationContext());
                            if (prevLoc==null)
                            {
                                prevLoc=location;
                            }
                            List<Alert> alerts = populateAlerts(alertMap);
                            mainAlgo(alerts,location,prevLoc);
                            prevLoc=location;
                            if(wakeLock != null && wakeLock.isHeld()){
                                wakeLock.release();
                            }

                            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FLAG", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = sharedPreferences.edit();
                            edit.putInt("FLAG",0);
                            edit.commit();
                    //        Toast.makeText(getApplicationContext(),"3: Here", Toast.LENGTH_SHORT).show();
                            handler.postDelayed(this, 2000);
                        }

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
        //TODO Set this right

        SharedPreferences duration = getApplication().getSharedPreferences("USERNUMBER", MODE_PRIVATE);
//        return duration.getString("USERNUMBER","");
        return "+919701420818";
    }

    private String getUserName(){
        //TODO Set this right
        return "Shashank";
    }


    private void removeExcessLocationRequests()
    {
        for(Map.Entry<String, Long> e: userSet.entrySet()){

            Date date = new Date();
            long mills = date.getTime() - e.getValue() ;

            if(mills > 600000 && e.getKey().startsWith("+")) {
                //   delete node
                DatabaseReference mDatabase =  FirebaseDatabase.getInstance().getReference("/broadcasting/" + getUserPhoneNumber() + "/LocRequests/" + e.getKey() );
                mDatabase.removeValue();

            }
        }

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
            Boolean errorFlag = BasicHelper.getErrorFlag(getApplicationContext());
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


            isNotificationReady = true;
            /*
                getCompatNotification() - To build sticky notification.
            */
            startForeground(GEOLOCATION_NOTIFICATION_ID, getCompatNotification());

        /*
            If the user clicks on stop button on notification.
        */
        if(intent != null){
            if ((intent.getAction()!=null) && intent.getAction().equals(STOP_ACTION)) {
                BasicHelper.setServiceStatus(getApplicationContext(),false);
                if(d != null && ch !=null){
                    d.removeEventListener(ch);
                 //   OOffFirebaseDatabases(getApplicationContext(),isAppInForeground(getApplicationContext()));
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

        FirebaseDatabase.getInstance().getReference("Testing/logs/brdcst").setValue("onTaskRemoved");
        BasicHelper.setAppInForeground(getApplicationContext(),false);
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


        Intent stopIntent = new Intent(this, FacemapLocationService.class);
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
