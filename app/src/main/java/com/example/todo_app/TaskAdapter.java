package com.example.todo_app;

import android.content.Context;
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

        holder.checkBox.setText(task.getName());
        holder.checkBox.setChecked(task.isCompleted());
        holder.taskDescription.setText(task.getDescription());
        holder.taskDueDate.setText("Due: " + task.getDueDate());

        int priorityColor;
        switch (task.getPriority()) {
            case "High":
                priorityColor = ContextCompat.getColor(context, R.color.priority_high);
                break;
            case "Medium":
                priorityColor = ContextCompat.getColor(context, R.color.priority_medium);
                break;
            case "Low":
                priorityColor = ContextCompat.getColor(context, R.color.priority_low);
                break;
            default:
                priorityColor = ContextCompat.getColor(context, R.color.white);
        }
        holder.cardView.setCardBackgroundColor(priorityColor);

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