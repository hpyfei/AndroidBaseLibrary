package com.licaigc.android;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by walfud on 2016/11/12.
 */

public class BaseActivity extends Activity {
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
