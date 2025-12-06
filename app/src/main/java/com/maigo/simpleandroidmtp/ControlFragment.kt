package com.maigo.simpleandroidmtp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.maigo.simpleandroidmtp.databinding.FragmentControlBinding

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!

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

        // MTPデバイスの接続状態を監視
        usbViewModel.connectedDevice.observe(viewLifecycleOwner) { device ->
            if (device == null) {
                // デバイスが切断されたらConnectFragmentに戻る
                Log.d(MainActivity.TAG, "MTP device disconnected. Navigating back to ConnectFragment.")
                findNavController().popBackStack()
            } else {
                // デバイスが接続されているので、ファイルリストなどを取得・表示する
                Log.d(MainActivity.TAG, "MTP device is connected: ${device.deviceName}")
                // TODO: MTPデバイスのルートオブジェクトを取得し、ファイルリストを表示する処理を実装
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
