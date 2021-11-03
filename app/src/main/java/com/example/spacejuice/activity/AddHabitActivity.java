package com.example.spacejuice.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spacejuice.Habit;
import com.example.spacejuice.MainActivity;
import com.example.spacejuice.R;
import com.example.spacejuice.Schedule;
import com.example.spacejuice.controller.HabitController;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddHabitActivity extends AppCompatActivity implements View.OnClickListener {
    /*
    This Activity is used to add and enter the details of a new habit
     */
    private EditText NameEdit;
    private String name;
    private EditText ReasonEdit;
    private String reason;
    private CheckBox Monday;
    private CheckBox Tuesday;
    private CheckBox Wednesday;
    private CheckBox Thursday;
    private CheckBox Friday;
    private CheckBox Saturday;
    private CheckBox Sunday;
    private TextView SelectedDate;
    private Date date;
    private Schedule schedule;
    private Habit habitReturn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit_add_edit);

        ImageButton backB = findViewById(R.id.backButtonHAE);
        Button confirmB = findViewById(R.id.confirmButtonHAE);

        SelectedDate = findViewById(R.id.textView5HAE);
        date = new Date();
        Format dateToString = new SimpleDateFormat("yyyy-MM-dd");
        SelectedDate.setText(dateToString.format(date));
        Button dateButton = findViewById(R.id.DateButtonHAE);
        dateButton.setOnClickListener(this);
        confirmB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NameEdit = findViewById(R.id.HabitNameHAE);
                name = NameEdit.getText().toString();  //get the name
                ReasonEdit = findViewById(R.id.HabitReasonHAE);
                reason = ReasonEdit.getText().toString(); //Get the Description
                Monday = findViewById(R.id.Monday);
                Tuesday = findViewById(R.id.Tuesday);
                Wednesday = findViewById(R.id.Wednesday);
                Thursday = findViewById(R.id.Thursday);
                Friday = findViewById(R.id.Friday);
                Saturday = findViewById(R.id.Saturday);
                Sunday = findViewById(R.id.Sunday);
                schedule = new Schedule(Sunday.isChecked(), Monday.isChecked(), Tuesday.isChecked(),
                        Wednesday.isChecked(), Thursday.isChecked(), Friday.isChecked(), Saturday.isChecked());
                habitReturn = new Habit(name, reason, -1);
                habitReturn.setStartDate(date);
                habitReturn.setSchedule(schedule);
                HabitController.addHabit(habitReturn);
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        backB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);
                finish();
            }
        });


    }

    @Override
    public void onClick(View view) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        date = new Date(year - 1900, monthOfYear, dayOfMonth);
                        SelectedDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
}
