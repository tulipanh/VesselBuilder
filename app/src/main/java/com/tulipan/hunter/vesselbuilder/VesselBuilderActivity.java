package com.tulipan.hunter.vesselbuilder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class VesselBuilderActivity extends FragmentActivity {
    public static final int REQUEST_WRITE_STORAGE = 100;
    private static boolean mPermissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vessel_builder_activity_layout);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new HomePageFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }

        boolean permissions = checkPermissions(); // This is weird. I may need to split into two functions.
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    public void replaceFragment(Fragment newFragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void reloadCurrentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment current = fm.findFragmentById(R.id.fragment_container);
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.detach(current);
        transaction.attach(current);
        transaction.commit();
    }

    public void clearFragmentStack() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment home = new HomePageFragment();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction().replace(R.id.fragment_container, home).addToBackStack(null).commit();
    }

    public boolean checkPermissions() {
        if (mPermissionsGranted) {
            return true;
        } else {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            } else {
                mPermissionsGranted = true;
            }
            return mPermissionsGranted;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Good. Continue.
                    mPermissionsGranted = true;
                } else {
                    Toast.makeText(this, "The app was not granted the permissions required to view and save models.", Toast.LENGTH_LONG).show();
                    mPermissionsGranted = false;
                }
            }
        }
    }
}
