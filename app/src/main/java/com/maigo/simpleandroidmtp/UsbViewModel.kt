package com.maigo.simpleandroidmtp

import android.app.Application
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.mtp.MtpConstants
import android.mtp.MtpDevice
import android.mtp.MtpObjectInfo
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Usb情報用ViewModel
 */
class UsbViewModel(application: Application) : AndroidViewModel(application) {

    // 検出されたUSBデバイスのリスト
    private val _deviceList = MutableLiveData<List<UsbDevice>>()
    val deviceList: LiveData<List<UsbDevice>> = _deviceList

    // MTP接続が確立したデバイス
    private val _connectedDevice = MutableLiveData<MtpDevice?>()
    val connectedDevice: LiveData<MtpDevice?> = _connectedDevice

    private var usbManager: UsbManager? = null

    // 現在のディレクトリにあるファイル/フォルダのリスト
    private val _mtpObjects = MutableLiveData<List<MtpObjectInfo>?>()
    val mtpObjects: LiveData<List<MtpObjectInfo>?> = _mtpObjects

    // 現在のディレクトリパス
    private val _currentPath = MutableLiveData<String>()
    val currentPath: LiveData<String> = _currentPath

    // エラーメッセージ通知用 (任意)
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // パスの階層を管理するためのスタック
    private val pathStack = ArrayDeque<Pair<Int, String>>() // (ObjectHandle, Name)

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
     * MTPデバイスが接続されたときに呼び出す
     */
    fun deviceConnected(device: MtpDevice) {
        _connectedDevice.value = device

        val storageIds = device.storageIds
        if (storageIds?.toList().isNullOrEmpty()) {
            Log.e("UsbViewModel", "No storage found on MTP device.")
            _error.postValue("利用可能なストレージが見つかりません。")
            return
        }

        // fetchObjectsの第3引数で、これが初回（ルート）であることを示す
        fetchObjects(storageIds!![0], 0xFFFFFFFF.toInt(), isEnteringNewFolder = true, folderName = "内部ストレージ")
    }

    /**
     * MTPデバイスが切断されたときに呼び出す
     */
    fun deviceDisconnected() {
        _connectedDevice.value = null
        _mtpObjects.value = null
        pathStack.clear() // 切断時にスタックもクリア
        _currentPath.value = ""
    }

    /**
     * 指定されたストレージIDとobjectHandleを持つオブジェクト（ディレクトリ）の内容を取得する
     * @param storageId ストレージID
     * @param parentObjectHandle 親フォルダのハンドル
     * @param isEnteringNewFolder 新しいフォルダに入るのか、上の階層に戻るのか
     * @param folderName 新しく入るフォルダの名前
     */
    fun fetchObjects(
        storageId: Int,
        parentObjectHandle: Int,
        isEnteringNewFolder: Boolean = false,
        folderName: String? = null
    ) {
        val device = _connectedDevice.value ?: return

        viewModelScope.launch {
            _mtpObjects.postValue(null) // 事前にリストをクリアしてローディング状態を示す
            try {
                val objects = withContext(Dispatchers.IO) {
                    // IOスレッドでMTPデバイスからオブジェクトIDリストを取得
                    val objectHandles = device.getObjectHandles(
                        storageId, // 最初のストレージIDを使用
                        0, // 全てのフォーマット
                        parentObjectHandle // 親オブジェクトのハンドル
                    )

                    // 各IDに対応するオブジェクト情報を取得
                    objectHandles?.toList()?.mapNotNull { handle ->
                        device.getObjectInfo(handle)
                    }
                }
                _mtpObjects.postValue(objects)
                updateCurrentPath(parentObjectHandle, isEnteringNewFolder, folderName)
            } catch (e: Exception) {
                Log.e("UsbViewModel", "Failed to fetch MTP objects", e)
                _error.postValue("ファイルの取得に失敗しました: ${e.message}")
                _mtpObjects.postValue(emptyList()) // エラー時は空リストを通知
            }
        }
    }

    // 1つ上の階層に戻る処理
    fun navigateUp() {
        // スタックに2つ以上（ルート＋どこか）なければ何もしない
        if (pathStack.size <= 1) return

        pathStack.removeLast() // 現在の階層を削除
        val parent = pathStack.last() // 親の階層を取得
        val storageId = _connectedDevice.value?.storageIds?.first() ?: return

        // isEnteringNewFolder = false でfetchObjectsを呼び出す
        fetchObjects(storageId, parent.first, isEnteringNewFolder = false)
    }

    /**
     * 現在のパス表示を更新する
     */
    private fun updateCurrentPath(
        objectHandle: Int,
        isEnteringNewFolder: Boolean,
        folderName: String?
    ) {
        if (isEnteringNewFolder) {
            // 新しいフォルダに入った場合、スタックに追加
            pathStack.addLast(Pair(objectHandle, folderName ?: "..."))
        } else {
            // 上の階層に戻った場合、スタックの整合性を確認（navigateUpで処理済だが念のため）
            if (pathStack.isNotEmpty() && pathStack.last().first != objectHandle) {
                // 予期せぬ状態。スタックをクリアしてルートに戻るのが安全
                pathStack.clear()
                pathStack.addLast(Pair(objectHandle, "内部ストレージ"))
            }
        }

        // スタックからパス文字列を生成してUIに通知
        _currentPath.postValue(pathStack.joinToString(" > ") { it.second })
    }

    /**
     * MTPデバイスとの接続を終了する
     */
    fun closeMtpDevice() {
        _connectedDevice.value?.close()
        _connectedDevice.value = null
    }
}
