package com.example.todo_app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class ReminderDialogFragment extends DialogFragment {

    private OnReminderSetListener listener;
    private Task task;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button btnSetReminder;
    private Button btnCancel;
    private Calendar calendar;

    public interface OnReminderSetListener {
        void onReminderSet(String taskId, long reminderTimeInMillis);
    }

    public static ReminderDialogFragment newInstance(Task task) {
        ReminderDialogFragment fragment = new ReminderDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("task", task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnReminderSetListener) {
            listener = (OnReminderSetListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ReminderDialogFragment.OnReminderSetListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder_dialog, container, false);
        task = (Task) getArguments().getSerializable("task");
        datePicker = view.findViewById(R.id.datePicker);
        timePicker = view.findViewById(R.id.timePicker);
        btnSetReminder = view.findViewById(R.id.btnSetReminder);
        btnCancel = view.findViewById(R.id.btnCancel);
        calendar = Calendar.getInstance();

        // Initialize DatePicker with current or due date
        if (task.getReminderTimeInMillis() > 0) {
            calendar.setTimeInMillis(task.getReminderTimeInMillis());
        } else if (task.getDueDateAsDate() != null) {
            calendar.setTime(task.getDueDateAsDate());
        }
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

        btnSetReminder.setOnClickListener(v -> {
            int year = datePicker.getYear();
            int month = datePicker.getMonth();
            int dayOfMonth = datePicker.getDayOfMonth();
            int hourOfDay = timePicker.getCurrentHour();
            int minute = timePicker.getCurrentMinute();

            Calendar reminderCalendar = Calendar.getInstance();
            reminderCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
            reminderCalendar.set(Calendar.MILLISECOND, 0);

            if (listener != null) {
                listener.onReminderSet(task.getId(), reminderCalendar.getTimeInMillis());
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }
}