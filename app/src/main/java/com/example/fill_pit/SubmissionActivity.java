package com.example.fill_pit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.cloudinary.android.MediaManager;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.HashMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SubmissionActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 101;
    private MapView mapView;
    private TextView tvLocation;
    private Marker movableMarker;
    private TextView tvImageName;
    private Button btnSubmit;
    private EditText etDescription, etLandmark;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private ImageView ivPreviewImage;
    private Button btnCaptureImage;
    private FirebaseAuth firebaseAuth;
    private String customUserId = null;

    private DatabaseReference databaseReference;

    private Uri selectedImageUri;

    private Uri cameraImageUri;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Do nothing for automatic location updates
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
    private TextView fullNameTextView, userIdTextView, phoneNumberTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission); // Only one setContentView call

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        btnSubmit = findViewById(R.id.btnSubmit);


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Views
        tvImageName = findViewById(R.id.tvImageName);
        btnCaptureImage = findViewById(R.id.btnSelectImage);
        ivPreviewImage = findViewById(R.id.ivPreviewImage);
        Button btnGetLocation = findViewById(R.id.btnGetLocation);
        etDescription = findViewById(R.id.etDescription);
        etLandmark = findViewById(R.id.etLandmark);
        mapView = findViewById(R.id.mapView);
        tvLocation = findViewById(R.id.tvLocation);
        // Initialize User Info Views
        fullNameTextView = findViewById(R.id.fullName);
        userIdTextView = findViewById(R.id.userId);
        phoneNumberTextView = findViewById(R.id.phoneNumber);


        // Set User-Agent for Configuration (assuming it's for maps or networking)
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Set click listener for capturing/selecting images
        btnCaptureImage.setOnClickListener(v -> showImageOptions());
        btnSubmit.setOnClickListener(v -> {
            handleSubmission();
        });

        // Setup the map
        setupMap();

        // Set click listener for getting location
        btnGetLocation.setOnClickListener(v -> returnToLiveLocation());

        // Load profile data
        loadProfileData();

        fetchUserData();

    }

    private void handleSubmission() {
        String description = etDescription.getText().toString().trim();
        String landmark = etLandmark.getText().toString().trim();
        GeoPoint location = movableMarker.getPosition();

        if (description.isEmpty() || landmark.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String email = user.getEmail();

        // Firebase Database reference to Complaints under this user
        DatabaseReference complaintsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(customUserId) // ✅ use "Fill_50537"
                .child("Complaints");

        // Reference to the ComplaintCounter under this user
        DatabaseReference counterRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(customUserId)
                .child("ComplaintCounter");

        // Get current complaint counter
        counterRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentCounter = mutableData.getValue(Integer.class);
                if (currentCounter == null) {
                    currentCounter = 0; // Initialize counter if it doesn't exist
                }
                int newCounter = currentCounter + 1;
                mutableData.setValue(newCounter); // Increment the counter

                return Transaction.success(mutableData); // Commit transaction
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    String complaintId = dataSnapshot.getValue(Integer.class).toString(); // Get updated complaint ID as string
                    Log.d("Firebase", "Complaint ID: " + complaintId);

                    // Upload image to Cloudinary
                    try {
                        MediaManager.get().upload(selectedImageUri)
                                .option("public_id", "complaint_images/" + complaintId)
                                .callback(new UploadCallback() {
                                    @Override
                                    public void onStart(String requestId) {
                                        Log.d("Cloudinary", "Upload started");
                                        Toast.makeText(SubmissionActivity.this, "Uploading image...", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onProgress(String requestId, long bytes, long totalBytes) {
                                        // Optional: update progress
                                    }

                                    @Override
                                    public void onSuccess(String requestId, Map resultData) {
                                        Log.d("Cloudinary", "Upload successful.");
                                        String imageUrl = resultData.get("secure_url").toString();

                                        // Prepare complaint data to be saved to Firebase
                                        HashMap<String, Object> complaintData = new HashMap<>();
                                        complaintData.put("description", description);
                                        complaintData.put("landmark", landmark);
                                        complaintData.put("latitude", location.getLatitude());
                                        complaintData.put("longitude", location.getLongitude());
                                        complaintData.put("imageUrl", imageUrl);
                                        complaintData.put("userId", customUserId); // ✅ not Firebase UID
                                        complaintData.put("email", email);

                                        // Save complaint data to Firebase
                                        complaintsRef.child(complaintId).setValue(complaintData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Firebase", "Complaint data saved.");
                                                    Toast.makeText(SubmissionActivity.this, "Complaint submitted successfully!", Toast.LENGTH_LONG).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Firebase", "Failed to submit complaint", e);
                                                    Toast.makeText(SubmissionActivity.this, "Failed to submit complaint data", Toast.LENGTH_SHORT).show();
                                                });
                                    }

                                    @Override
                                    public void onError(String requestId, ErrorInfo error) {
                                        Log.e("Cloudinary", "Upload failed: " + error.getDescription());
                                        Toast.makeText(SubmissionActivity.this, "Image upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onReschedule(String requestId, ErrorInfo error) {
                                        Log.e("Cloudinary", "Upload rescheduled: " + error.getDescription());
                                    }
                                })
                                .dispatch();

                    } catch (Exception e) {
                        Log.e("Cloudinary", "Upload error", e);
                        Toast.makeText(SubmissionActivity.this, "An error occurred while uploading image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SubmissionActivity.this, "Failed to increment complaint counter", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                // Search user by email field
                databaseReference.orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                        String fullName = userSnapshot.child("fullName").getValue(String.class);
                                        String phoneNumber = userSnapshot.child("phone").getValue(String.class);

                                        // ✅ Get custom user ID like "Fill_50537"
                                        customUserId = userSnapshot.getKey();

                                        // Update the UI
                                        updateUI(fullName, customUserId, phoneNumber);
                                        break;
                                    }
                                } else {
                                    Toast.makeText(SubmissionActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(SubmissionActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "User email is null", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
        }
    }



    private void updateUI(String fullName, String userId, String phoneNumber) {
        TextView fullNameTextView = findViewById(R.id.fullName);
        TextView userIdTextView = findViewById(R.id.userId);
        TextView phoneNumberTextView = findViewById(R.id.phoneNumber);

        // Update the TextViews with the fetched data
        fullNameTextView.setText("Full Name: " + fullName);
        userIdTextView.setText("User ID: " + userId);
        phoneNumberTextView.setText("Phone: " + phoneNumber);
    }

        // Method to load profile data from SharedPreferences
        private void loadProfileData() {
            SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);

            String fullName = sharedPreferences.getString("fullName", "N/A");
            String userId = sharedPreferences.getString("userId", "N/A");
            String phoneNumber = sharedPreferences.getString("phoneNumber", "N/A");

            setTextView(fullNameTextView, "Full Name:", fullName);
            setTextView(userIdTextView, "User ID:", userId);
            setTextView(phoneNumberTextView, "Phone:", phoneNumber);
        }

        // Helper method to set text for TextViews
        private void setTextView(TextView textView, String label, String value) {
            if (textView != null) {
                textView.setText(label + " " + value);
            }
        }

        @Override
        protected void onResume() {
            super.onResume();
            loadProfileData();  // Refresh data from SharedPreferences
            fetchUserData();  // Refresh data every time the activity becomes visible
        }

    @SuppressLint("ClickableViewAccessibility")
    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        mapController.setCenter(new GeoPoint(28.6139, 77.2090)); // Default: Delhi

        // Add a movable marker on the map
        GeoPoint initialPoint = new GeoPoint(28.6139, 77.2090);
        movableMarker = new Marker(mapView);
        movableMarker.setPosition(initialPoint);
        movableMarker.setDraggable(true); // Make the marker draggable
        movableMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        movableMarker.setTitle("Drag me!");
        mapView.getOverlays().add(movableMarker);

        // Set a listener to update location when marker is moved
        movableMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                GeoPoint newPoint = marker.getPosition();
                updateLocationBasedOnMarker(newPoint);
            }

            @Override
            public void onMarkerDragStart(Marker marker) {}
        });

        // Handling the touch events to prevent scroll for the whole screen
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                // Disable screen scrolling when touching the map
                v.getParent().requestDisallowInterceptTouchEvent(true);
            } else {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }

    private void returnToLiveLocation() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            getCurrentLocation();
        }
    }


    private void showImageOptions() {
        final CharSequence[] options = {"Capture Image", "Choose from Gallery", "Cancel"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Image");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Capture Image")) {
                dispatchTakePictureIntent();
            } else if (options[item].equals("Choose from Gallery")) {
                dispatchPickPictureIntent();
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = File.createTempFile(
                        "IMG_" + System.currentTimeMillis(),
                        ".jpg",
                        getExternalFilesDir(null)
                );
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchPickPictureIntent() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && cameraImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cameraImageUri);
                    ivPreviewImage.setImageBitmap(bitmap);
                    tvImageName.setText("Captured Image");
                    selectedImageUri = cameraImageUri;
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load captured image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        ivPreviewImage.setImageBitmap(bitmap);
                        tvImageName.setText(getFileName(imageUri));
                        selectedImageUri = imageUri;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load selected image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }




    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            Toast.makeText(this, "Location Manager is null", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            Toast.makeText(this, "Location Services are Disabled. Please enable them.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Request high-accuracy location updates
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastKnownLocation != null) {
                updateMapLocation(lastKnownLocation);
                Toast.makeText(this, "Location fetched successfully!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Unable to fetch location. Please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

            private void updateMapLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        GeoPoint currentLocation = new GeoPoint(latitude, longitude);

        mapView.getController().setCenter(currentLocation);
        mapView.zoomToBoundingBox(new BoundingBox(latitude + 0.005, longitude + 0.005,
                latitude - 0.005, longitude - 0.005), true);

        // Move the marker to the live location
        movableMarker.setPosition(currentLocation);
        updateLocationBasedOnMarker(currentLocation);
    }

    private void updateLocationBasedOnMarker(GeoPoint geoPoint) {
        double latitude = geoPoint.getLatitude();
        double longitude = geoPoint.getLongitude();

        // Display the coordinates in the TextView
        String coordinates = "Lat: " + latitude + ", Long: " + longitude;
        tvLocation.setText("Coordinates: " + coordinates);

        // Convert coordinates to address and display
        String address = getAddressFromCoordinates(latitude, longitude);
        tvLocation.append("\nAddress: " + address);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "You have denied the permission permanently. Please enable it from settings.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                }
            }
        }
    }

    // Create a method to convert coordinates to an address using Geocoder
    private String getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getAddressLine(0); // Get the full address
            } else {
                return "Address not found";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Unable to fetch address";
        }
    }

}

