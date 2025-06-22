package com.example.fill_pit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fill_pit.fragments.HistoryFragment;
import com.example.fill_pit.fragments.HomeFragment;
import com.example.fill_pit.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavView;
    private static final int CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Directly load the main UI
        setupMainActivityUI();
    }

    private void setupMainActivityUI() {
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        bottomNavView = findViewById(R.id.bottomNavBar);
        loadFragment(new HomeFragment(), false);
        initializeUI();
    }

    private void initializeUI() {
        LinearLayout uploadImageButton = findViewById(R.id.uploadImageButton);
        LinearLayout previousSubmissionsButton = findViewById(R.id.previousSubmissionsButton);
        androidx.cardview.widget.CardView complaintStatusCard = findViewById(R.id.complaintStatusCard);

        if (uploadImageButton != null) {
            uploadImageButton.setOnClickListener(v -> checkCameraPermission());
        }

        if (previousSubmissionsButton != null) {
            previousSubmissionsButton.setOnClickListener(v -> {
                if (!(fragmentManager.findFragmentById(R.id.fragmentContainer) instanceof HistoryFragment)) {
                    loadFragment(new HistoryFragment(), true);
                }
            });
        }

        if (complaintStatusCard != null) {
            complaintStatusCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ComplaintStatusActivity.class);
                startActivity(intent);
            });
        }

        if (bottomNavView != null) {
            bottomNavView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.history) {
                    selectedFragment = new HistoryFragment();
                } else if (itemId == R.id.settings) {
                    selectedFragment = new SettingsFragment();
                }

                if (selectedFragment != null) {
                    Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
                    if (currentFragment == null || !currentFragment.getClass().equals(selectedFragment.getClass())) {
                        loadFragment(selectedFragment, true);
                    }
                    return true;
                }

                return false;
            });
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        } else {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        transaction.commit();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        // Your camera logic here
        Toast.makeText(this, "Camera opened!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager != null && fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
