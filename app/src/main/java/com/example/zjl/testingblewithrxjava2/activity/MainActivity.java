package com.example.zjl.testingblewithrxjava2.activity;


import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.example.zjl.testingblewithrxjava2.R;
import com.example.zjl.testingblewithrxjava2.Service.WriteBleService;
import com.example.zjl.testingblewithrxjava2.contract.BleContract;
import com.example.zjl.testingblewithrxjava2.model.BleModel;
import com.example.zjl.testingblewithrxjava2.presenter.BlePresenter;
import com.example.zjl.testingblewithrxjava2.utils.HexStringTwo;
import com.jaydenxiao.common.base.BaseActivity;
import com.jaydenxiao.common.commonutils.ToastUtil;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.UUID;

import butterknife.BindView;

public class MainActivity extends BaseActivity<BlePresenter,BleModel> implements BleContract.View {
    @BindView(R.id.btnBle)
    Button btnBle;
    @BindView(R.id.text2)
    TextView text2;
    @BindView(R.id.notification)
    Button notification;
    @BindView(R.id.write)
    Button write;

    String uuidStringNotification = "0000fff4-0000-1000-8000-00805f9b34fb";
    String uuidStringWrite = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static final String CLOSE_SIGNAL = "486F000000000000000000000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000005461";

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
        write.setOnClickListener(v->{
            ToastUtil.showShort("onClick");
            Intent broadcast = new Intent();
            broadcast.setAction("android.intent.action.MY_BROADCAST_LISTENER");
            sendBroadcast(broadcast);
        });

        text2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WriteBleService.class);
            startService(intent);
        });
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
