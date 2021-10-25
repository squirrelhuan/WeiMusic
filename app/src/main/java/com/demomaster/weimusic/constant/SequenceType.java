package com.demomaster.weimusic.constant;

public enum SequenceType {
    Unknow(-1,"Unknow"),//不循环，不随机
    Normal(0,"順序播放"),//不循环，不随机
    REPEAT_ALL(1,"全部循环"),//全部循环
    Shuffle(2,"随机播放"),//随机播放
    REPEAT_CURRENT(3,"单曲循环");//单曲循环
    private int value = 0;
    private String name;
    SequenceType(int value,String name) {//必须是private的，否则编译错误
        this.value = value;
        this.name = name;
    }
    public int value() {
        return this.value;
    }

    public static SequenceType getEnum(int value) {
        SequenceType resultEnum = null;
        SequenceType[] enumArray = SequenceType.values();
        for (int i = 0; i < enumArray.length; i++) {
            if (enumArray[i].value()  == value) {
                resultEnum = enumArray[i];
                break;
            }
        }
        return resultEnum;
    }

    public String getName() {
        return name;
    }
}
