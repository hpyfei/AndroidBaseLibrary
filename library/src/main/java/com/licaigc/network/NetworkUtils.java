package com.licaigc.network;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;

import com.licaigc.AndroidBaseLibrary;
import com.licaigc.PermissionUtils;
import com.licaigc.algorithm.hash.HashUtils;
import com.licaigc.io.IoUtils;
import com.licaigc.rxjava.SimpleEasySubscriber;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by walfud on 2016/7/28.
 */
public class NetworkUtils {
    public static final String TAG = "NetworkUtils";

    // Get Byte Data

    /**
     * 一次性下载所有数据. 如果下载大量数据, 建议使用 {@link #getLarge(String, OnGetLarge)}.
     *
     * @param url
     * @return 如果下载失败, 则返回 `null`
     * @see #getLarge(String, OnGetLarge)
     */
    public static Observable<byte[]> get(String url) {
        INetwork networkInterface = getNetworkInterface();
        return networkInterface.get(url)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Response<ResponseBody>, byte[]>() {
                    @Override
                    public byte[] call(Response<ResponseBody> response) {
                        byte[] bytes = null;
                        if (response.isSuccessful()) {
                            try {
                                bytes = response.body().bytes();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        return bytes;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public interface OnGet {
        /**
         * 总会被调用
         *
         * @param suc       成功返回 `true`, 失败返回 `false`
         * @param result    成功时有效
         * @param throwable 失败时有效
         */
        void onFinish(boolean suc, byte[] result, Throwable throwable);
    }

    public static void get(String url, final OnGet onGet) {
        get(url).subscribe(new SimpleEasySubscriber<byte[]>() {
            @Override
            public void onFinish(boolean suc, byte[] result, Throwable throwable) {
                super.onFinish(suc, result, throwable);

                if (onGet != null) {
                    onGet.onFinish(suc, result, throwable);
                }
            }
        });
    }

    public interface OnGetLarge {
        void onStart();

        /**
         * @param bytes
         * @param totalLength    总数据量
         * @param receivedLength 已收到的总数据量
         * @return 返回 false 表明请求取消任务, 则不再收到后续 `onData` 回调, 同时 `onFinish` 认为失败
         */
        boolean onData(byte[] bytes, long totalLength, long receivedLength);

        /**
         * 总会被调用
         *
         * @param suc       成功返回 `true`, 失败返回 `false`. `onData` 返回 false 导致任务取消也算失败.
         * @param throwable 失败时有效
         */
        void onFinish(boolean suc, Throwable throwable);
    }

    public static abstract class SimpleOnGetLarge implements OnGetLarge {
        @Override
        public void onStart() {
        }

        @Override
        public boolean onData(byte[] bytes, long totalLength, long receivedLength) {
            return true;
        }

        @Override
        public void onFinish(boolean suc, Throwable throwable) {
        }
    }

    public static void getLarge(final String url, final OnGetLarge onGetLarge) {
        INetwork networkInterface = getNetworkInterface();
        networkInterface.getLarge(url)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Response<ResponseBody>, InputStream>() {
                    private long mCurrLen;

                    @Override
                    public InputStream call(Response<ResponseBody> response) {
                        Throwable err = null;
                        if (response.isSuccessful()) {
                            InputStream inputStream = response.body().byteStream();
                            final int bufLen = 8 * 1024;
                            byte[] buf = new byte[bufLen];
                            try {
                                int readLen;
                                while ((readLen = inputStream.read(buf)) != -1) {
                                    mCurrLen += readLen;

                                    boolean conti = true;
                                    if (onGetLarge != null) {
                                        if (readLen == bufLen) {
                                            conti = onGetLarge.onData(buf, response.body().contentLength(), mCurrLen);
                                        } else {
                                            // 最后一次, 可能读取不足 bufLen
                                            conti = onGetLarge.onData(Arrays.copyOf(buf, readLen), response.body().contentLength(), mCurrLen);
                                        }
                                    }
                                    if (!conti) {
                                        throw new RuntimeException("user stopped");
                                    }
                                }
                                return null;
                            } catch (Exception e) {
                                e.printStackTrace();
                                err = e;
                            }
                        }
                        throw new RuntimeException(err);
                    }
                })
                .subscribe(new SimpleEasySubscriber<InputStream>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        if (onGetLarge != null) {
                            onGetLarge.onStart();
                        }
                    }

                    @Override
                    public void onFinish(boolean suc, InputStream result, Throwable throwable) {
                        super.onFinish(suc, result, throwable);
                        if (onGetLarge != null) {
                            onGetLarge.onFinish(suc, throwable);
                        }
                    }
                });
    }

    // Download File

    /**
     * 默认存放在 `Context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)` 中.
     * 需要 'WRITE_EXTERNAL_STORAGE' 权限.
     * @param url
     * @return
     */
    public static Observable<File> downloadFile(String url) {
        if (!PermissionUtils.hasPermission("android.permission.WRITE_EXTERNAL_STORAGE")) {
            return Observable.empty();
        }

        return downloadFile(url, new File(AndroidBaseLibrary.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), HashUtils.md5(url)));
    }
    public static Observable<File> downloadFile(String url, final File target) {
        return get(url)
                .observeOn(Schedulers.io())
                .map(new Func1<byte[], File>() {
                    @Override
                    public File call(byte[] bytes) {
                        if (IoUtils.output(target, bytes) == null) {
                            throw new RuntimeException(String.format("Write failed: %s", target.getAbsolutePath()));
                        }
                        return target;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public interface OnDownloadFile {
        void onFinish(boolean suc);
    }

    public static void downloadFile(String url, final File target, final OnDownloadFile onDownloadFile) {
        downloadFile(url, target).subscribe(new SimpleEasySubscriber<File>() {
            @Override
            public void onFinish(boolean suc, File result, Throwable throwable) {
                super.onFinish(suc, result, throwable);
                if (onDownloadFile != null) {
                    onDownloadFile.onFinish(suc);
                }
            }
        });
    }

    public interface OnDownloadFileWithProgress {
        void onStart();

        /**
         * @param progress [0, 100]
         */
        boolean onProgress(int progress);

        void onFinish(boolean suc);
    }

    public static void downloadFileWithProgress(final String url, final File target, final OnDownloadFileWithProgress onDownloadFileWithProgress) {
        getLarge(url, new SimpleOnGetLarge() {
            @Override
            public void onStart() {
                super.onStart();
                if (onDownloadFileWithProgress != null) {
                    onDownloadFileWithProgress.onStart();
                }
            }

            @Override
            public boolean onData(byte[] bytes, long totalLength, long receivedLength) {
                boolean ret = super.onData(bytes, totalLength, receivedLength);
                IoUtils.output(target, bytes, receivedLength > bytes.length);

                if (onDownloadFileWithProgress != null) {
                    ret = onDownloadFileWithProgress.onProgress((int) (receivedLength * 100 / totalLength));
                }
                return ret;
            }

            @Override
            public void onFinish(boolean suc, Throwable throwable) {
                super.onFinish(suc, throwable);
                if (onDownloadFileWithProgress != null) {
                    onDownloadFileWithProgress.onFinish(suc);
                }
            }
        });
    }

    public interface OnDownloadBySystem {
        void onFinish(boolean suc, File file);
    }

    /**
     * 需要 'WRITE_EXTERNAL_STORAGE' 权限. 此外, 会读取 'style/primaryColor' 属性.
     *
     * @param url
     * @param target
     * @param onDownloadBySystem
     */
    public static void downloadBySystem(String url, File target, final OnDownloadBySystem onDownloadBySystem) {
        if (!PermissionUtils.hasPermission("android.permission.WRITE_EXTERNAL_STORAGE")) {
            return;
        }

        Context appContext = AndroidBaseLibrary.getContext();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationUri(Uri.fromFile(target));
        final DownloadManager downloadManager = (DownloadManager) appContext.getSystemService(Context.DOWNLOAD_SERVICE);
        final long id = downloadManager.enqueue(request);
        appContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(id));
                if (cursor.moveToFirst()) {
                    Uri localUri = Uri.parse(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
                    switch (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            if (onDownloadBySystem != null) {
                                onDownloadBySystem.onFinish(true, new File(localUri.getPath()));
                            }
                            break;
                        case DownloadManager.STATUS_FAILED:
                            if (onDownloadBySystem != null) {
                                onDownloadBySystem.onFinish(false, null);
                            }
                            break;
                    }
                }
                cursor.close();
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    // Network Type

    /**
     * 返回当前联网类型
     * 需要 `android.permission.ACCESS_NETWORK_STATE` 权限
     * @return -1: 没网, 0: 移动网络, 1: wifi
     * @see {@link ConnectivityManager#TYPE_MOBILE}
     * @see {@link ConnectivityManager#TYPE_WIFI}
     */
    public static int getNetworkType() {
        if (!PermissionUtils.hasPermission("android.permission.ACCESS_NETWORK_STATE")) {
            return -1;
        }

        ConnectivityManager connMgr = (ConnectivityManager) AndroidBaseLibrary.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() ? networkInfo.getType() : -1;
    }

    public static boolean isWifiConnected() {
        return getNetworkType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isNetworkAvailable() {
        return getNetworkType() != -1;
    }

    //
    private static INetwork getNetworkInterface() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("http://empty")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(INetwork.class);
    }
}
