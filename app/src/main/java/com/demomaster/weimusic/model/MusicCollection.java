package com.demomaster.weimusic.model;

import java.io.Serializable;

/**
 * Created by huan on 2018/1/17.
 */

public class MusicCollection implements Serializable {

    private long id;
    private String name;
    private int count;

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
}
