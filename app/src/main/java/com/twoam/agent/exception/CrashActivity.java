package com.twoam.agent.exception;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.twoam.agent.R;

public class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
    }
}
