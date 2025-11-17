package com.maigo.simpleandroidmtp

import android.hardware.usb.UsbDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maigo.simpleandroidmtp.databinding.ListItemDeviceBinding

/**
 * デバイス一覧表示用アダプター
 */
class DeviceListAdapter(
    private val onItemClicked: (UsbDevice) -> Unit
) : ListAdapter<UsbDevice, DeviceListAdapter.DeviceViewHolder>(DeviceDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ListItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = getItem(position)
        holder.bind(device)
        holder.itemView.setOnClickListener {
            onItemClicked(device)
        }
    }

    // ViewHolder
    class DeviceViewHolder(private val binding: ListItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: UsbDevice) {
            binding.deviceNameTextView.text = device.productName ?: "Unknown Device"
            val deviceIdText = "Device ID: ${device.deviceId}"
            binding.deviceIdTextView.text = deviceIdText
        }
    }

    // DiffUtil
    object DeviceDiffCallback : DiffUtil.ItemCallback<UsbDevice>() {
        override fun areItemsTheSame(oldItem: UsbDevice, newItem: UsbDevice): Boolean {
            return oldItem.deviceId == newItem.deviceId
        }

        override fun areContentsTheSame(oldItem: UsbDevice, newItem: UsbDevice): Boolean {
            return oldItem == newItem
        }
    }
}
