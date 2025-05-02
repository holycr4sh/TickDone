package com.example.todo_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Task> tasks;
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
        this.tasks = tasks;
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

        // Set task name
        holder.checkBox.setText(task.getName());
        holder.checkBox.setChecked(task.isCompleted());

        // Set description and due date
        holder.taskDescription.setText(task.getDescription());
        holder.taskDueDate.setText("Due: " + task.getDueDate());

        // Set priority color
        int priorityColor;
        switch (task.getPriority()) {
            case "High":
                priorityColor = Color.parseColor("#FFE57373"); // Light red
                break;
            case "Medium":
                priorityColor = Color.parseColor("#FFFFE0B2"); // Light orange
                break;
            case "Low":
                priorityColor = Color.parseColor("#FFC5E1A5"); // Light green
                break;
            default:
                priorityColor = Color.WHITE;
                break;
        }
        holder.cardView.setCardBackgroundColor(priorityColor);

        // Set completed appearance
        if (task.isCompleted()) {
            holder.checkBox.setPaintFlags(holder.checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskDueDate.setPaintFlags(holder.taskDueDate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.checkBox.setPaintFlags(holder.checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.taskDueDate.setPaintFlags(holder.taskDueDate.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Set overdue appearance
        if (task.isOverdue() && !task.isCompleted()) {
            holder.taskDueDate.setTextColor(Color.RED);
        } else {
            holder.taskDueDate.setTextColor(Color.GRAY);
        }

        // Set reminder icon
        if (task.hasReminder()) {
            holder.btnSetReminder.setImageResource(R.drawable.ic_reminder_set);
        } else {
            holder.btnSetReminder.setImageResource(R.drawable.ic_reminder);
        }

        // Set listeners
        holder.checkBox.setOnClickListener(v -> {
            boolean isChecked = holder.checkBox.isChecked();
            task.setCompleted(isChecked);
            notifyItemChanged(position);
            listener.onTaskCompleted(position, isChecked);
        });

        holder.btnDelete.setOnClickListener(v -> {
            listener.onTaskDeleted(position);
        });

        holder.btnEdit.setOnClickListener(v -> {
            listener.onTaskEdit(position);
        });

        holder.btnSetReminder.setOnClickListener(v -> {
            listener.onTaskReminder(position);
        });
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

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
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