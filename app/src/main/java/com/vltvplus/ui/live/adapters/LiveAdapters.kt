package com.vltvplus.ui.live.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vltvplus.R
import com.vltvplus.data.database.entities.CategoryEntity
import com.vltvplus.data.database.entities.ChannelEntity
import com.vltvplus.data.database.entities.EpgCacheEntity
import com.vltvplus.databinding.ItemChannelBinding
import com.vltvplus.databinding.ItemCategoryBinding
import com.vltvplus.databinding.ItemEpgBinding
import java.text.SimpleDateFormat
import java.util.*

class ChannelAdapter(
    private val onChannelClick: (ChannelEntity) -> Unit,
    private val onChannelFocus: (ChannelEntity) -> Unit = {}
) : ListAdapter<ChannelEntity, ChannelAdapter.VH>(
    object : DiffUtil.ItemCallback<ChannelEntity>() {
        override fun areItemsTheSame(a: ChannelEntity, b: ChannelEntity) = a.streamId == b.streamId
        override fun areContentsTheSame(a: ChannelEntity, b: ChannelEntity) = a == b
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val binding: ItemChannelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(channel: ChannelEntity) {
            binding.tvName.text = channel.name
            binding.tvNowPlaying.text = ""

            Glide.with(binding.root)
                .load(channel.streamIcon)
                .placeholder(R.drawable.ic_live_tv)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivLogo)

            binding.root.setOnClickListener { onChannelClick(channel) }
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                binding.focusHighlight.visibility = if (hasFocus)
                    android.view.View.VISIBLE else android.view.View.INVISIBLE
                if (hasFocus) onChannelFocus(channel)
            }
        }
    }
}

class CategoryChipAdapter(
    private val onClick: (CategoryEntity) -> Unit
) : ListAdapter<CategoryEntity, CategoryChipAdapter.VH>(
    object : DiffUtil.ItemCallback<CategoryEntity>() {
        override fun areItemsTheSame(a: CategoryEntity, b: CategoryEntity) = a.categoryId == b.categoryId
        override fun areContentsTheSame(a: CategoryEntity, b: CategoryEntity) = a == b
    }
) {
    private var selectedId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: CategoryEntity) {
            binding.tvCategory.text = category.categoryName
            val selected = category.categoryId == selectedId
            binding.tvCategory.setTextColor(
                if (selected) binding.root.context.getColor(R.color.vltv_blue)
                else binding.root.context.getColor(R.color.text_secondary)
            )
            binding.root.setOnClickListener {
                selectedId = category.categoryId
                onClick(category)
                notifyDataSetChanged()
            }
        }
    }
}

class EpgAdapter : ListAdapter<EpgCacheEntity, EpgAdapter.VH>(
    object : DiffUtil.ItemCallback<EpgCacheEntity>() {
        override fun areItemsTheSame(a: EpgCacheEntity, b: EpgCacheEntity) = a.id == b.id
        override fun areContentsTheSame(a: EpgCacheEntity, b: EpgCacheEntity) = a == b
    }
) {
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemEpgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val binding: ItemEpgBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(epg: EpgCacheEntity) {
            binding.tvTitle.text = epg.title ?: ""
            val start = epg.startTimestamp?.let { sdf.format(Date(it * 1000)) } ?: ""
            val end = epg.stopTimestamp?.let { sdf.format(Date(it * 1000)) } ?: ""
            binding.tvTime.text = "$start - $end"
            binding.tvNowPlaying.visibility = if (epg.nowPlaying)
                android.view.View.VISIBLE else android.view.View.GONE
        }
    }
}
