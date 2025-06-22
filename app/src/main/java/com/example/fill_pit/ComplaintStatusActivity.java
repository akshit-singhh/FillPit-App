package com.example.fill_pit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ComplaintStatusActivity extends AppCompatActivity {

    private LinearLayout statusContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_status);

        statusContainer = findViewById(R.id.statusContainer);

        List<ComplaintStatus> complaintStatuses = getComplaintStatusData();

        for (int i = 0; i < complaintStatuses.size(); i++) {
            addStatusView(complaintStatuses.get(i), i, complaintStatuses.size());
        }
    }

    private List<ComplaintStatus> getComplaintStatusData() {
        List<ComplaintStatus> statuses = new ArrayList<>();
        statuses.add(new ComplaintStatus("Complaint Received", "2023-10-26 10:00", true));
        statuses.add(new ComplaintStatus("Under Review", "2023-10-27 12:00", true));
        statuses.add(new ComplaintStatus("Assigned to Technician", "2023-10-28 14:00", true));
        statuses.add(new ComplaintStatus("In Progress", "2023-10-29 16:00", true));
        statuses.add(new ComplaintStatus("Resolved", "2023-10-30 18:00", false));
        return statuses;
    }

    private void addStatusView(ComplaintStatus status, int index, int totalStatus) {
        LayoutInflater inflater = LayoutInflater.from(this);
        // Fix: Inflate status_item.xml instead of activity_complaint_status
        View statusView = inflater.inflate(R.layout.status_item, statusContainer, false);

        TextView statusNameTextView = statusView.findViewById(R.id.statusNameTextView);
        TextView statusTimeTextView = statusView.findViewById(R.id.statusTimeTextView);
        View statusLine = statusView.findViewById(R.id.statusLine);
        View statusCircle = statusView.findViewById(R.id.statusCircle);

        statusNameTextView.setText(status.getName());
        statusTimeTextView.setText(status.getTime());

        if (status.isCompleted()) {
            statusCircle.setBackgroundResource(R.drawable.status_completed_circle);
            statusLine.setBackgroundResource(R.color.status_completed_line);
        } else {
            statusCircle.setBackgroundResource(R.drawable.status_pending_circle);
            statusLine.setBackgroundResource(R.color.status_pending_line);
        }

        // Hide the line for the last status
        if (index == totalStatus - 1) {
            statusLine.setVisibility(View.GONE);
        }

        statusContainer.addView(statusView);
    }

    private static class ComplaintStatus {
        private final String name;
        private final String time;
        private final boolean completed;

        ComplaintStatus(String name, String time, boolean completed) {
            this.name = name;
            this.time = time;
            this.completed = completed;
        }

        String getName() {
            return name;
        }

        String getTime() {
            return time;
        }

        boolean isCompleted() {
            return completed;
        }
    }
}
