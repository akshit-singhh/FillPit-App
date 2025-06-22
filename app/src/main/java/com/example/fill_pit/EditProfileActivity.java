package com.example.fill_pit;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editFullName, editPhoneNumber, editAddress, editCity, editPostalCode, editMunicipalArea;
    private Spinner editStateSpinner;
    private Button updateButton;
    private TextView textViewUserId, registrationDateTextView;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;


    private String currentUserEmail;
    private String customUserId; // Fill_XXXXXX

    private final String[] indianStates = {
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa",
            "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
            "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland",
            "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
            "Uttar Pradesh", "Uttarakhand", "West Bengal", "Andaman and Nicobar Islands",
            "Chandigarh", "Dadra and Nagar Haveli", "Daman and Diu", "Lakshadweep", "Delhi",
            "Puducherry", "Ladakh"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        currentUserEmail = auth.getCurrentUser().getEmail();

        // Init Views
        textViewUserId = findViewById(R.id.textViewUserId);
        editFullName = findViewById(R.id.editFullName);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        editAddress = findViewById(R.id.editStreetAddress);
        editCity = findViewById(R.id.editCity);
        editPostalCode = findViewById(R.id.editPostalCode);
        editMunicipalArea = findViewById(R.id.editMunicipalArea);
        editStateSpinner = findViewById(R.id.editStateSpinner);
        updateButton = findViewById(R.id.updateButton);
        registrationDateTextView = findViewById(R.id.registrationDate);

        // Setup Spinner
        String[] statesWithHint = new String[indianStates.length + 1];
        statesWithHint[0] = "Select State";
        System.arraycopy(indianStates, 0, statesWithHint, 1, indianStates.length);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statesWithHint);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editStateSpinner.setAdapter(adapter);

        // Load data from Firebase
        fetchUserDataFromFirebase();

        // Update button
        updateButton.setOnClickListener(v -> updateUserDataInFirebase());
    }

    private void fetchUserDataFromFirebase() {
        usersRef.orderByChild("email").equalTo(currentUserEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                customUserId = userSnap.getKey(); // like Fill_88501
                                textViewUserId.setText("User ID: " + customUserId);

                                // Populate the editable fields
                                editFullName.setText(userSnap.child("fullName").getValue(String.class));
                                editPhoneNumber.setText(userSnap.child("phone").getValue(String.class));
                                editAddress.setText(userSnap.child("address").getValue(String.class));
                                editCity.setText(userSnap.child("city").getValue(String.class));
                                editPostalCode.setText(userSnap.child("postalCode").getValue(String.class));
                                editMunicipalArea.setText(userSnap.child("municipal").getValue(String.class));

                                // Set the state spinner selection
                                String state = userSnap.child("state").getValue(String.class);
                                editStateSpinner.setSelection(getStatePosition(state));

                                String registrationDate = userSnap.child("registrationDate").getValue(String.class);
                                registrationDateTextView.setText("Registration Date: " + registrationDate);

                                break; // Only first match
                            }
                        } else {
                            Toast.makeText(EditProfileActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditProfileActivity.this, "Error fetching user", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserDataInFirebase() {
        if (customUserId == null) {
            Toast.makeText(this, "User ID missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect data from UI
        String fullName = editFullName.getText().toString().trim();
        String phone = editPhoneNumber.getText().toString().trim();
        String street = editAddress.getText().toString().trim();
        String city = editCity.getText().toString().trim();
        String state = editStateSpinner.getSelectedItem().toString();
        String postal = editPostalCode.getText().toString().trim();
        String municipal = editMunicipalArea.getText().toString().trim();

        // Basic validation
        if (fullName.isEmpty() || phone.isEmpty() || street.isEmpty() || city.isEmpty() || postal.isEmpty() || state.equals("Select State")) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the registration date from the displayed TextView
        String registrationDate = registrationDateTextView.getText().toString().replace("Registration Date: ", "").trim();

        // Model class
        UserData updatedUser = new UserData(fullName, phone, currentUserEmail, street, city, state, postal, municipal, registrationDate);

        // Update in DB
        usersRef.child(customUserId).setValue(updatedUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int getStatePosition(String state) {
        for (int i = 0; i < indianStates.length; i++) {
            if (indianStates[i].equals(state)) {
                return i + 1; // +1 to account for the first "Select State" item
            }
        }
        return 0;
    }

    // Model
    public static class UserData {
        public String fullName, phone, email, address, city, state, postalCode, municipal, registrationDate;

        public UserData() {}

        public UserData(String fullName, String phone, String email, String address,
                        String city, String state, String postalCode, String municipal, String registrationDate) {
            this.fullName = fullName;
            this.phone = phone;
            this.email = email;
            this.address = address;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.municipal = municipal;
            this.registrationDate = registrationDate;
        }
    }
}
