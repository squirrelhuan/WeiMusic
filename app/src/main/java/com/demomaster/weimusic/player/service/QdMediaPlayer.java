package com.demomaster.weimusic.player.service;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

public class QdMediaPlayer extends MediaPlayer {

    QdMediaPlayer(){
        OnPreparedListener selfListener = new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                hasPrepared = true;
                if(onPreparedListener!=null) {
                    onPreparedListener.onPrepared(mp);
                }
            }
        };
        super.setOnPreparedListener(selfListener);
    }

    public boolean hasPrepared;
    OnPreparedListener onPreparedListener;
    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        onPreparedListener = listener;
    }

    @Override
    public void release() {
        super.release();
        hasPrepared = false;
    }

    @Override
    public void reset() {
        super.reset();
        hasPrepared = false;
    }

    private boolean hasDataSource;//是否设置了音频源
    private void dataSourceSetting() {
        hasDataSource = true;
        if(onPlayerChangeListener!=null){
            onPlayerChangeListener.onDataSourceChanged();
        }
    }
    @Override
    public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException, IllegalArgumentException, IllegalStateException {
        super.setDataSource(fd, offset, length);
        dataSourceSetting();
    }

    @Override
    public void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException, IllegalStateException {
        super.setDataSource(dataSource);
        dataSourceSetting();
    }

    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri, @Nullable Map<String, String> headers, @Nullable List<HttpCookie> cookies) throws IOException {
        super.setDataSource(context, uri, headers, cookies);
        dataSourceSetting();
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        if(onPlayerChangeListener!=null){
            onPlayerChangeListener.onStart();
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        if(onPlayerChangeListener!=null){
            onPlayerChangeListener.onPause();
        }
    }
    OnPlayerChangeListener onPlayerChangeListener;
    public void setOnPlayerChangeListener(OnPlayerChangeListener onPlayerChangeListener) {
        this.onPlayerChangeListener = onPlayerChangeListener;
    }

    public static interface OnPlayerChangeListener {
        void onStart();

        void onPause();

        void onStop();

        void onComplete();

        void onError();
        void onDataSourceChanged();
    }
}
