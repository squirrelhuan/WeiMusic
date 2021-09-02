package com.demomaster.weimusic.constant;


import com.demomaster.weimusic.model.MusicResponse;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * Created by Squirrel桓 on 2019/1/1.
 */
public interface RetrofitInterface {

    @Headers({"User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
    @GET("http://music.163.com/api/search/pc?csrf_token=hlpretag=&hlposttag=&&type=1&offset=0&total=true&limit=20")
    Observable<MusicResponse> searchMusicInfo(@Query("s") String key);

}
