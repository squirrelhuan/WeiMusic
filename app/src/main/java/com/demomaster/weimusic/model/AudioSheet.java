package com.demomaster.weimusic.model;

import java.io.Serializable;
import java.util.List;

import cn.demomaster.quickdatabaselibrary.annotation.Constraints;
import cn.demomaster.quickdatabaselibrary.annotation.DBTable;
import cn.demomaster.quickdatabaselibrary.annotation.SQLObj;

/**
 * Created by huan on 2018/1/17.
 */

@DBTable(name = "AudioSheet")
public class AudioSheet implements Serializable {

    @SQLObj(name = "id",constraints = @Constraints(autoincrement = true,primaryKey = true))
    private long id;
    @SQLObj(name = "name")
    private String name;
    @SQLObj(name = "themeColor")
    private int themeColor=0xffffffff;
    private int count;
    @SQLObj(name = "isSystem")
    private boolean isSystem;//是否是系统歌单
    @SQLObj(name = "imgSrc")
    private String imgSrc;
    private List<AudioInfo> audioInfoList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<AudioInfo> getMusicInfoList() {
        return audioInfoList;
    }

    public void setMusicInfoList(List<AudioInfo> audioInfoList) {
        this.audioInfoList = audioInfoList;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public int getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(int themeColor) {
        this.themeColor = themeColor;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }
}
