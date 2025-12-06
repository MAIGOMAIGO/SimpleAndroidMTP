package com.maigo.simpleandroidmtp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.maigo.simpleandroidmtp.databinding.FragmentConnectBinding

/**
 * 接続管理画面
 */
class ConnectFragment : Fragment() {
    // databinding
    private var _binding: FragmentConnectBinding? = null
    private val binding get() = _binding!!

    // ActivityスコープのViewModelを共有
    private val usbViewModel: UsbViewModel by activityViewModels()
    private lateinit var deviceListAdapter: DeviceListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerViewのアダプターを設定
        setupRecyclerView()

        // ViewModelの状態を監視
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // アダプターを初期化。リスト項目クリック時の処理を定義
        deviceListAdapter = DeviceListAdapter { device ->
            // MainActivityのメソッドを呼び出してパーミッションを要求
            (activity as? MainActivity)?.requestUsbPermission(device)
        }

        binding.deviceListRecyclerView.apply {
            adapter = deviceListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        // 接続状態を監視
        usbViewModel.connectedDevice.observe(viewLifecycleOwner) { device ->
            if (device != null) {
                val action = ConnectFragmentDirections.actionConnectFragmentToControlFragment(device.deviceId)
                findNavController().navigate(action)
            }
        }

        // 検出されたデバイスリストを監視
        usbViewModel.deviceList.observe(viewLifecycleOwner) { devices ->
            if (devices.isNullOrEmpty()) {
                binding.noDeviceTextView.visibility = View.VISIBLE
                binding.deviceListRecyclerView.visibility = View.GONE
            } else {
                binding.noDeviceTextView.visibility = View.GONE
                binding.deviceListRecyclerView.visibility = View.VISIBLE
                // RecyclerViewのアダプターにリストを渡す
                deviceListAdapter.submitList(devices)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
