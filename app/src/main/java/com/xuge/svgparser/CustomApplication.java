package com.xuge.svgparser;

import android.app.Application;
import android.content.Context;

/**
 * Created at 2019/4/24 下午7:35.
 *
 * @author yixu.wang
 */
public class CustomApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
