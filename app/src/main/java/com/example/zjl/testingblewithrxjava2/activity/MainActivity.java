package com.example.zjl.testingblewithrxjava2.activity;


import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.zjl.testingblewithrxjava2.R;
import com.example.zjl.testingblewithrxjava2.Service.WriteBleService;
import com.example.zjl.testingblewithrxjava2.app.AppApplication;
import com.example.zjl.testingblewithrxjava2.app.AppConstant;
import com.example.zjl.testingblewithrxjava2.contract.BleContract;
import com.example.zjl.testingblewithrxjava2.model.BleModel;
import com.example.zjl.testingblewithrxjava2.presenter.BlePresenter;
import com.example.zjl.testingblewithrxjava2.utils.HexString;
import com.example.zjl.testingblewithrxjava2.utils.HexStringTwo;
import com.jaydenxiao.common.base.BaseActivity;
import com.jaydenxiao.common.baserx.RxSchedulers;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.jaydenxiao.common.commonutils.ToastUtil;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends BaseActivity<BlePresenter,BleModel> implements BleContract.View {
    @BindView(R.id.btnBle)
    Button btnBle;
    @BindView(R.id.text2)
    TextView text2;
    @BindView(R.id.notification)
    Button notification;
    @BindView(R.id.write)
    Button write;
    @BindView(R.id.editText)
    EditText editText;

    String uuidStringNotification = "0000fff4-0000-1000-8000-00805f9b34fb";
    String uuidStringWrite = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static final String CLOSE_SIGNAL = "000000000000000000000000000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000000000000";

    UUID uuidNotification = null;
    UUID uuidWrite = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initPresenter() {
        mPresenter.setVM(this, mModel);
    }

    @Override
    public void initView() {
        uuidNotification = UUID.fromString(uuidStringNotification);
        uuidWrite = UUID.fromString(uuidStringWrite);
        startServiceWithWriteBle();
        initListener();
    }

    //初始化建立服务监听蓝牙写入操作
    private void startServiceWithWriteBle() {
        Intent intent = new Intent(MainActivity.this, WriteBleService.class);
        startService(intent);
    }

    private void initListener() {
        btnBle.setOnClickListener(v->{
            ToastUtil.showShort("onClick");
            text2.setText("start...");
            mPresenter.getBleUUIDWithMac("7C:EC:79:E5:B3:B4");
        });
        notification.setOnClickListener(v->{
            ToastUtil.showShort("onClick");
            text2.setText("start...");
            mPresenter.setUpNotificationRequest(uuidNotification);
        });
        //FIXME 服务中操作还是活动中操作二选一
        write.setOnClickListener(v->{
//            ToastUtil.showShort("onClick");
//            if (editText.getEditableText().length()>0) {
//                Intent broadcast = new Intent();
//                broadcast.setAction("android.intent.action.MY_BROADCAST_LISTENER");
////                broadcast.putExtra(AppConstant.WRITE_BYTE, editText.getEditableText().toString());
//                broadcast.putExtra(AppConstant.WRITE_BYTE, CLOSE_SIGNAL);
//                sendBroadcast(broadcast);
//            }else {
//                ToastUtil.showShort("请先在文本中输入要写入的数据");
//            }

            startWriteByte();

        });
    }

    private void startWriteByte() {
        RxBleClient rxBleClient = AppApplication.getRxBleClient(AppApplication.getAppContext());
        String uuidStringWrite = "0000fff3-0000-1000-8000-00805f9b34fb";
        final UUID uuidWrite = UUID.fromString(uuidStringWrite);
        PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
        CompositeDisposable disposable = new CompositeDisposable();
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
                                        LogUtils.loge("write=======" + HexString.bytesToHex(by));
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
    }

    @Override
    public void returnSetUpNotification(Float length, Float angle, int battery) {
        ToastUtil.showShort("测量长度"+length);
    }

    @Override
    public void returnSetUpWrite(byte[] bytes) {
        ToastUtil.showShort("写入成功");
    }

    @Override
    public void returnGetBleUUIDWithMac(RxBleDeviceServices deviceServices, String macAddress) {
        text2.setText(macAddress);
        ToastUtil.showShort("OK");
    }

    @Override
    public void showLoading(String title) {

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public void showErrorTip(String msg) {
        text2.setText(msg);
    }
}
