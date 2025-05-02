package com.example.todo_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class ModifyTaskActivity extends AppCompatActivity {
    private EditText taskName, taskDescription;
    private TextView selectedDate;
    private RadioGroup priorityGroup;
    private Calendar calendar;
    private Task task;
    private int taskPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_task);

        // Initialize views
        taskName = findViewById(R.id.taskName);
        taskDescription = findViewById(R.id.taskDescription);
        selectedDate = findViewById(R.id.tvNewSelectedDate);
        priorityGroup = findViewById(R.id.rgPriority);
        calendar = Calendar.getInstance();

        // Get task from intent
        task = (Task) getIntent().getSerializableExtra("task");
        taskPosition = getIntent().getIntExtra("position", -1);

        // Populate fields with task data
        if (task != null) {
            taskName.setText(task.getName());
            taskDescription.setText(task.getDescription());
            selectedDate.setText(task.getDueDate());

            // Set the right priority radio button
            switch (task.getPriority()) {
                case "High":
                    priorityGroup.check(R.id.rbPriorityHigh);
                    break;
                case "Medium":
                    priorityGroup.check(R.id.rbPriorityMedium);
                    break;
                case "Low":
                    priorityGroup.check(R.id.rbPriorityLow);
                    break;
            }
        }

        // Date picker button
        Button btnChangeDate = findViewById(R.id.btnChangeDate);
        btnChangeDate.setOnClickListener(v -> showDatePicker());

        // Save changes button
        Button btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            String dateStr = (month + 1) + "/" + day + "/" + year;
            selectedDate.setText(dateStr);
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveChanges() {
        String name = taskName.getText().toString();
        String description = taskDescription.getText().toString();
        String date = selectedDate.getText().toString();

        String priority = "Medium";
        int selectedId = priorityGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.rbPriorityHigh) priority = "High";
        else if (selectedId == R.id.rbPriorityLow) priority = "Low";

        Task updatedTask = new Task(name, description, date, priority);
        updatedTask.setCompleted(task.isCompleted());

        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedTask", updatedTask);
        resultIntent.putExtra("position", taskPosition);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
