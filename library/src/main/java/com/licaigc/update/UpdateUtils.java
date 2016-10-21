package com.licaigc.update;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.licaigc.AndroidBaseLibrary;
import com.licaigc.ManifestUtils;
import com.licaigc.PackageUtils;
import com.licaigc.PermissionUtils;
import com.licaigc.library.R;
import com.licaigc.network.NetworkUtils;
import com.licaigc.rxjava.SimpleEasySubscriber;
import com.licaigc.view.ViewUtils;

import java.io.File;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by walfud on 2016/7/28.
 */
public class UpdateUtils {
    public static final String TAG = "UpdateUtils";

    private static IUpdate getUpdateInterface() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("http://v.guihua.com/v1/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(IUpdate.class);
    }

    public interface OnCheckUpdate {
        /**
         * 无更新
         */
        int NO_UPDATE = 1;
        /**
         * `onUpdate` 返回了 `false` (即 客户端取消)
         */
        int CANCEL = 10;
        /**
         * 用户点击了 '立即升级'
         */
        int USER_OK = 100;
        /**
         * 用户点击了 '取消'
         */
        int USER_CANCEL = 101;
        /**
         * 内部错误
         */
        int ERROR = 1000;

        /**
         * 服务器正常返回时会被调用, 主线程被调用
         *
         * @param oldVersionName
         * @param newVersionName
         * @return `true` 则继续后续弹窗流程, `false` 则终止
         */
        boolean onUpdate(String oldVersionName, String newVersionName);

        /**
         * 总会被调用, 主线程被调用
         *
         * @param result
         */
        void onFinish(int result);
    }

    /**
     * 需要 'WRITE_EXTERNAL_STORAGE' 权限
     *
     * @param context
     * @param onCheckUpdate
     */
    public static void checkUpdate(final Activity context, final OnCheckUpdate onCheckUpdate) {
        checkUpdate(context, ManifestUtils.getMeta("UMENG_CHANNEL"), onCheckUpdate);
    }
    public static void checkUpdate(final Activity context, String channel, final OnCheckUpdate onCheckUpdate) {
        if (!PermissionUtils.hasPermission("android.permission.WRITE_EXTERNAL_STORAGE")) {
            if (onCheckUpdate != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        onCheckUpdate.onFinish(OnCheckUpdate.ERROR);
                    }
                });
            }
            return;
        }

        IUpdate updateInterface = getUpdateInterface();
        final Context appContext = AndroidBaseLibrary.getContext();
        final PackageInfo packageInfo;
        try {
            packageInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();

            if (onCheckUpdate != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        onCheckUpdate.onFinish(OnCheckUpdate.ERROR);
                    }
                });
            }
            return;
        }
        updateInterface.checkUpdate(AndroidBaseLibrary.getContext().getPackageName(), packageInfo.versionName, 1, channel)
//        updateInterface.checkUpdate("com.talicai.timiclient", "2.3.4", 1, "anzhuoshichang")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap(new Func1<Response<ResponseCheckUpdate>, Observable<ResponseCheckUpdate>>() {
                    @Override
                    public Observable<ResponseCheckUpdate> call(final Response<ResponseCheckUpdate> response) {
                        // 返回值详情: http://www.tuluu.com/platform/ymir/wikis/home
                        if (response.code() == 200) {
                            // 没更新
                            if (onCheckUpdate != null) {
                                onCheckUpdate.onFinish(OnCheckUpdate.NO_UPDATE);
                            }
                            return Observable.empty();
                        } else if (response.code() == 222) {
                            // 有更新
                            final ResponseCheckUpdate responseCheckUpdate = response.body();

                            boolean conti = true;
                            if (onCheckUpdate != null) {
                                conti = onCheckUpdate.onUpdate(packageInfo.versionName, responseCheckUpdate.data.version);
                            }
                            if (conti) {
                                return NetworkUtils.get(responseCheckUpdate.data.image)
                                        .observeOn(Schedulers.io())
                                        .map(new Func1<byte[], ResponseCheckUpdate>() {
                                            @Override
                                            public ResponseCheckUpdate call(byte[] bytes) {
                                                try {
                                                    responseCheckUpdate.data.pic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                return responseCheckUpdate;
                                            }
                                        });
                            } else {
                                if (onCheckUpdate != null) {
                                    onCheckUpdate.onFinish(OnCheckUpdate.CANCEL);
                                }
                                return Observable.empty();
                            }
                        } else if (response.code() == 400) {
                            // 客户端错误
                            if (onCheckUpdate != null) {
                                onCheckUpdate.onFinish(OnCheckUpdate.ERROR);
                            }
                            return Observable.error(new RuntimeException(response.body().message));
                        } else if (response.code() == 500){
                            // 服务器内部错误
                            if (onCheckUpdate != null) {
                                onCheckUpdate.onFinish(OnCheckUpdate.ERROR);
                            }
                            return Observable.error(new RuntimeException("服务器正在维护, 请稍后再试..."));
                        } else {
                            // 不支持的 status code 或者
                            if (onCheckUpdate != null) {
                                onCheckUpdate.onFinish(OnCheckUpdate.ERROR);
                            }
                            return Observable.error(new RuntimeException(String.format("不支持的 status code (%d)", response.code())));
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<ResponseCheckUpdate, Void>() {
                    @Override
                    public Void call(final ResponseCheckUpdate responseCheckUpdate) {
                        final Dialog dialog = new Dialog(context, android.R.style.Theme_Material_Dialog);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_check_update);
                        TextView titleTv = ViewUtils.findViewById(dialog, R.id.tv_title);
                        TextView descTv = ViewUtils.findViewById(dialog, R.id.tv_desc);
                        ImageView picIv = ViewUtils.findViewById(dialog, R.id.iv_pic);
                        Button okBtn = ViewUtils.findViewById(dialog, R.id.btn_ok);
                        Button cancelBtn = ViewUtils.findViewById(dialog, R.id.btn_cancel);

                        titleTv.setText(responseCheckUpdate.data.title);
                        descTv.setText(responseCheckUpdate.data.desc);
                        if (responseCheckUpdate.data.pic != null) {
                            picIv.setImageBitmap(responseCheckUpdate.data.pic);
                        } else {
                            // 下载图片失败, 则隐藏图片改用短样式
                            picIv.setVisibility(View.GONE);
                        }
                        okBtn.setBackgroundColor(AndroidBaseLibrary.getPrimaryColor());
                        if (responseCheckUpdate.data.force) {
                            cancelBtn.setVisibility(View.GONE);
                            dialog.setCancelable(false);
                        }
                        okBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                NetworkUtils.downloadBySystem(responseCheckUpdate.data.url, new File(appContext.getExternalCacheDir(), responseCheckUpdate.data.md5), new NetworkUtils.OnDownloadBySystem() {
                                    @Override
                                    public void onFinish(boolean suc, File file) {
                                        if (suc) {
                                            PackageUtils.installPackage(file);
                                        }
                                    }
                                });
                                dialog.dismiss();

                                if (onCheckUpdate != null) {
                                    onCheckUpdate.onFinish(OnCheckUpdate.USER_OK);
                                }
                            }
                        });
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();

                                if (onCheckUpdate != null) {
                                    onCheckUpdate.onFinish(OnCheckUpdate.USER_CANCEL);
                                }
                            }
                        });
                        dialog.show();
                        return null;
                    }
                })
                .subscribe(new SimpleEasySubscriber<Void>());
    }

    //

}
