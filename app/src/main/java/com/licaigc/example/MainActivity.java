package com.licaigc.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.licaigc.AndroidBaseLibrary;
import com.licaigc.collection.CollectionUtils;
import com.licaigc.trace.Track;
import com.licaigc.update.UpdateUtils;

import java.util.ArrayList;
import java.util.List;

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
                UpdateUtils.checkUpdate(MainActivity.this, new UpdateUtils.OnCheckUpdate() {
                    @Override
                    public boolean onUpdate(String oldVersionName, String newVersionName) {
                        Log.e(TAG, oldVersionName + ":" + newVersionName);
                        Toast.makeText(MainActivity.this, "onUpdate", Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    @Override
                    public void onFinish(int result) {
                        Log.e(TAG, String.valueOf(result));
                        Toast.makeText(MainActivity.this, "onFinish", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        AndroidBaseLibrary.initialize(getApplicationContext());

        Track.onActivate();

        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            l.add(i);
        }

        List<Integer> ff = CollectionUtils.filter(l, new Comparable<Integer>() {
            @Override
            public int compareTo(Integer integer) {
                return (4 <= integer && integer < 9) ? 0 : 1;
            }
        });

        mBtn.post(new Runnable() {
            @Override
            public void run() {
                mBtn.performClick();
            }
        });
    }
}
