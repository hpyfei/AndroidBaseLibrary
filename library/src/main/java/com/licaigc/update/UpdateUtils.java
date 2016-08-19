package com.licaigc.update;


import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.licaigc.AndroidBaseLibrary;
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
                .baseUrl(String.format("%s/v%s/", "http://192.168.8.21:34000", 1))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(IUpdate.class);
    }

    public interface OnCheckUpdate {
        void onFinish(boolean okOrCancel);
    }

    /**
     * 需要 'WRITE_EXTERNAL_STORAGE' 权限
     *
     * @param context
     * @param pkgName
     */
    public static void checkUpdate(final Context context, String pkgName, final OnCheckUpdate onCheckUpdate) {
        if (!PermissionUtils.hasPermission("android.permission.WRITE_EXTERNAL_STORAGE")) {
            return;
        }

        IUpdate updateInterface = getUpdateInterface();
        final Context appContext = AndroidBaseLibrary.getContext();
        PackageInfo packageInfo = null;
        try {
            packageInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        updateInterface.checkUpdate(pkgName, packageInfo.versionName, packageInfo.versionCode, ManifestUtils.getMeta("UMENG_CHANNEL"))
        updateInterface.checkUpdate("com.talicai.timiclient", "2.3.4", 1, "anzhuoshichang")
                .subscribeOn(Schedulers.io())
                .concatMap(new Func1<Response<ResponseCheckUpdate>, Observable<ResponseCheckUpdate>>() {
                    @Override
                    public Observable<ResponseCheckUpdate> call(final Response<ResponseCheckUpdate> response) {
                        // 返回值详情: http://www.tuluu.com/platform/ymir/wikis/home
                        if (response.code() == 200) {
                            // 没更新
                        } else if (response.code() == 222) {
                            // 有更新
                            final ResponseCheckUpdate responseCheckUpdate = response.body();
                            return NetworkUtils.get(responseCheckUpdate.data.image)
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
                        } else if (response.code() == 400) {
                            throw new RuntimeException(response.body().message);
                        } else if (response.code() == 500){
                            // 服务器内部错误
                            throw new RuntimeException("服务器正在维护, 请稍后再试...");
                        } else {
                            // 不支持的 status code 或者
                            throw new RuntimeException(String.format("不支持的 status code (%d)", response.code()));
                        }

                        return Observable.empty();
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

                        if (responseCheckUpdate.data.pic == null) {
                            // 下载图片失败, 则隐藏图片改用短样式
                            picIv.setVisibility(View.GONE);
                        }
                        TypedValue typedValue = new TypedValue();
                        if (context.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true)) {
                            int primaryColor = typedValue.data;
                            okBtn.setBackgroundColor(primaryColor);
                        }
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
                                    onCheckUpdate.onFinish(true);
                                }
                            }
                        });
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();

                                if (onCheckUpdate != null) {
                                    onCheckUpdate.onFinish(false);
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
