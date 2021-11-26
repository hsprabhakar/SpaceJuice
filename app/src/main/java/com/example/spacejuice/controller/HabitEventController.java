package com.example.spacejuice.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.spacejuice.Habit;
import com.example.spacejuice.HabitEvent;
import com.example.spacejuice.MainActivity;
import com.example.spacejuice.Member;
import com.example.spacejuice.activity.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HabitEventController {
    public static void editHabitEvent(Habit habit, HabitEvent habitEvent){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int uid = habit.getUid();
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Members")
                .document(MainActivity.getUser().getMemberName())
                .collection("Habits").document(habit.getTitle()).collection("Events")
                .document(String.valueOf(habitEvent.getEventId()));
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    // if this name exist
                    if (document.exists()) {
                        documentReference.update("url",habitEvent.getImage());
                        documentReference.update("Description", habitEvent.getDescription());
                        }
                    }
                }
        });

    }

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
                String id = String.valueOf(habitEvent.getEventId());
                eventDocRef = habitRef.collection("Events").document(id);
                eventDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("Id", habitEvent.getEventId());
                            data.put("Description", habitEvent.getDescription());
                            data.put("Date", habitEvent.getDate());
                            data.put("Url", habitEvent.getImage());
                            data.put("Location", new GeoPoint(habitEvent.getLatitude(), habitEvent.getLongitude()));
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
    public static void loadHabitEventsFromFirebase(Habit habit, String name, final HabitController.OnHabitEventsLoaded callback) {
        Log.d("debugInfoLogin", "HabitEventController.loadHabitEventsFromFirebase() - initialized for " + habit.getTitle());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int uid = habit.getUid();
        Query habitEventQuery =
                db.collection("Members").document(name).collection("Habits")
                        .whereEqualTo("ID", uid);
        Task<QuerySnapshot> habitEventQueryTask = habitEventQuery.get();
        habitEventQueryTask.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {    //get the document with the proper habit uID
                Log.d("debugInfoLogin", "HabitEventController.loadHabitEventsFromFirebase() - habitEventQueryTask onSuccess for " + habit.getTitle());
                DocumentReference habitRef = Objects.requireNonNull(habitEventQueryTask
                        .getResult()).getDocuments().get(0).getReference();
                CollectionReference collectionReference = habitRef.collection("Events");
                Task<QuerySnapshot> habitEventRef = collectionReference.get();
                habitEventRef.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Log.d("debugInfoLogin", "HabitEventController.loadHabitEventsFromFirebase() - HabitEventRef onSuccss for " + habit.getTitle());

                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        int docs_loaded = 0;
                        int docs_size = docs.size();

                        if (docs_size == 0) {
                            Log.d("debugInfoLogin", "HabitEventController.loadHabitEventsFromFirebase() - docs size = 0 for " + habit.getTitle() + "... sending callback");
                            callback.onHabitEventsComplete(true);
                        } else {

                            for (DocumentSnapshot doc : docs) {
                                docs_loaded += 1;
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
                                if (name == MainActivity.getUser().getMemberName()) {
                                    habitEvent.setLocation(loc.getLatitude(), loc.getLongitude());
                                }
                                habitEvent.setDone(isDone);

                                if (!habit.containsEventId(id)) {
                                    habit.addEvent(habitEvent);
                                }
                                Log.d("debugInfoLogin", "HabitEventController.loadHabitEventsFromFirebase() - loading doc #" + docs_loaded + " for " + habit.getTitle());
                                if (docs_loaded == docs_size) {
                                    Log.d("debugInfoLogin", "HabitEventController.loadHabitEventsFromFirebase() - last doc for " + habit.getTitle() + "... sending callback");
                                    Log.d("iterChecker", "COMPLETE - LAST EVENT LOADEO FOR " + habit.getTitle());
                                    callback.onHabitEventsComplete(true);
                                }
                            }


                        }


                    }

                });
            }
        });
    }

    public static void generateMissedEvents(Context context) {

        Member user = MainActivity.getUser();
        int size = user.getHabitListItems().size();
        Log.d("debugInfoLogin", "HabitEventController.generateMissedEvents - initialized..");
        for (Habit h : user.getHabitListItems()) {
            generateHabitMissedEvents(h);
        }
        ((LoginActivity) context).finishLogin();
    }

    public static void generateHabitMissedEvents(Habit habit) {
        Log.d("debugInfoLogin", "HabitEventController.generateHabitMissedEvents() - intialized for " + habit.getTitle());
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
                    Log.d("debugInfoLogin", "HabitEventController.generateHabitMissedEvents() - ** MISSED EVENT GENERATED ** for " + habit.getTitle() + " on " + (dateIterator.getTime()).toString());
                    habit.addMissedEvent(dateIterator);
                } else {
                    Log.d("debugInfoLogin", "HabitEventController.generateHabitMissedEvents() - " + habit.getTitle() + " was completed on day of week #" + (dateIterator.get(Calendar.DAY_OF_WEEK)));
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
/*
    public interface OnCompleteCallback {
        void onComplete(boolean suc);
    }

 */
}
