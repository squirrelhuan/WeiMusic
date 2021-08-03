package com.demomaster.weimusic.interfaces;


import com.demomaster.weimusic.model.MusicInfo;

import java.util.List;

/**
 * 接口类，抽象方法用来获取过滤后的数据
 *
 */
public interface FilterListener {
    void getFilterData(List<MusicInfo> list);// 获取过滤后的数据
}