package com.maigo.simpleandroidmtp

import android.mtp.MtpConstants
import android.mtp.MtpObjectInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maigo.simpleandroidmtp.databinding.ListItemFileBinding

class MtpObjectIfoAdapter(
    private val onItemClick: (MtpObjectInfo) -> Unit
) : ListAdapter<MtpObjectInfo, MtpObjectIfoAdapter.MtpObjectInfoViewHolder>(MtpObjectInfoDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MtpObjectInfoViewHolder {
        val binding = ListItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MtpObjectInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MtpObjectInfoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    class MtpObjectInfoViewHolder(private val binding: ListItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mtpObjectInfo: MtpObjectInfo) {
            binding.textFileName.text = mtpObjectInfo.name
            if (mtpObjectInfo.format == MtpConstants.FORMAT_ASSOCIATION) {
                // フォルダの場合のアイコン設定
                binding.iconFileType.setImageResource(R.drawable.folder_24px)
            } else {
                // ファイルの場合のアイコン設定
                binding.iconFileType.setImageResource(R.drawable.draft_24px)
            }
        }
    }

    class MtpObjectInfoDiffCallback : DiffUtil.ItemCallback<MtpObjectInfo>() {
        override fun areItemsTheSame(oldItem: MtpObjectInfo, newItem: MtpObjectInfo): Boolean {
            return oldItem.objectHandle == newItem.objectHandle
        }

        override fun areContentsTheSame(oldItem: MtpObjectInfo, newItem: MtpObjectInfo): Boolean {
            return oldItem.objectHandle == newItem.objectHandle
        }
    }
}