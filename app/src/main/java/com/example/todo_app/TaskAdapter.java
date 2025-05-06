package com.example.todo_app;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Task> tasks; // Changed to the filtered list
    private final Context context;
    private final OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onTaskCompleted(int position, boolean isCompleted);
        void onTaskDeleted(int position);
        void onTaskEdit(int position);
        void onTaskReminder(int position);
    }

    public TaskAdapter(Context context, List<Task> tasks, OnTaskActionListener listener) {
        this.context = context;
        this.tasks = tasks; // Now receives the filtered list
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        View holderView = holder.itemView;

        holder.btnSetReminder.setOnClickListener(v -> {
            if (!task.hasReminder()) {
                ReminderDialogFragment.newInstance(task)
                        .show(((MainActivity) context).getSupportFragmentManager(), "ReminderDialog");
            } else {
                // Option 1: Show dialog to modify or remove
                ReminderDialogFragment.newInstance(task)
                        .show(((MainActivity) context).getSupportFragmentManager(), "ReminderDialog");
                // Option 2: Directly remove (with optional confirmation)
                // ((MainActivity) context).onTaskReminder(position);
            }
        });

        holder.checkBox.setText(task.getName());
        holder.checkBox.setChecked(task.isCompleted());
        holder.taskDescription.setText(task.getDescription());
        holder.taskDueDate.setText("Due: " + task.getDueDate());

        switch (task.getPriority()) {
            case "High":
                holder.priorityColorIndicator.setBackgroundResource(R.drawable.priority_circle_high);
                break;
            case "Medium":
                holder.priorityColorIndicator.setBackgroundResource(R.drawable.priority_circle_medium);
                break;
            case "Low":
                holder.priorityColorIndicator.setBackgroundResource(R.drawable.priority_circle_low);
                break;
            default:
                holder.priorityColorIndicator.setBackgroundResource(android.R.color.transparent); // Or a default color
                break;
        }

// Reset text color (since the card background is now white)
        holder.checkBox.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        holder.taskDescription.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        holder.taskDueDate.setTextColor(ContextCompat.getColor(context, android.R.color.black));

// Reset icon colors to default (black or gray)
        int defaultIconColor = ContextCompat.getColor(context, R.color.black); // Or your preferred default
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(defaultIconColor, PorterDuff.Mode.SRC_IN);
        holder.btnDelete.setColorFilter(colorFilter);
        holder.btnEdit.setColorFilter(colorFilter);
        holder.btnSetReminder.setColorFilter(colorFilter);
        holder.btnSetReminder.setImageResource(task.hasReminder() ? R.drawable.ic_reminder_set : R.drawable.ic_reminder);
        holder.btnSetReminder.setColorFilter(colorFilter); // Re-apply after setting image

        if (task.isCompleted()) {
            holder.checkBox.setPaintFlags(holder.checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.checkBox.setPaintFlags(holder.checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.btnSetReminder.setImageResource(task.hasReminder() ? R.drawable.ic_reminder_set : R.drawable.ic_reminder);

        holder.checkBox.setOnClickListener(v -> {
            boolean isChecked = holder.checkBox.isChecked();
            task.setCompleted(isChecked);
            notifyItemChanged(position);
            listener.onTaskCompleted(position, isChecked);
        });

        holder.btnDelete.setOnClickListener(v -> listener.onTaskDeleted(position));
        holder.btnEdit.setOnClickListener(v -> listener.onTaskEdit(position));
        holder.btnSetReminder.setOnClickListener(v -> listener.onTaskReminder(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        CheckBox checkBox;
        TextView taskDescription;
        TextView taskDueDate;
        ImageButton btnDelete;
        ImageButton btnEdit;
        ImageButton btnSetReminder;
        View priorityColorIndicator;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            priorityColorIndicator = itemView.findViewById(R.id.priorityColorIndicator);
            cardView = (CardView) itemView;
            checkBox = itemView.findViewById(R.id.todoCheckBox);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskDueDate = itemView.findViewById(R.id.taskDueDate);
            btnDelete = itemView.findViewById(R.id.btnDeleteTask);
            btnEdit = itemView.findViewById(R.id.btnEditTask);
            btnSetReminder = itemView.findViewById(R.id.btnSetReminder);
        }
    }
}