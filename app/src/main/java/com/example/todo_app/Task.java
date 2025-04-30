package com.example.todo_app;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task implements Serializable {
    private String name;
    private String description;
    private String dueDate;
    private String priority;

    private boolean completed;

    public Task(String name, String description, String dueDate, String priority) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
    }

    // Getters & Setters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDueDate() { return dueDate; }
    public String getPriority() { return priority; }

    public boolean isCompleted() {
        return completed;
    }
    public String toStorageString() {
        return name + "|||" + description + "|||" + dueDate + "|||" + priority;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public static Task fromStorageString(String str) {
        String[] parts = str.split("\\|\\|\\|");
        return new Task(parts[0], parts[1], parts[2], parts[3]);
    }
}