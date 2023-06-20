package com.example.absensi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class DashboardLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "com.example.absensi.DashboardLoginActivity";
    private AppCompatButton btn_login, btn_sign_up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_login);
        btn_login = findViewById(R.id.btn_login);
        btn_sign_up = findViewById(R.id.btn_signup);
        if(isDevMode()){
            Toast.makeText(this, "developed mode is active", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "developed mode is not active", Toast.LENGTH_SHORT).show();
        }

        btn_login.setOnClickListener(this);
        btn_sign_up.setOnClickListener(this);
    }

    @android.annotation.TargetApi(17) public boolean isDevMode() {
        if(Integer.valueOf(android.os.Build.VERSION.SDK) == 16) {
            return android.provider.Settings.Secure.getInt(getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED , 0) != 0;
        } else if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 17) {
            return android.provider.Settings.Secure.getInt(getApplicationContext().getContentResolver(),
                    android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0) != 0;
        } else return false;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.btn_login:
                intent = new Intent(this,LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_signup:
                intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}