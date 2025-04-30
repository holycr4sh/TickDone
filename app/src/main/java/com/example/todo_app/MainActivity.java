package com.example.todo_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
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
        adapter = new TaskAdapter(tasks);
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
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Task newTask = (Task) data.getSerializableExtra("newTask");
            tasks.add(newTask);
            adapter.notifyDataSetChanged(); // Changed from notifyItemInserted
            saveTasks();
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
}