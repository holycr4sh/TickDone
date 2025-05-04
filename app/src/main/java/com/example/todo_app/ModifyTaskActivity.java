package com.example.todo_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Locale;

public class ModifyTaskActivity extends AppCompatActivity {
    private static final String TAG = "ModifyTaskActivity";
    private EditText taskName, taskDescription;
    private TextView selectedDate;
    private RadioGroup priorityGroup;
    private Calendar calendar;
    private Task task;
    private int taskPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.modify_task);
            Log.d(TAG, "ModifyTaskActivity started");

            // Initialize views
            taskName = findViewById(R.id.taskName);
            taskDescription = findViewById(R.id.taskDescription);
            selectedDate = findViewById(R.id.tvNewSelectedDate);
            priorityGroup = findViewById(R.id.rgPriority);

            // Verify views were found
            if (taskName == null) Log.e(TAG, "taskName view is null");
            if (taskDescription == null) Log.e(TAG, "taskDescription view is null");
            if (selectedDate == null) Log.e(TAG, "selectedDate view is null");
            if (priorityGroup == null) Log.e(TAG, "priorityGroup view is null");

            calendar = Calendar.getInstance();

            // Check if we have intent data
            if (getIntent() == null) {
                Log.e(TAG, "Intent is null");
                throw new RuntimeException("No intent received");
            }

            if (getIntent().getExtras() == null) {
                Log.e(TAG, "Intent extras are null");
                throw new RuntimeException("No intent extras received");
            }

            // Get task from the intent
            task = (Task) getIntent().getSerializableExtra("task");
            taskPosition = getIntent().getIntExtra("position", -1);

            // Log what we received
            if (task == null) {
                Log.e(TAG, "Task is null");
                Toast.makeText(this, "Task data is missing", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            Log.d(TAG, "Received task: " + task.getName() + ", ID: " + task.getId() + ", Position: " + taskPosition);

            // Set UI values from task
            taskName.setText(task.getName());
            taskDescription.setText(task.getDescription());
            selectedDate.setText(task.getDueDate());
            setPriorityRadioButton(task.getPriority());

            // Set up buttons
            Button btnChangeDate = findViewById(R.id.btnChangeDate);
            Button btnSaveChanges = findViewById(R.id.btnSaveChanges);

            if (btnChangeDate == null) Log.e(TAG, "btnChangeDate is null");
            if (btnSaveChanges == null) Log.e(TAG, "btnSaveChanges is null");

            btnChangeDate.setOnClickListener(v -> showDatePicker());
            btnSaveChanges.setOnClickListener(v -> saveChanges());

        } catch (Exception e) {
            Log.e(TAG, "Initialization failed: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading task editor: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setPriorityRadioButton(String priority) {
        switch (priority) {
            case "High":
                priorityGroup.check(R.id.rbHigh);
                break;
            case "Medium":
                priorityGroup.check(R.id.rbMedium);
                break;
            case "Low":
                priorityGroup.check(R.id.rbLow);
                break;
            default:
                priorityGroup.check(R.id.rbMedium);
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            selectedDate.setText(String.format(Locale.getDefault(), "%d/%d/%d", month + 1, day, year));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveChanges() {
        try {
            String name = taskName.getText().toString().trim();
            String description = taskDescription.getText().toString().trim();
            String date = selectedDate.getText().toString();

            if (name.isEmpty()) {
                taskName.setError("Task name cannot be empty");
                return;
            }

            task.setName(name);
            task.setDescription(description);
            task.setDueDate(date);
            task.setPriority(getSelectedPriority());

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedTask", task);
            resultIntent.putExtra("position", taskPosition);
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error saving changes", e);
            Toast.makeText(this, "Error saving task", Toast.LENGTH_LONG).show();
        }
    }

    private String getSelectedPriority() {
        int selectedId = priorityGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.rbHigh) return "High";
        if (selectedId == R.id.rbLow) return "Low";
        return "Medium";
    }
}