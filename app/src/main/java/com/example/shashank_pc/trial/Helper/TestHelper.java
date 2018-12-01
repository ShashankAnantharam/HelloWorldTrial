package com.example.shashank_pc.trial.Helper;

import android.content.Context;
import android.widget.Toast;

import com.example.shashank_pc.trial.classes.AlertContact;
import com.example.shashank_pc.trial.classes.CreatedBy;
import com.example.shashank_pc.trial.classes.Location;
import com.example.shashank_pc.trial.classes.Lookout;
import com.example.shashank_pc.trial.classes.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ThrowOnExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestHelper {

    public static void setTasks(Context context)
    {
        AlertContact alertContact = new AlertContact();
        alertContact.setTimeStamp(-1L);
        alertContact.setUserStatus("Pending");
        Map<String,AlertContact> selectedContacts = new HashMap<>();
        selectedContacts.put("+919701420818",alertContact);
        Location location = new Location();
        location.setLatitude(12.9214774);
        location.setLongitude(77.6691266);
        CreatedBy createdBy = new CreatedBy();
        createdBy.setId("919701420818");
        createdBy.setName("Shashank");

        Task task = new Task();
        task.setLocation(location);
        task.setCreatedBy(createdBy);
        task.setSelectedContacts(selectedContacts);
        task.setCompletedAt(-1L);
        task.setCompletedBy("");
        task.setDescription("Testing task");
        task.setName("Test task at bellandur");
        task.setHasDeadline(false);
        task.setAddress("Prestige Ferns Galaxy, Bellandur, Bangaluru, India");
        task.setDaily(false);
        task.setRadius(1270.34375);

        FirebaseFirestore.getInstance().collection("Users").document("+919701420818")
                .collection("Task(Others)").document("T123123133").set(task);

    }

    public static void setLookouts(Context context)
    {
        AlertContact alertContact = new AlertContact();
        alertContact.setId("+919701420818");
        alertContact.setTimeStamp(-1L);
        List<AlertContact> alertContactList = new ArrayList<>();
        alertContactList.add(alertContact);
        Location location = new Location();
        location.setLatitude(12.9214774);
        location.setLongitude(77.6691266);

        Lookout lookout1 = new Lookout();
        lookout1.setEnabled(true);
        lookout1.setDaily(true);
        lookout1.setSelectedContacts(alertContactList);
        lookout1.setLocation(location);
        lookout1.setName("MS Bellandur");
        lookout1.setRadius(127.734375);
        lookout1.setToTime(1533025800000L);

        lookout1.setFromTime(1532997000000L);
        lookout1.setAddress("Prestige Ferns Galaxy, Bellandur, Bangaluru, India");
        lookout1.setCreatedBy("+919701420818");
        lookout1.setCreatedByName("Shashank");

       // Toast.makeText(context,"Done",Toast.LENGTH_SHORT).show();

        Lookout lookout2 = new Lookout();
        lookout2.setEnabled(true);
        lookout2.setDaily(false);
        lookout2.setSelectedContacts(alertContactList);
        lookout2.setLocation(location);
        lookout2.setName("MS Bellandur");
        lookout2.setRadius(127.734375);
        lookout2.setToTime(1533025800000L);

        lookout2.setFromTime(1532997000000L);
        lookout2.setAddress("Prestige Ferns Galaxy, Bellandur, Bangaluru, India");
        lookout2.setCreatedBy("+919701420818");
        lookout2.setCreatedByName("Shashank");

        FirebaseFirestore.getInstance().collection("Users").document("+919701420818")
                .collection("Lookout(Others)").document("L123123133").set(lookout1);

        FirebaseFirestore.getInstance().collection("Users").document("+919701420818")
                .collection("Lookout(Others)").document("L123132331").set(lookout2);

    }

}
