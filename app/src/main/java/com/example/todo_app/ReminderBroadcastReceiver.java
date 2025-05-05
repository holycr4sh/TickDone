package com.example.todo_app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";
    public static final String TASK_NAME_EXTRA = "task_name";
    public static final String TASK_ID_EXTRA = "task_id";
    protected static final String CHANNEL_ID = "task_reminders"; // Defined in your Application class

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra(TASK_NAME_EXTRA);
        String taskId = intent.getStringExtra(TASK_ID_EXTRA);

        Log.d(TAG, "Reminder received for task: " + taskName + " (ID: " + taskId + ")");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_reminder) // Use your reminder icon
                .setContentTitle("Task Reminder!")
                .setContentText("Don't forget to: " + taskName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check for POST_NOTIFICATIONS permission before showing the notification
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(taskId.hashCode(), builder.build()); // Show notification if permission is granted
            } else {
                Log.w(TAG, "Notification permission not granted, cannot show reminder for task: " + taskName);
                // You might want to handle this case (e.g., log it, or potentially reschedule the reminder for later)
            }
        } else {
            // On older versions, permission is granted at install time
            notificationManager.notify(taskId.hashCode(), builder.build());
        }
    }
}