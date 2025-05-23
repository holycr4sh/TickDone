package com.example.todo_app;

import static com.example.todo_app.ReminderBroadcastReceiver.CHANNEL_ID;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener, ReminderDialogFragment.OnReminderSetListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> tasks;
    private List<Task> displayedTasks; // List to hold tasks that are currently displayed (filtered)
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "TodoPrefs";
    private static final String TASKS_KEY = "tasks";
    private static final String FILTER_KEY = "filter"; // Key to save the selected filter

    private Spinner filterSpinner;
    private String currentFilter = "All"; // Default filter
    private AlarmManager alarmManager; // Declaration of the alarmManager variable

    private ActivityResultLauncher<String> requestExactAlarmPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("MainActivity", "SCHEDULE_EXACT_ALARM permission granted");
                    // Proceed with scheduling the reminder
                    // You might want to reschedule any pending reminders here
                } else {
                    Log.w("MainActivity", "SCHEDULE_EXACT_ALARM permission not granted");
                    Toast.makeText(this, "Precise reminders might not work without this permission.", Toast.LENGTH_LONG).show();
                    // Optionally, explain why the permission is needed and how to enable it in settings
                }
            });

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // 'S' is for Android 12 (API 31)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SCHEDULE_EXACT_ALARM
            ) == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "SCHEDULE_EXACT_ALARM permission already granted");
                // Proceed with scheduling
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                // Explain why the permission is needed (optional UI)
                Log.w("MainActivity", "Showing SCHEDULE_EXACT_ALARM permission rationale");
                Toast.makeText(this, "This app needs precise alarm permission for reliable reminders.", Toast.LENGTH_LONG).show();
                requestExactAlarmPermissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM);
            } else {
                // Request the permission directly
                Log.d("MainActivity", "Requesting SCHEDULE_EXACT_ALARM permission");
                requestExactAlarmPermissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM);
            }
        } else {
            // On older versions, this permission is not needed
            Log.d("MainActivity", "SCHEDULE_EXACT_ALARM permission not needed on older versions");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE); // Initialize alarmManager
        createNotificationChannel(); // Call createNotificationChannel after super.onCreate()
        setContentView(R.layout.activity_main);

        checkNotificationPermission();
        checkExactAlarmPermission(); // Request SCHEDULE_EXACT_ALARM permission
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        tasks = loadTasks();
        displayedTasks = new ArrayList<>(tasks);

        recyclerView = findViewById(R.id.tasksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, displayedTasks, this);
        recyclerView.setAdapter(adapter);

        updateEmptyState();
        setupFilterSpinner();

        ExtendedFloatingActionButton fab = findViewById(R.id.fab); // Changed the type here
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    private void setupFilterSpinner() {
        filterSpinner = findViewById(R.id.filterSpinner);
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this, R.array.filter_options, R.layout.spinner_item_layout); // Use custom layout
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);

        // Load the previously selected filter
        currentFilter = sharedPreferences.getString(FILTER_KEY, "All");
        filterSpinner.setSelection(getFilterPosition(currentFilter));

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = parent.getItemAtPosition(position).toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(FILTER_KEY, currentFilter);
                editor.apply();
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private int getFilterPosition(String filter) {
        String[] filterOptions = getResources().getStringArray(R.array.filter_options);
        for (int i = 0; i < filterOptions.length; i++) {
            if (filterOptions[i].equals(filter)) {
                return i;
            }
        }
        return 0; // Default to "All"
    }

    private void applyFilter() {
        displayedTasks.clear();
        displayedTasks.addAll(tasks); // Reset to all tasks before filtering

        switch (currentFilter) {
            case "Priority":
                filterByPriority();
                break;
            case "Due Date":
                filterByDueDate();
                break;
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void filterByPriority() {
        Collections.sort(displayedTasks, (task1, task2) -> {
            int priorityOrder = getPriorityValue(task1.getPriority()) - getPriorityValue(task2.getPriority());
            if (priorityOrder == 0) {
                return task1.getDueDateAsDate().compareTo(task2.getDueDateAsDate());
            }
            return priorityOrder;
        });
    }

    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High":
                return 1;
            case "Medium":
                return 2;
            case "Low":
                return 3;
            default:
                return 4; // For unknown priorities, sort them last
        }
    }

    private void filterByDueDate() {
        Collections.sort(displayedTasks, Comparator.comparing(Task::getDueDateAsDate));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == 1) {
            Task newTask = (Task) data.getSerializableExtra("newTask");
            if (newTask != null) {
                tasks.add(newTask);
                applyFilter(); // Re-apply filter after adding a task
                saveTasks();
                updateEmptyState();
            }
        } else if (requestCode == 2) {
            Task updatedTask = (Task) data.getSerializableExtra("updatedTask");
            int position = data.getIntExtra("position", -1);
            if (updatedTask != null && position != -1) {
                tasks.set(position, updatedTask);
                applyFilter(); // Re-apply filter after editing a task
                saveTasks();
                adapter.notifyItemChanged(position);
            }
        }
    }

    private void updateEmptyState() {
        TextView emptyState = findViewById(R.id.emptyState);
        if (displayedTasks.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
        }
    }

    private void saveTasks() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(tasks);
        editor.putString(TASKS_KEY, json);
        editor.apply();
    }

    private List<Task> loadTasks() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(TASKS_KEY, null);
        Type type = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> loadedTasks = gson.fromJson(json, type);
        return loadedTasks != null ? loadedTasks : new ArrayList<>();
    }

    @Override
    public void onTaskCompleted(int position, boolean isCompleted) {
        Task task = displayedTasks.get(position); // Use displayedTasks
        task.setCompleted(isCompleted);
        saveTasks();
        applyFilter(); // Re-apply filter to update the list
    }

    @Override
    public void onTaskDeleted(int position) {
        Task task = displayedTasks.get(position); // Use displayedTasks
        tasks.remove(task);
        displayedTasks.remove(position);
        saveTasks();
        adapter.notifyItemRemoved(position);
        updateEmptyState();
        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskEdit(int position) {
        try {
            if (position < 0 || position >= displayedTasks.size()) { // Use displayedTasks
                Log.e("MainActivity", "Invalid position: " + position);
                Toast.makeText(this, "Invalid task position", Toast.LENGTH_SHORT).show();
                return;
            }

            Task taskToEdit = displayedTasks.get(position); // Use displayedTasks
            Log.d("MainActivity", "Original task: " + taskToEdit.getName() + ", ID: " + taskToEdit.getId());

            Intent intent = new Intent(this, ModifyTaskActivity.class);
            intent.putExtra("task", new Task(taskToEdit));
            intent.putExtra("position", tasks.indexOf(taskToEdit)); // Send original position
            startActivityForResult(intent, 2);
        } catch (Exception e) {
            Log.e("MainActivity", "Edit Error: " + e.getMessage(), e);
            Toast.makeText(this, "Error editing task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReminderSet(String taskId, long reminderTimeInMillis) {
        for (int i = 0; i < displayedTasks.size(); i++) {
            if (displayedTasks.get(i).getId().equals(taskId)) {
                Task task = displayedTasks.get(i);
                task.setHasReminder(true);
                task.setReminderTimeInMillis(reminderTimeInMillis);
                adapter.notifyItemChanged(i);
                saveTasks();
                Log.d("MainActivity", "onReminderSet - reminderTimeInMillis: " + reminderTimeInMillis); // Added log
                scheduleReminder(task);
                break;
            }
        }
    }

    @Override
    public void onTaskReminder(int position) {
        if (position >= 0 && position < displayedTasks.size()) {
            Task task = displayedTasks.get(position);
            ReminderDialogFragment.newInstance(task)
                    .show(getSupportFragmentManager(), "ReminderDialog");
        }
    }

    private void scheduleReminder(Task task) {
        Log.d("MainActivity", "scheduleReminder called for: " + task.getName());
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy hh:mm a", Locale.US);
        try {
            if (task.hasReminder() && task.getReminderTimeInMillis() > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(task.getReminderTimeInMillis());

                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    Log.w("MainActivity", "Reminder time is in the past, scheduling for the future.");
                    calendar.add(Calendar.MINUTE, 1);
                    task.setReminderTimeInMillis(calendar.getTimeInMillis());
                    saveTasks();
                }

                Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
                intent.putExtra(ReminderBroadcastReceiver.TASK_NAME_EXTRA, task.getName());
                intent.putExtra(ReminderBroadcastReceiver.TASK_ID_EXTRA, task.getId());
                //intent.addFlags(Intent.FLAG_WAKE_FROM_IDLE); // Removed FLAG_WAKE_FROM_IDLE

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, task.getId().hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), null);
                        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                        Log.d("MainActivity", "Reminder scheduled (using setAlarmClock) for: " + task.getName() + " on " + sdf.format(calendar.getTime()));
                    } else {
                        Log.w("MainActivity", "Cannot schedule exact alarm: Permission not granted or system restriction.");
                        Toast.makeText(this, "Reminder might not be exact.", Toast.LENGTH_SHORT).show();
                        // As a fallback, you might consider using setAlarm() which is less precise
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        Log.d("MainActivity", "Reminder scheduled (using set) for: " + task.getName() + " on " + sdf.format(calendar.getTime()));
                    }
                } else {
                    AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), null);
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                    Log.d("MainActivity", "Reminder scheduled (using setAlarmClock) for: " + task.getName() + " on " + sdf.format(calendar.getTime()));
                }

            } else if (task.hasReminder() && task.getReminderTimeInMillis() == 0) {
                Log.w("MainActivity", "Reminder time not set for: " + task.getName());
                Toast.makeText(this, "Please set a time for the reminder", Toast.LENGTH_SHORT).show();
                task.setHasReminder(false);
                adapter.notifyItemChanged(displayedTasks.indexOf(task));
                saveTasks();
            } else if (!task.hasReminder()) {
                cancelReminder(task);
            }

        } catch (Exception e) {
            Log.e("MainActivity", "Error scheduling reminder: " + e.getMessage());
            Toast.makeText(this, "Error scheduling reminder", Toast.LENGTH_SHORT).show();
            task.setHasReminder(false);
            adapter.notifyItemChanged(displayedTasks.indexOf(task));
            saveTasks();
        }
    }
    private void cancelReminder(Task task) {
        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, task.getId().hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        Log.d("MainActivity", "Reminder cancelled for: " + task.getName());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminders";
            String description = "Reminders for your todo tasks";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("MainActivity", "Notification permission granted");
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                    // You can proceed with setting up reminders here if needed
                } else {
                    Log.w("MainActivity", "Notification permission not granted");
                    Toast.makeText(this, "Notification permission is required for reminders", Toast.LENGTH_LONG).show();
                    // Optionally, explain why the permission is needed and how to enable it
                }
            });

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Notification permission already granted");
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Explain to the user why the permission is needed (optional UI)
                Log.w("MainActivity", "Showing notification permission rationale");
                Toast.makeText(this, "This app needs permission to send notifications for reminders.", Toast.LENGTH_LONG).show();
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Request the permission directly
                Log.d("MainActivity", "Requesting notification permission");
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // On older versions, permission is granted at install time
            Log.d("MainActivity", "Notification permission granted by default on older versions");
        }
    }
}