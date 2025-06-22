package com.example.fill_pit.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fill_pit.EditProfileActivity;
import com.example.fill_pit.LauncherActivity;
import com.example.fill_pit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsFragment extends Fragment {

    private TextView fullNameTextView, userIdTextView, phoneNumberTextView, emailAddressTextView,
            addressTextView, cityTextView, stateTextView, postalCodeTextView,
            municipalAreaTextView, registrationDateTextView;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static final String PREFS_NAME = "UserProfilePrefs";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        fullNameTextView = view.findViewById(R.id.fullName);
        userIdTextView = view.findViewById(R.id.userId);
        phoneNumberTextView = view.findViewById(R.id.phoneNumber);
        emailAddressTextView = view.findViewById(R.id.emailAddress);
        addressTextView = view.findViewById(R.id.streetAddress);
        cityTextView = view.findViewById(R.id.city);
        stateTextView = view.findViewById(R.id.state);
        postalCodeTextView = view.findViewById(R.id.postalCode);
        municipalAreaTextView = view.findViewById(R.id.municipalArea);
        registrationDateTextView = view.findViewById(R.id.registrationDate);
        ImageButton editProfileButton = view.findViewById(R.id.editProfileButton);
        Button logoutButton = view.findViewById(R.id.logoutButton);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            usersRef = FirebaseDatabase.getInstance().getReference("Users");
            loadProfileData(currentUser.getEmail()); // Only once initially
        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                loadProfileData(user.getEmail());
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logout());

        return view;
    }

    private void loadProfileData(String currentUserEmail) {
        swipeRefreshLayout.setRefreshing(true); // Show refresh spinner

        usersRef.orderByChild("email").equalTo(currentUserEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        swipeRefreshLayout.setRefreshing(false); // Hide refresh spinner
                        boolean found = false;

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            found = true;
                            String customUserId = userSnapshot.getKey();

                            setTextView(fullNameTextView, "Full Name:", userSnapshot.child("fullName").getValue(String.class));
                            setTextView(userIdTextView, "User ID:", customUserId);
                            setTextView(phoneNumberTextView, "Phone:", userSnapshot.child("phone").getValue(String.class));
                            setTextView(emailAddressTextView, "Email:", userSnapshot.child("email").getValue(String.class));
                            setTextView(addressTextView, "Street Address:", userSnapshot.child("address").getValue(String.class));
                            setTextView(cityTextView, "City:", userSnapshot.child("city").getValue(String.class));
                            setTextView(stateTextView, "State:", userSnapshot.child("state").getValue(String.class));
                            setTextView(postalCodeTextView, "Postal Code:", userSnapshot.child("postalCode").getValue(String.class));
                            setTextView(municipalAreaTextView, "Municipal Area:", userSnapshot.child("municipal").getValue(String.class));
                            setTextView(registrationDateTextView, "Registration Date:", userSnapshot.child("registrationDate").getValue(String.class));
                            break;
                        }

                        if (!found) {
                            Toast.makeText(getContext(), "User not found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setTextView(TextView textView, String label, String value) {
        textView.setText(label + " " + (value != null && !value.isEmpty() ? value : "N/A"));
    }

    private void logout() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        auth.signOut();

        Intent intent = new Intent(getActivity(), LauncherActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
