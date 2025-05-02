package com.example.todo_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> tasks;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "TodoPrefs";
    private static final String TASKS_KEY = "tasks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load tasks from storage
        tasks = loadTasks();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.tasksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, tasks, this);
        recyclerView.setAdapter(adapter);

        // Floating Action Button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == 1) { // New task result
            Task newTask = (Task) data.getSerializableExtra("newTask");
            if (newTask != null) {
                tasks.add(newTask);
                adapter.notifyItemInserted(tasks.size() - 1);
                saveTasks();
            }
        } else if (requestCode == 2) { // Modified task result
            Task updatedTask = (Task) data.getSerializableExtra("updatedTask");
            int position = data.getIntExtra("position", -1);
            if (updatedTask != null && position != -1) {
                tasks.set(position, updatedTask);
                adapter.notifyItemChanged(position);
                saveTasks();
            }
        }
    }

    private List<Task> loadTasks() {
        String json = sharedPreferences.getString(TASKS_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Task>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    private void saveTasks() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(tasks);
        editor.putString(TASKS_KEY, json);
        editor.apply();
    }

    // TaskAdapter listener implementations
    @Override
    public void onTaskCompleted(int position, boolean isCompleted) {
        tasks.get(position).setCompleted(isCompleted);
        adapter.notifyItemChanged(position);
        saveTasks();
    }

    @Override
    public void onTaskDeleted(int position) {
        if (position >= 0 && position < tasks.size()) {
            tasks.remove(position);
            adapter.notifyItemRemoved(position);
            saveTasks();
            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskEdit(int position) {
        if (position >= 0 && position < tasks.size()) {
            Intent intent = new Intent(this, ModifyTaskActivity.class);
            intent.putExtra("task", tasks.get(position));
            intent.putExtra("position", position);
            startActivityForResult(intent, 2);
        }
    }

    @Override
    public void onTaskReminder(int position) {
        if (position >= 0 && position < tasks.size()) {
            Task task = tasks.get(position);
            task.setHasReminder(!task.hasReminder());
            adapter.notifyItemChanged(position);
            saveTasks();
            String message = task.hasReminder() ? "Reminder set" : "Reminder removed";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}