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
     * MTPデバイスとの接続を開始する
     */
    fun startMtpDevice(usbDevice: UsbDevice) {
        val mtpDevice = MtpDevice(usbDevice)
        val usbConnection = usbManager?.openDevice(usbDevice)
        if (usbConnection == null) {
            // TODO: 接続失敗時のエラーハンドリング
        } else {
            if (mtpDevice.open(usbConnection)) {
                _connectedDevice.value = mtpDevice
            } else {
                // TODO: 接続失敗時のエラーハンドリング
            }
        }
    }

    /**
     * 接続済みデバイスの情報をクリアする。
     * 画面遷移の完了後に呼び出すことで、意図しない再遷移を防ぐ。
     */
    fun clearConnectedDevice() {
        _connectedDevice.value = null
    }

    /**
     * MTPデバイスとの接続を終了する
     */
    fun closeMtpDevice() {
        _connectedDevice.value?.close()
        _connectedDevice.value = null
    }
}
