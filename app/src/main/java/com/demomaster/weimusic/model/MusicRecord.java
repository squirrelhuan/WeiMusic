package com.demomaster.weimusic.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class MusicRecord implements Serializable {

    private long sheetId;//歌单id
    private long songId;//歌曲id
    private int index;//在歌单中的排序
    private float progress;//播放进度

    public long getSheetId() {
        return sheetId;
    }

    public void setSheetId(long sheetId) {
        this.sheetId = sheetId;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    @NonNull
    @Override
    public String toString() {
        return "sheetId="+sheetId+",songId="+songId+",index="+index+",progress="+progress;
    }
}
