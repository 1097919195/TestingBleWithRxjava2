package com.example.zjl.testingblewithrxjava2.model;


import com.example.zjl.testingblewithrxjava2.app.AppApplication;
import com.example.zjl.testingblewithrxjava2.contract.BleContract;
import com.example.zjl.testingblewithrxjava2.utils.HexStringTwo;
import com.jaydenxiao.common.baserx.RxSchedulers;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanResult;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import io.reactivex.Maybe;
import io.reactivex.Observable;


/**
 * Created by Administrator on 2018/5/10 0010.
 */

public class BleModel implements BleContract.Model {
    private RxBleClient rxBleClient = AppApplication.getRxBleClient(AppApplication.getAppContext());
    public static final String CLOSE_SIGNAL = "486F000000000000000000000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000005461";

    @Override
    public Maybe<RxBleDeviceServices> getBleUUIDWithMac(String macAddress) {
        return rxBleClient.getBleDevice(macAddress)
                .establishConnection(false) //autoConnect flag布尔值：是否直接连接到远程设备（false）或在远程设备变为可用时立即自动连接
                .flatMapSingle(RxBleConnection::discoverServices)
                .firstElement() // Disconnect automatically after discovery
                .compose(RxSchedulers.io_main_maybe());
    }

    @Override
    public Observable<byte[]> setUpNotification(UUID characteristicUUID) {
        return rxBleClient.getBleDevice("7C:EC:79:E5:B3:B4")
                .establishConnection(false)
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(characteristicUUID))
                .flatMap(notificationObservable -> notificationObservable)
                .compose(RxSchedulers.io_main());
    }

    @Override
    public Maybe<byte[]> setUpWrite(UUID characteristicUUID,byte[] data) {
        byte[] bytes = HexStringTwo.hexStringToBytes(CLOSE_SIGNAL);
        byte[] bytes1 = Arrays.copyOfRange(bytes, 0, 20);
        byte[] bytes2 = Arrays.copyOfRange(bytes, 20, 40);
        byte[] bytes3 = Arrays.copyOfRange(bytes, 40, 60);
        ArrayList<byte[]> arrayList = new ArrayList<>();
        arrayList.add(bytes1);
        arrayList.add(bytes2);
        arrayList.add(bytes3);
        return rxBleClient.getBleDevice("7C:EC:79:E5:B3:B4")
                .establishConnection(false)
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(characteristicUUID,bytes))
                .firstElement()
                .compose(RxSchedulers.io_main_maybe());
    }
}
