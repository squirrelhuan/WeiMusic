package com.demomaster.weimusic.constant;

public class ThemeConstants {
    public static enum WallPagerType {
        withSystem(0),//系统自带
        withMusic(1),//跟随音乐
        customPicture(2);//自定义图片

        private int value = 0;
        WallPagerType(int value) {//必须是private的，否则编译错误
            this.value = value;
        }

        public static WallPagerType valueOf(int value) {
            switch (value) {
                case 0:
                    return withSystem;
                case 1:
                    return withMusic;
                case 2:
                    return customPicture;
                default:
                    return withSystem;
            }
        }

        public int value() {
            return this.value;
        }
    }
    public static enum CoverType {
        withSystem(0),//系统自带
        withMusic(1),//跟随音乐
        customPicture(2);//自定义图片

        private int value = 0;
        CoverType(int value) {//必须是private的，否则编译错误
            this.value = value;
        }

        public static CoverType valueOf(int value) {
            switch (value) {
                case 0:
                    return withSystem;
                case 1:
                    return withMusic;
                case 2:
                    return customPicture;
                default:
                    return withSystem;
            }
        }

        public int value() {
            return this.value;
        }
    }

}
