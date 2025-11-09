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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SimpleAndroidMtp"
        const val ACTION_USB_PERMISSION = "com.maigo.simpleandroidmtp.ACTION_USB_PERMISSION"
    }

    private val usbManager by lazy {
        getSystemService(Context.USB_SERVICE) as UsbManager
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_USB_PERMISSION -> {
                    val device = intent.toUsbDevice()
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let { onUsbDeviceConnected(it) }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = intent.toUsbDevice()
                    device?.let { requestPermission(it) }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = intent.toUsbDevice()
                    // detachedのログ用
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // filter設定
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        // USBブロードキャスト登録
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag") // 旧バージョン向けの処理のため警告を抑制
            registerReceiver(usbReceiver, filter)
        }

        // 起動時に接続済みデバイス確認
        checkConnectedDevices(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // USBデバイス接続でアプリが起動した場合にも対応
        checkConnectedDevices(intent)
    }

    /**
     * 接続しているusbDevice検索
     */
    private fun checkConnectedDevices(intent: Intent) {
        val device = intent.toUsbDevice()
        if (device != null) {
            requestPermission(device)
        } else {
            // 既に接続されているデバイス一覧を確認
            usbManager.deviceList.values.forEach { requestPermission(it) }
        }
    }

    /**
     * usbDeviceの個別なパーミッションを取得
     */
    private fun requestPermission(device: UsbDevice) {
        if (!usbManager.hasPermission(device)) {
            val permissionIntent = PendingIntent.getBroadcast(
                this, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
            )
            usbManager.requestPermission(device, permissionIntent)
        } else {
            onUsbDeviceConnected(device)
        }
    }

    /**
     * usbDeviceと接続した後の処理
     * TODO: mtpDeviceの処理に繋げる予定
     * @param device 接続できそうな[UsbDevice]
     */
    private fun onUsbDeviceConnected(device: UsbDevice) {
         val mtpDevice = MtpDevice(device)
         val connection = usbManager.openDevice(device)
         mtpDevice.open(connection)
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
