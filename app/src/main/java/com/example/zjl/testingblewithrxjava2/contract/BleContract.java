package com.example.zjl.testingblewithrxjava2.contract;

import com.jaydenxiao.common.base.BaseModel;
import com.jaydenxiao.common.base.BasePresenter;
import com.jaydenxiao.common.base.BaseView;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.UUID;

import io.reactivex.Maybe;
import io.reactivex.Observable;


/**
 * Created by Administrator on 2018/5/10 0010.
 */

public interface BleContract {
    interface Model extends BaseModel {
        Maybe<RxBleDeviceServices> getBleUUIDWithMac(String macAddress);

        Observable<byte[]> setUpNotification(UUID characteristicUUID);

        Maybe<byte[]> setUpWrite(UUID characteristicUUID,byte[] data);
    }

    interface View extends BaseView {
        void returnGetBleUUIDWithMac(RxBleDeviceServices deviceServices, String macAddress);

        void returnSetUpNotification(Float length, Float angle, int battery);

        void returnSetUpWrite(byte[] bytes);
    }

    abstract class Presenter extends BasePresenter<View, Model> {
        public abstract void getBleUUIDWithMac(String macAddress);

        public abstract void setUpNotificationRequest(UUID characteristicUUID);

        public abstract void setUpWriteRequest(UUID characteristicUUID,byte[] data);
    }
}
