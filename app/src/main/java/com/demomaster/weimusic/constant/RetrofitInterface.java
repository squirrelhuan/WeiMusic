package com.demomaster.weimusic.constant;


import com.demomaster.weimusic.model.MusicResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;


/**
 * Created by Squirrel桓 on 2019/1/1.
 */
public interface RetrofitInterface {

    @Headers({"User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
    @GET("http://music.163.com/api/search/pc?csrf_token=hlpretag=&hlposttag=&&type=1&offset=0&total=true&limit=20")
    Observable<MusicResponse> searchMusicInfo(@Query("s") String key);

}
