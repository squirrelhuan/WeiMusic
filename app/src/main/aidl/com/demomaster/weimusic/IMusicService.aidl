package com.demomaster.weimusic;

import android.graphics.Bitmap;
interface IMusicService
{
    boolean isPlaying();
    void stop();
    void pause();
    void play();
    void prev();
    void next();
    long duration();
    long position();
    int seek(int pos);
    String getTrackName();
    String getAlbumName();
    long getAlbumId();
}

