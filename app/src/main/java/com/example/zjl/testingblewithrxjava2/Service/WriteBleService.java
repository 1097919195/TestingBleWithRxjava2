package com.example.zjl.testingblewithrxjava2.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zjl.testingblewithrxjava2.app.AppApplication;
import com.example.zjl.testingblewithrxjava2.app.AppConstant;
import com.example.zjl.testingblewithrxjava2.utils.HexString;
import com.example.zjl.testingblewithrxjava2.utils.HexStringTwo;
import com.jaydenxiao.common.baserx.RxSchedulers;
import com.jaydenxiao.common.baserx.RxSubscriber;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.polidea.rxandroidble2.RxBleClient;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Administrator on 2018/7/2 0002.
 */

public class WriteBleService extends Service {
    private RxBleClient rxBleClient = AppApplication.getRxBleClient(AppApplication.getAppContext());
    String uuidStringWrite = "0000fff3-0000-1000-8000-00805f9b34fb";
    UUID uuidWrite = null;
    String CLOSE_SIGNAL = "";
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBroadcastReceiver();
    }

    private void initBroadcastReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction("android.intent.action.MY_BROADCAST_LISTENER");
        registerReceiver(mReceiver, mFilter);// 开机启动时首次开启服务后注册接收器
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.MY_BROADCAST_LISTENER")) {
                LogUtils.loge("服务监听广播...");
                CLOSE_SIGNAL = intent.getStringExtra(AppConstant.WRITE_BYTE);
                uuidWrite = UUID.fromString(uuidStringWrite);
//                byte[] bytes = HexStringTwo.hexStringToBytes(String.valueOf((int)Math.round(Math.random()*100)));
                byte[] bytes = HexStringTwo.hexStringToBytes("1234567890");
                byte[] bytes1 = Arrays.copyOfRange(bytes, 0, 1);
                byte[] bytes2 = Arrays.copyOfRange(bytes, 2, 3);
                byte[] bytes3 = Arrays.copyOfRange(bytes, 4, 5);
                LogUtils.loge("length: "+bytes1.length);
                ArrayList<byte[]> arrayList = new ArrayList<>();
                arrayList.add(bytes1);
                arrayList.add(bytes2);
                arrayList.add(bytes3);

                DisposableObserver<Long> disposableObserver = new DisposableObserver<Long>() {
                    @Override
                    public void onNext(Long l) {
                        int i = l.intValue();
                        if (i == arrayList.size()) {
                            disposable.clear();
                            LogUtils.loge("complete");
                        } else {
                            rxBleClient.getBleDevice("7C:EC:79:E5:B3:B4")
                                    .establishConnection(false)
                                    .takeUntil(disconnectTriggerSubject)
                                    .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(uuidWrite, arrayList.get(i)))
//                                    .firstElement()
                                    .subscribe(
                                            by ->
                                            {
                                                LogUtils.loge("write=======" +HexString.bytesToHex(by));
                                                disconnectTriggerSubject.onNext(true);
                                            }
                                            , e -> LogUtils.loge(i+" times "+e.toString())

                                    );
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                };

                Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                        .compose(RxSchedulers.io_main())
                        .subscribe(disposableObserver);
                disposable.add(disposableObserver);


            } else {
                LogUtils.loge("监听广播失败");
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
