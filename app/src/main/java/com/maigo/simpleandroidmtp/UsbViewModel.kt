package com.maigo.simpleandroidmtp

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.mtp.MtpDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Usb情報用ViewModel
 */
class UsbViewModel : ViewModel() {

    // 検出されたUSBデバイスのリスト
    private val _deviceList = MutableLiveData<List<UsbDevice>>()
    val deviceList: LiveData<List<UsbDevice>> = _deviceList

    // MTP接続が確立したデバイス
    private val _connectedDevice = MutableLiveData<MtpDevice?>()
    val connectedDevice: LiveData<MtpDevice?> = _connectedDevice

    private var usbManager: UsbManager? = null

    /**
     * UsbManager設置処理
     */
    fun setUsbManager(usbManager: UsbManager?) {
        this.usbManager = usbManager
    }

    /**
     * 検出されたUSBデバイスのリストを更新する
     */
    fun updateDeviceList(devices: List<UsbDevice>) {
        _deviceList.value = devices
    }

    /**
     * 接続したMTPデバイスの設置
     */
    fun setConnectedMtpDevice(device: MtpDevice?) {
        _connectedDevice.value = device
    }

    /**
     * MTPデバイスとの接続を終了する
     */
    fun closeMtpDevice() {
        _connectedDevice.value?.close()
        _connectedDevice.value = null
    }
}
