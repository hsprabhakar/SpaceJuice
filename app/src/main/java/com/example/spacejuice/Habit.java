package com.example.spacejuice;

import android.util.Log;

import java.util.Date;

public class Habit {
    private static int TITLE_LENGTH = 20;
    private static int REASON_LENGTH = 30;
    private Indicator indicator;
    private String title;
    private String reason;
    private Date startDate;
    //Schedule schedule;

    public Habit(String title, String reason) {
        setTitle(title);
        setReason(reason);
        setStartDate(new Date());
        setIndicator(Indicator.EMPTY);
        newSchedule();
    }

    public Habit(String title, String reason, Indicator indicator) {
        setTitle(title);
        setReason(reason);
        setStartDate(new Date());
        setIndicator(indicator);
    }

    public enum Indicator {
        // allows the use of EMPTY, BRONZE, SILVER, GOLD to reference the indicator image
        // usage Indicator.GOLD.image will retrieve the image ID for the gold indicator
        // Indicator.type will return an integer representing its indicator type

        EMPTY(0,"empty", R.drawable.indicator_empty),
        BRONZE(1,"bronze", R.drawable.indicator_bronze),
        SILVER(2,"silver", R.drawable.indicator_silver),
        GOLD(3,"gold", R.drawable.indicator_gold);

        public String string;
        public int image;
        public int type;

        private Indicator(int type, String string, int imageInt) {
            this.string = string;
            this.image = imageInt;
            this.type = type;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    private void generateHabitID() {
        //todo: generate an ID unique to this habit (unique relative to this user's habits)
        //this.id = id;
    }

    public void setTitle(String s) {
        int l = s.length();
        if (l > TITLE_LENGTH) { l = TITLE_LENGTH; }
        this.title = s.substring(0,l);
        Log.d("debugInfo", "title set to " + this.title);
    }

    public void setReason(String s) {
        int l = s.length();
        if (l > REASON_LENGTH) { l = REASON_LENGTH; }
        this.reason = s.substring(0,l);
        Log.d("debugInfo", "reason set to " + this.reason);
    }

    public void setIndicator(Indicator indicator) {
        this.indicator = indicator;
    }

    public void setStartDate(Date d) {
        this.startDate = d;
    }

    public void newSchedule() {
        //todo: create Schedule Type
        //this.schedule = new Schedule();
    }

    public String getTitle() {
        return this.title;
    }

    public String getReason() {
        return this.reason;
    }

    public Indicator getIndicator() { return this.indicator; }

    public Date getStartDate() {
        return this.startDate;
    }

    @Override
    public String toString() {
        return this.title;
    }

    //public Schedule getSchedule() {
    //    return this.schedule;
    //}
}
