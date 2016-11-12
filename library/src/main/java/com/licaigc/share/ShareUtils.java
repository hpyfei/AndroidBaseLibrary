package com.licaigc.share;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import com.licaigc.AndroidBaseLibrary;
import com.licaigc.android.PackageUtils;

import java.io.File;

/**
 * Created by walfud on 2016/8/9.
 */
public class ShareUtils {
    public static final String TAG = "ShareUtils";

    public static void sendImageToWechat(File img) {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName(PackageUtils.PKG_NAME_WECHAT, "com.tencent.mm.ui.tools.ShareImgUI");
        intent.setComponent(comp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(img));
        AndroidBaseLibrary.getContext().startActivity(intent);
    }
}
