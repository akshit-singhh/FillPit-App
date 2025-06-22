package com.example.fill_pit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        Button loginButton = findViewById(R.id.login_button);
        TextView signupRedirectText = findViewById(R.id.signUpRedirectText);
        TextView forgotPassword = findViewById(R.id.forgot_password);

        auth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String pass = loginPassword.getText().toString().trim();

            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!pass.isEmpty()) {
                    auth.signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(authResult -> {
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                checkUserProfile(); // Check if profile data exists
                            })
                            .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    loginPassword.setError("Password cannot be empty");
                }
            } else if (email.isEmpty()) {
                loginEmail.setError("Email cannot be empty");
            } else {
                loginEmail.setError("Please enter a valid email");
            }
        });

        signupRedirectText.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));

        forgotPassword.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot, null);
            EditText emailBox = dialogView.findViewById(R.id.emailBox);

            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            dialogView.findViewById(R.id.btnReset).setOnClickListener(view1 -> {
                String userEmail = emailBox.getText().toString().trim();

                if (TextUtils.isEmpty(userEmail) || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    Toast.makeText(LoginActivity.this, "Enter your registered email", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Check your email to reset password", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            dialogView.findViewById(R.id.btnCancel).setOnClickListener(view1 -> dialog.dismiss());

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            dialog.show();
        });
    }

    private void checkUserProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userEmail = currentUser.getEmail();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("email").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Profile exists → go to MainActivity
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else {
                            // Profile not found → go to NewSignUpActivity
                            startActivity(new Intent(LoginActivity.this, NewSignUpActivity.class));
                        }
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "Error checking user profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Auto-login check: user is already logged in, check their profile
            checkUserProfile();
        }
    }
}
