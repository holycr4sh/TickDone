package com.example.todo_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Locale;

public class NewTaskActivity extends AppCompatActivity {
    private EditText taskName, taskDescription;
    private TextView selectedDate;
    private RadioGroup priorityGroup;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        taskName = findViewById(R.id.taskName);
        taskDescription = findViewById(R.id.taskDescription);
        selectedDate = findViewById(R.id.tvSelectedDate);
        priorityGroup = findViewById(R.id.rgPriority);
        calendar = Calendar.getInstance();

        Button btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectDate.setOnClickListener(v -> showDatePicker());

        Button btnSaveTask = findViewById(R.id.btnSaveTask);
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            String dateStr = String.format(Locale.getDefault(), "%d/%d/%d", month + 1, day, year);
            selectedDate.setText(dateStr);
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTask() {
        String name = taskName.getText().toString().trim();
        String description = taskDescription.getText().toString().trim();
        String date = selectedDate.getText().toString();

        if (name.isEmpty()) {
            taskName.setError("Task name cannot be empty");
            return;
        }

        if (date.equals("No selected date")) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
            return;
        }

        String priority = "Medium";
        int selectedId = priorityGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.rbHigh) priority = "High";
        else if (selectedId == R.id.rbLow) priority = "Low";

        Task newTask = new Task(name, description, date, priority);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("newTask", newTask);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}