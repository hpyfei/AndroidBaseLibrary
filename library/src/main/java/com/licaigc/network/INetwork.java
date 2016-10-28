package com.licaigc.network;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by walfud on 2016/7/28.
 * <a href="http://www.tuluu.com/platform/ymir/wikis/home">doc</a>
 */
public interface INetwork {
    @GET
    Observable<Response<ResponseBody>> get(@Url String url);

    @GET
    Observable<Response<ResponseBody>> get(@Url String url, @QueryMap Map<String, Object> param);

    @GET
    @Streaming
    Observable<Response<ResponseBody>> getLarge(@Url String url);

    @POST
    Observable<Response<ResponseBody>> post(@Url String url, @Body Object param);
}
