package cn.demomaster.sporthealth_library;

import android.app.Application;

import cn.demomaster.qdlogger_library.QDLogger;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        QDLogger.init(this, "/fitnessexercise/");
    }
}
