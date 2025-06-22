package com.example.fill_pit;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // Not logged in
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            // Logged in
            startActivity(new Intent(this, MainActivity.class));
        }

        finish(); // Close LauncherActivity
    }
}
