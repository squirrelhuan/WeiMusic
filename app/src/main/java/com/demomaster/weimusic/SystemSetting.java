package com.demomaster.weimusic;

import com.demomaster.weimusic.constant.Constants;

import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;

public class SystemSetting {


    /**
     * 获取壁紙跟随系统设置是的 资源id
     * @return
     */
    public static int getWallPagerWithSystemValue() {
        return QDSharedPreferences.getInstance().getInt(Constants.Key_Theme_WallPager_System, 0);
    }

    /**
     * 获取壁紙自定义 图片路径
     * @return
     */
    public static String getWallPagerWithCustomValue() {
        return QDSharedPreferences.getInstance().getString(Constants.Key_Theme_WallPager_Custom, null);
    }

    /**
     * 获取自定以封面 图片路径
     * @return
     */
    public static String getCoverWithCustomValue() {
        return QDSharedPreferences.getInstance().getString(Constants.Key_Theme_Cover_Custom, null);
    }

    /**
     * 获取系统预设的封面资源 id
     * @return
     */
    public static int getCoverWithSystemValue() {
        return QDSharedPreferences.getInstance().getInt(Constants.Key_Theme_Cover_System, 0);
    }
}
