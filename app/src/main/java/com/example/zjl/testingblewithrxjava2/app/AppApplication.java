package com.example.zjl.testingblewithrxjava2.app;

import android.content.Context;


import com.example.zjl.testingblewithrxjava2.BuildConfig;
import com.jaydenxiao.common.baseapp.BaseApplication;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.polidea.rxandroidble2.RxBleClient;

import io.reactivex.plugins.RxJavaPlugins;

/**
 * Created by Administrator on 2018/5/10 0010.
 */

public class AppApplication extends BaseApplication {

    private RxBleClient rxBleClient;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化logger,注意拷贝的话BuildConfig.LOG_DEBUG一定要是在当前module下的包名，配置文件中判断测适和发行版本
        LogUtils.logInit(BuildConfig.LOG_DEBUG);
        rxBleClient = RxBleClient.create(this);
        setRxJavaErrorHandler();
    }

    public static RxBleClient getRxBleClient(Context context) {
        AppApplication application = ((AppApplication) context.getApplicationContext());
        return application.rxBleClient;
    }

    private void setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(throwable -> LogUtils.loge("throw test RxJava2===="+throwable.getMessage()));
    }
}
