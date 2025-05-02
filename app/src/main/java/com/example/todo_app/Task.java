package com.example.todo_app;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Task implements Serializable {
    private String id;
    private String name;
    private String description;
    private String dueDate;
    private String priority;
    private boolean completed;
    private boolean hasReminder;
    private long reminderTimeMillis;

    public Task(String name, String description, String dueDate, String priority) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = false;
        this.hasReminder = false;
        this.reminderTimeMillis = 0;
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean hasReminder() { return hasReminder; }
    public void setHasReminder(boolean hasReminder) { this.hasReminder = hasReminder; }
    public long getReminderTimeMillis() { return reminderTimeMillis; }
    public void setReminderTimeMillis(long reminderTimeMillis) { this.reminderTimeMillis = reminderTimeMillis; }

    public Date getDueDateAsDate() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("M/d/yyyy", Locale.US);
            return format.parse(dueDate);
        } catch (ParseException | NullPointerException e) {
            return null;
        }
    }

    public boolean isOverdue() {
        Date dueDate = getDueDateAsDate();
        if (dueDate == null) return false;
        return !completed && dueDate.before(new Date());
    }
}