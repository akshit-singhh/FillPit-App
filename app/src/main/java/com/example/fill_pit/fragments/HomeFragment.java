package com.example.fill_pit.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.Button;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fill_pit.ComplaintStatusActivity;
import com.example.fill_pit.R;
import com.example.fill_pit.SubmissionActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Previous Submissions Card
        CardView cardView = view.findViewById(R.id.previousSubmissionsCard);
        BottomNavigationView bottomNavView = requireActivity().findViewById(R.id.bottomNavBar);
        CardView myButton = view.findViewById(R.id.complaintStatusCard);

        cardView.setOnClickListener(v -> {
            ScaleAnimation scaleAnimation = new ScaleAnimation(
                    1.0f, 0.95f, 1.0f, 0.95f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            );
            scaleAnimation.setDuration(100);
            scaleAnimation.setFillAfter(true);
            v.startAnimation(scaleAnimation);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

            if (!(currentFragment instanceof HistoryFragment)) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, new HistoryFragment())
                        .addToBackStack("history_fragment")
                        .commit();

                if (bottomNavView != null) {
                    bottomNavView.setSelectedItemId(R.id.history);
                }
            }
        });

        // Upload Image Button
        LinearLayout uploadImageButton = view.findViewById(R.id.uploadImageButton);
        uploadImageButton.setOnClickListener(v -> redirectToSubmissionActivity());

        CardView complaintStatusCard = view.findViewById(R.id.complaintStatusCard); // Add this line
        complaintStatusCard.setOnClickListener(v -> redirectToComplaintStatusActivity()); // Add this listener

        return view;
    }

    private void redirectToSubmissionActivity() {
        Intent intent = new Intent(requireActivity(), SubmissionActivity.class);
        startActivity(intent);
    }

    private void redirectToComplaintStatusActivity() {
        Intent intent = new Intent(requireActivity(), ComplaintStatusActivity.class);
        startActivity(intent);
    }
}