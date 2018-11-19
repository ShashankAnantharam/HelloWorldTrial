package com.example.shashank_pc.trial.userStatusClasses;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.shashank_pc.trial.Helper.BasicHelper;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DetectedActivitiesIntentService  extends IntentService {

    protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        DetectedActivityWrappers wrapper = null;
        int maxConfidence = 0;
        for (DetectedActivity activity : detectedActivities) {
            Log.e(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
            if(wrapper==null || wrapper.getConfidence()>maxConfidence)
            {
                if(!wrapper.getActivityType().equals("Unknown") && !wrapper.getActivityType().equals("Tilting"))
                {
                    wrapper = new DetectedActivityWrappers(activity);
                }
            }
        }

        if(wrapper!=null)
            broadcastActivity(wrapper);
    }

    private void broadcastActivity(DetectedActivityWrappers activity) {
        Intent intent = new Intent(Constants.BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("type", activity.getActivityType());
        intent.putExtra("confidence", activity.getConfidence());

        BasicHelper.setUserMovementState(getApplicationContext(),activity);


        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        //FirebaseDatabase.getInstance().getReference("NewTesting/"+Long.toString(System.currentTimeMillis())).setValue(new DetectedActivityWrappers(activity));
    }

    private class UserActivityLogs{

    }

}