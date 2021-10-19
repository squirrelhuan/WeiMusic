package com.demomaster.weimusic.constant;

public enum AudioStation {
    idle(0,"暂无播放"),
    change(1,"change"),
    PLAYSTATE_CHANGED(2,"播放状态变换"),
    song_changed(3,"歌曲切换"),
    QUEUE_CHANGED(4,"播放列表切换"),
    FAVORITE_CHANGED(5,"收藏状态变化"),
    REPEATMODE_CHANGED(6,"播放顺序变化"),
    service_ready(7,"播放服务初始化完成"),
    Play(8,"播放"),
    Pause(9,"暂停"),
    loadData(10,"数据加载"),
    preToPlay(11,"准备播放"),
    preToPause(12,"准备暂停"),
    preToPlayNext(13,"准备播放下一首"),
    preToPlayLast(14,"准备播放上一首"),
    audio_source_change_next(15,"音频源切换到下一首"),
    audio_source_change_last(16,"音频源切换到上一首"),
    audio_ready(17,"音频准备完成"),
    audio_source_change(18,"音频源切换了"),
    permission_pass(21,"權限通過"),
    LoseFocus(31,"失去焦點"),
    HasHide(32,"已隐藏"),

    DOWNLOAD_SUCCESS(48,"下载完成"),
    sheet_changed(49,"歌单信息变更"),
    sheet_create(50,"歌单创建"),

    ThemeCoverChange(51,"主题封面更改"),//主题封面更改
    ThemeWallPagerChange(52,"主题壁紙更改"),


    CURSOR_CHANGED(61,"游标移动");//主题壁紙更改
    private int value = 0;
    private String desc = "";

    AudioStation(int value) {//必须是private的，否则编译错误
        this.value = value;
    }

    AudioStation(int value,String desc) {//必须是private的，否则编译错误
        this.value = value;
        this.desc = desc;
    }

    public int value() {
        return this.value;
    }

    public String getDesc() {
        return desc;
    }

    public static AudioStation getEnum(int value) {
        AudioStation resultEnum = null;
        AudioStation[] enumArray = AudioStation.values();
        for (int i = 0; i < enumArray.length; i++) {
            if (enumArray[i].value() == value) {
                resultEnum = enumArray[i];
                break;
            }
        }
        return resultEnum;
    }
}
