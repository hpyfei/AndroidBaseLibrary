package com.licaigc.update;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by walfud on 2016/7/28.
 * <a href="http://www.tuluu.com/platform/ymir/wikis/home">doc</a>
 */
public interface IUpdate {
    @GET("update/check")
    Observable<Response<ResponseCheckUpdate>> checkUpdate(@Query("pkg_name") String pkgName, @Query("version") String versionName, @Query("platform") int platform, @Query("channel") String channel);
}
