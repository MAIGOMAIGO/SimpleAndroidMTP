package com.maigo.simpleandroidmtp

import android.mtp.MtpConstants
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.maigo.simpleandroidmtp.databinding.FragmentControlBinding

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!
    private lateinit var fileAdapter: MtpObjectInfoAdapter

    // ActivityとViewModelを共有
    private val usbViewModel: UsbViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // MTPデバイスの接続状態を監視
        usbViewModel.connectedDevice.observe(viewLifecycleOwner) { device ->
            if (device == null) {
                // デバイスが切断されたらConnectFragmentに戻る
                Log.d(
                    MainActivity.TAG,
                    "MTP device disconnected. Navigating back to ConnectFragment."
                )
                findNavController().popBackStack()
            } else {
                // usbViewModel.deviceConnected(device) が既に呼ばれているはず
                Log.d(MainActivity.TAG, "MTP device is connected: ${device.deviceName}")
            }
        }

        // ViewModelでファイルリストの状態を監視
        usbViewModel.mtpObjects.observe(viewLifecycleOwner) { files ->
            binding.progressBar.visibility = View.GONE
            if (files.isNullOrEmpty()) {
                binding.emptyFolderTextView.visibility = View.VISIBLE
                fileAdapter.submitList(emptyList())
            } else {
                binding.emptyFolderTextView.visibility = View.GONE
                fileAdapter.submitList(files)
            }
        }

        // ViewModelで現在のパスを監視
        usbViewModel.currentPath.observe(viewLifecycleOwner) { path ->
            binding.textCurrentPath.text = path
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // ViewModelに上の階層へ移動するよう指示
                usbViewModel.navigateUp()
            }
        })
    }

    private fun setupRecyclerView() {
        fileAdapter = MtpObjectInfoAdapter { mtpObject ->
            // アイテムがクリックされたときの処理
            if (mtpObject.format == MtpConstants.FORMAT_ASSOCIATION) { // フォルダの場合
                // フォルダの中身を取得して表示
                usbViewModel.fetchObjects(
                    storageId = mtpObject.storageId,
                    parentObjectHandle = mtpObject.objectHandle,
                    isEnteringNewFolder = true,
                    folderName = mtpObject.name
                )
            } else {
                // ファイルの場合の処理（ダウンロードなど）
                // TODO: ファイルダウンロード処理を実装
            }
        }
        binding.recyclerViewFiles.adapter = fileAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
