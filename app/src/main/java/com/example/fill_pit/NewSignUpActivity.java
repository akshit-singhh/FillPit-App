package com.example.fill_pit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class NewSignUpActivity extends AppCompatActivity {

    private EditText fullNameEditText, userIdEditText, phoneEditText, emailEditText,
            addressEditText, cityEditText, stateEditText, postalCodeEditText, municipalEditText;
    private Button submitButton;

    private DatabaseReference userRef;
    private String generatedUserId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sign_up);

        initializeUI();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users"); // ✅ updated node name

        if (currentUser != null) {
            emailEditText.setText(currentUser.getEmail());
            emailEditText.setEnabled(false);
        } else {
            Toast.makeText(this, "No authenticated user found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userIdEditText.setEnabled(false);
        generateUniqueUserId();

        submitButton.setOnClickListener(v -> saveUserDetails());
    }

    private void initializeUI() {
        fullNameEditText = findViewById(R.id.editTextFullName);
        userIdEditText = findViewById(R.id.editUserId);
        phoneEditText = findViewById(R.id.editPhone);
        emailEditText = findViewById(R.id.editEmail);
        addressEditText = findViewById(R.id.editStreet);
        cityEditText = findViewById(R.id.editTextCity);
        stateEditText = findViewById(R.id.editState);
        postalCodeEditText = findViewById(R.id.editTextPostalCode);
        municipalEditText = findViewById(R.id.editMunicipal);
        submitButton = findViewById(R.id.submit_button);
    }

    private void generateUniqueUserId() {
        Random random = new Random();
        attemptGenerateId(random, 5);
    }

    private void attemptGenerateId(Random random, int remainingAttempts) {
        if (remainingAttempts <= 0) {
            Toast.makeText(this, "Failed to generate unique User ID. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = "Fill_" + (10000 + random.nextInt(90000));

        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    generatedUserId = userId;
                    userIdEditText.setText(generatedUserId);
                } else {
                    attemptGenerateId(random, remainingAttempts - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NewSignUpActivity.this, "Error generating User ID", Toast.LENGTH_SHORT).show();
                Log.e("UserIDGeneration", "Firebase error: " + error.getMessage());
            }
        });
    }

    private void saveUserDetails() {
        String fullName = fullNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String state = stateEditText.getText().toString().trim();
        String postalCode = postalCodeEditText.getText().toString().trim();
        String municipal = municipalEditText.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || address.isEmpty() || city.isEmpty() ||
                state.isEmpty() || postalCode.isEmpty() || municipal.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (generatedUserId == null || generatedUserId.isEmpty()) {
            Toast.makeText(this, "User ID not generated. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        submitButton.setEnabled(false); // Prevent multiple clicks

        String registrationDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a", Locale.getDefault()).format(new Date());

        user_data user = new user_data(
                fullName,
                generatedUserId,
                phone,
                email,
                address,
                city,
                state,
                postalCode,
                municipal,
                registrationDate
        );

        userRef.child(generatedUserId).setValue(user).addOnCompleteListener(task -> {
            submitButton.setEnabled(true);
            if (task.isSuccessful()) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                prefs.edit()
                        .putBoolean("isRegistered", true)
                        .putString("userId", generatedUserId)
                        .apply();

                Toast.makeText(this, "Details saved successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(NewSignUpActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save details", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error saving user: " + task.getException());
            }
        });
    }
}