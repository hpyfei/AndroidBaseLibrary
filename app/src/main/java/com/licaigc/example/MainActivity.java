package com.licaigc.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.licaigc.AndroidBaseLibrary;
import com.licaigc.trace.Track;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    private Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Track.onActivate();
            }
        });

        AndroidBaseLibrary.initialize(getApplicationContext());

        mBtn.post(new Runnable() {
            @Override
            public void run() {
                mBtn.performClick();
            }
        });
    }
}
