package com.example.absensi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class DashboardActivity extends AppCompatActivity {

    private ChipNavigationBar chipNavigationBar;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        chipNavigationBar = findViewById(R.id.chipNavBar);
        chipNavigationBar.setItemSelected(R.id.home,true);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();

        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (i){
                    case R.id.home:
                        fragment = new HomeFragment();
                        break;
                    case R.id.profile:
                        fragment = new ProfileFragment();
                        break;
                    default:
                }

                if(fragment!=null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                }
            }
        });
    }
}