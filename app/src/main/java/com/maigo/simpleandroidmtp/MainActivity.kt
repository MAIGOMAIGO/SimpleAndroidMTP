package com.maigo.simpleandroidmtp

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.mtp.MtpDevice
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.maigo.simpleandroidmtp.databinding.ActivityMainBinding

/**
 * MTPで遊ぶためのMainActivity
 */
class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SimpleAndroidMtp"
        const val ACTION_USB_PERMISSION = "com.maigo.simpleandroidmtp.ACTION_USB_PERMISSION"
    }

    private lateinit var binding: ActivityMainBinding
    private val usbViewModel: UsbViewModel by viewModels()

    private val usbManager by lazy {
        getSystemService(Context.USB_SERVICE) as UsbManager
    }

    // usb用intent受信用
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val device: UsbDevice? = intent.toUsbDevice()
                        if (usbManager.hasPermission(device)) {
                            device?.let {
                                Log.d(TAG, "Permission granted for device. Starting MTP.")
                                startMtpDevice(it)
                            }
                        } else {
                            Log.d(TAG, "Permission denied for device $device")
                        }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    Log.d(TAG, "USB device attached.")
                    checkForConnectedDevices()
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    Log.d(TAG, "USB device detached.")
                    // ViewModelに切断を通知
                    val detachedDevice = intent.toUsbDevice()
                    detachedDevice?.let { device ->
                        if (usbViewModel.connectedDevice.value?.deviceId == device.deviceId) {
                            usbViewModel.closeMtpDevice()
                        }
                    }
                    checkForConnectedDevices()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUsbReceiver()
        checkForConnectedDevices()
        usbViewModel.setUsbManager(usbManager)
    }

    override fun onDestroy() {
        // ブロードキャスト受信終了
        unregisterReceiver(usbReceiver)
        super.onDestroy()
    }

    /**
     * usbレシーバーセットアップ
     */
    private fun setupUsbReceiver() {
        // フィルター用意
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(usbReceiver, filter)
        }
    }

    /**
     * 接続済みusbDeviceを確認
     */
    private fun checkForConnectedDevices() {
        val deviceList = usbManager.deviceList.values.toList()
        usbViewModel.updateDeviceList(deviceList)

        // パーミッション済みのデバイスがあれば接続試行
        deviceList.find { usbManager.hasPermission(it) }?.let {
            startMtpDevice(it)
        }
    }

    /**
     * usbDeviceの個別なパーミッションを取得
     */
    fun requestUsbPermission(device: UsbDevice) {
        val intent = Intent(ACTION_USB_PERMISSION).apply {
            putExtra(UsbManager.EXTRA_DEVICE, device)
        }
        val permissionIntent = PendingIntent.getBroadcast(
            this, device.deviceId, intent, PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    /**
     * MTPデバイスとの接続を開始する
     */
    fun startMtpDevice(usbDevice: UsbDevice) {
        val mtpDevice = MtpDevice(usbDevice)
        val usbConnection = usbManager.openDevice(usbDevice)
        if (usbConnection == null) {
            Toast.makeText(this, "Failed to open USB connection", Toast.LENGTH_SHORT).show()
        } else {
            if (mtpDevice.open(usbConnection)) {
                usbViewModel.setConnectedMtpDevice(mtpDevice)
            } else {
               Toast.makeText(this, "Failed to open MTP device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Intentから [UsbDevice] をバージョンに合わせて取得
     * @return 取得した [UsbDevice]。存在しない場合はnull。
     */
    private fun Intent.toUsbDevice(): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }
}
