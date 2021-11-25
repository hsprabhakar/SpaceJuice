package com.example.spacejuice.controller;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.spacejuice.Habit;
import com.example.spacejuice.HabitEvent;
import com.example.spacejuice.MainActivity;
import com.example.spacejuice.Member;
import com.example.spacejuice.activity.HabitDetailsActivity;
import com.example.spacejuice.activity.LoginActivity;
import com.example.spacejuice.activity.UploadImageActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.OnProgressListener;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.StorageTask;
//import com.google.firebase.storage.UploadTask;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HabitEventController {

    public static void addHabitEvent(Habit habit, HabitEvent habitEvent) {
        // adds a HabitEvent to the array of events contained by a Habit
        habit.addEvent(habitEvent);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int uid = habit.getUid();
        Query habitQuery =
                db.collection("Members").document(MainActivity.getUser()
                        .getMemberName()).collection("Habits")
                        .whereEqualTo("ID", uid);
        Task<QuerySnapshot> habitQueryTask = habitQuery.get();
        habitQueryTask.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {    //get the document with the proper habit uID
                DocumentReference habitRef = Objects.requireNonNull(habitQueryTask
                        .getResult()).getDocuments().get(0).getReference();
                DocumentReference eventDocRef;
                //eventDocRef = habitRef.collection("Events").document(String.valueOf(habitEvent.getEventId()));
                eventDocRef = habitRef.collection("Events").document(getDocumentIdString(habitEvent));
                eventDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("Id", habitEvent.getEventId());
                            data.put("Description", habitEvent.getDescription());
                            data.put("Date", habitEvent.getDate());
                            data.put("Url", habitEvent.getImage());
                            data.put("Location",new GeoPoint(habitEvent.getLatitude(), habitEvent.getLongitude()));
                            data.put("Done", habitEvent.isDone());
                            eventDocRef.set(data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("debugInfo", "Habit has been added successfully");
                                            LoginController.updateMaxID();
                                        }
                                    });
                        }
                    }
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void loadHabitEventsFromFirebase(Habit habit, final HabitController.OnHabitLoaded callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int uid = habit.getUid();
        Query habitQuery =
                db.collection("Members").document(MainActivity.getUser()
                        .getMemberName()).collection("Habits")
                        .whereEqualTo("ID", uid);
        Task<QuerySnapshot> habitQueryTask = habitQuery.get();
        habitQueryTask.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {    //get the document with the proper habit uID
                Log.d("debugInfo", "retrieved habit events from firebase");
                DocumentReference habitRef = Objects.requireNonNull(habitQueryTask
                        .getResult()).getDocuments().get(0).getReference();
                CollectionReference collectionReference = habitRef.collection("Events");
                Task<QuerySnapshot> habitEventRef = collectionReference.get();
                habitEventRef.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Log.d("debugInfo", "retrieved the habit events document");

                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();

                        for (DocumentSnapshot doc : docs) {
                            int id = Objects.requireNonNull(doc.getLong("Id")).intValue();
                            Map<String, Object> data = doc.getData();
                            assert data != null;
                            String Url = (String) data.get("Url");
                            Date date = ((com.google.firebase.Timestamp) data.get("Date")).toDate();
                            String des = (String) data.get("Description");
                            GeoPoint loc = (GeoPoint) data.get("Location");
                            Boolean isDone = (Boolean) data.get("Done");
                            HabitEvent habitEvent = new HabitEvent();
                            habitEvent.setEventId(id);
                            habitEvent.setDescription(des);
                            habitEvent.setDate(date);
                            habitEvent.setImage(Url);
                            assert loc != null;
                            habitEvent.setLocation(loc.getLatitude(), loc.getLongitude());
                            habitEvent.setDone(isDone);

                            if (!habit.containsEventId(id)) {
                                habit.addEvent(habitEvent);
                            }
                            Log.d("iterChecker", "document #" + id);

                        }
                        Log.d("iterChecker", "COMPLETE");

                        callback.onComplete(true);

                    }

                });
            }
        });
    }

    public static void generateMissedEvents(Context context) {

        Member user = MainActivity.getUser();
        int size = user.getHabitListItems().size();
        Log.d("debugInfo", "generateMissedEvents was called.... " + size + " habits found..");
        for (Habit h : user.getHabitListItems()) {
            generateHabitMissedEvents(h);
        }
        ((LoginActivity) context).finishLogin();
    }

    public static void generateHabitMissedEvents(Habit habit) {
        Log.d("debugInfo", "checking habit: " + habit.getTitle() + " for missed events...");
        Member user = MainActivity.getUser();
        Calendar dateIterator = Calendar.getInstance();      //iterator used to generated missed events day by day
        Calendar prevMidnightCal = Calendar.getInstance();   //the calendar object for the user's "next midnight" after their last login
        Calendar habitCreationTime = Calendar.getInstance(); //the calendar object for the habit's creation time
        Date prevMidnightDate = user.getPrevNextMidnight();
        Date habitStartDate = habit.getStartDate();

        habitCreationTime.setTime(habitStartDate);
        prevMidnightCal.setTime(prevMidnightDate);

        //convert to millis to compare dates
        if (prevMidnightDate.compareTo(habitStartDate) < 0) {
            prevMidnightDate = habitStartDate;
        }

        dateIterator.setTime(prevMidnightDate);
        Calendar currentDate = TimeController.getCurrentTime();
        dateIterator.add(Calendar.MILLISECOND, -1); // set the date Iterator to 11:59:59.999 previous day

        // check first day separately
        // because it might still have been completed on that day
        if (TimeController.compareCalendarDays(dateIterator, currentDate) < 0) {
            int dayOfWeek = dateIterator.get(Calendar.DAY_OF_WEEK);
            if (habit.getSchedule().checkScheduleDay(dayOfWeek)) {
                if (!habit.completedOnDay(dateIterator)) {
                    Log.d("debugInfo", "missed event generated for " + habit.getTitle() + " on " + (dateIterator.getTime()).toString());
                    habit.addMissedEvent(dateIterator);
                } else {
                    Log.d("debugInfo", habit.getTitle() + " was completed on day " + dayOfWeek + "....");
                }


            }
        }

        dateIterator.add(Calendar.DATE, 1);
        // don't need to check if habit was done on rest of days,
        // because we know the user was never logged in for those days
        for (; dateIterator.getTimeInMillis() < currentDate.getTimeInMillis(); dateIterator.add(Calendar.DATE, 1)) {
            int dayOfWeek = dateIterator.get(Calendar.DAY_OF_WEEK);
            if (habit.getSchedule().checkScheduleDay(dayOfWeek)) {
                habit.addMissedEvent(dateIterator);
            }

        }

    }

    public static String getDocumentIdString(HabitEvent event) {
        @SuppressLint("DefaultLocale") String idString = String.format("%012d", event.getEventId());
        return idString;
    }

    public interface OnCompleteCallback {
        void onComplete(boolean suc);
    }
}
