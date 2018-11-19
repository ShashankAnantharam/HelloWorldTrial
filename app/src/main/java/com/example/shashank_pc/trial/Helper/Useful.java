package com.example.shashank_pc.trial.Helper;

public class Useful {
    /*

    private float powerSaverAlgoOld(float calulatedtime, Location location)
    {
        //Time is in seconds. Need to return in milliseconds
        Long currTime = System.currentTimeMillis();

        com.example.shashank_pc.trial.classes.Location fixLocation = BasicHelper.getFixLocation(getApplicationContext());
        Long fixLocTime = BasicHelper.getFixTime(getApplicationContext());

        Float fixLocDist=0f;

        if(fixLocation != null){
            Location fixLocWrapper = new Location("dummyprovider");
            fixLocWrapper.setLongitude(fixLocation.getLongitude());
            fixLocWrapper.setLatitude(fixLocation.getLatitude());
            fixLocDist =  location.distanceTo(fixLocWrapper);
        }
    //    Toast.makeText(getApplicationContext(),"fix Loc: "+Float.toString(fixLocDist),Toast.LENGTH_SHORT).show();

        if(fixLocation == null || (fixLocDist > 75f && BasicHelper.getErrorFlag(getApplicationContext()))){

            //TODO Check change
            fixLocation = new com.example.shashank_pc.trial.classes.Location();
            fixLocation.setLongitude(location.getLongitude());
            fixLocation.setLatitude(location.getLatitude());
            fixLocTime = currTime;

            BasicHelper.setFixLocation(getApplicationContext(),fixLocation);
            BasicHelper.setFixTime(getApplicationContext(),fixLocTime);
            turnOnFirebaseDatabases(getApplicationContext());

        }else if(fixLocDist > 75f && !BasicHelper.getErrorFlag(getApplicationContext())){
            BasicHelper.setErrorFlag(getApplicationContext(),true);
            calulatedtime = 3f;
        }else{
            BasicHelper.setErrorFlag(getApplicationContext(),false);

            //Logs
            FirebaseDatabase.getInstance().getReference("Testing/here").setValue(Long.toString(currTime-fixLocTime));
            if(currTime - fixLocTime > 300000){ // check time format  :: 720000 is in mills
                calulatedtime = Math.max(120, calulatedtime);

                /*
                  Set firebase databases to offline after 5 minutes of inactivity

    turnOffFirebaseDatabases(getApplicationContext(),isAppInForeground(getApplicationContext()));
}
            if(currTime - fixLocTime > 600000){
                    calulatedtime = Math.max(240, calulatedtime);
                    //this.showServiceExitedNotification();
                    }
                    if(currTime - fixLocTime > 1200000){
                    calulatedtime = Math.max(470, calulatedtime);
                    //this.showServiceExitedNotification();
                    }

                    }
//TODO Chek this if needed        AsyncStorage.setItem("PreviousLocation", JSON.stringify(location));

                    //TODO check time units
                    return calulatedtime*1000;

                    }
     */
}
