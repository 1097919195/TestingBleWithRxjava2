package com.example.zjl.testingblewithrxjava2.presenter;

import com.example.zjl.testingblewithrxjava2.app.AppConstant;
import com.example.zjl.testingblewithrxjava2.contract.BleContract;

import com.example.zjl.testingblewithrxjava2.utils.HexString;
import com.example.zjl.testingblewithrxjava2.utils.HexStringTwo;
import com.jaydenxiao.common.baserx.RxSubscriber;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.polidea.rxandroidble2.RxBleDeviceServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/5/10 0010.
 */

public class BlePresenter extends BleContract.Presenter{

    @Override
    public void getBleUUIDWithMac(String macAddress) {
        mRxManage.add(mModel.getBleUUIDWithMac(macAddress)
                .subscribe(deviceServices -> {
                    mView.returnGetBleUUIDWithMac(deviceServices,macAddress);
                },e -> {mView.showErrorTip("connectFail");
                    LogUtils.loge(e.getCause().toString());}));

    }

    @Override
    public void setUpNotificationRequest(UUID characteristicUUID) {
        mRxManage.add(mModel.setUpNotification(characteristicUUID)
                .subscribeWith(new RxSubscriber<byte[]>(mContext,false) {
                    @Override
                    protected void _onNext(byte[] bytes) {
                        String s = HexString.bytesToHex(bytes);
                        if (s.length() == AppConstant.STANDARD_LENGTH) {
                            int code = Integer.parseInt("8D6A", 16);
                            int length = Integer.parseInt(s.substring(0, 4), 16);
                            int angle = Integer.parseInt(s.substring(4, 8), 16);
                            int battery = Integer.parseInt(s.substring(8, 12), 16);
                            int a1 = length ^ code;
                            int a2 = angle ^ code;
                            int a3 = battery ^ code;
                            a1 += AppConstant.ADJUST_VALUE;
                            mView.returnSetUpNotification(Float.valueOf(a1) / 10, Float.valueOf(a2) / 10, a3);
                        }

                    }

                    @Override
                    protected void _onError(String message) {
//                        mView.showErrorTip("连接通讯失败！");

                    }
                }));
    }

    @Override
    public void setUpWriteRequest(UUID characteristicUUID,byte[] data) {
        mRxManage.add(mModel.setUpWrite(characteristicUUID, data)
                .subscribe(
                        bytes -> {
                            mView.returnSetUpWrite(bytes);
                        }
                ));
    }

}
