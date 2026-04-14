package com.vltvplus.ui.detail.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vltvplus.R
import com.vltvplus.data.database.entities.EpisodeEntity
import com.vltvplus.databinding.ItemEpisodeBinding
import com.vltvplus.databinding.ItemSeasonTabBinding

class EpisodeListAdapter(
    private val onClick: (EpisodeEntity) -> Unit
) : ListAdapter<EpisodeEntity, EpisodeListAdapter.VH>(
    object : DiffUtil.ItemCallback<EpisodeEntity>() {
        override fun areItemsTheSame(a: EpisodeEntity, b: EpisodeEntity) = a.episodeId == b.episodeId
        override fun areContentsTheSame(a: EpisodeEntity, b: EpisodeEntity) = a == b
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, pos: Int) = holder.bind(getItem(pos))

    inner class VH(private val b: ItemEpisodeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(ep: EpisodeEntity) {
            b.tvEpisodeNum.text = "E${ep.episodeNum}"
            b.tvTitle.text = ep.title ?: "Episódio ${ep.episodeNum}"
            b.tvOverview.text = ep.plot ?: ""
            b.tvDuration.text = ep.duration ?: ep.durationSecs?.let { "${it / 60}min" } ?: ""

            val imageUrl = ep.stillPath ?: ep.movieImage ?: ep.coverBig
            Glide.with(b.root).load(imageUrl)
                .placeholder(R.drawable.placeholder_episode)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(b.ivThumbnail)

            b.root.setOnClickListener { onClick(ep) }
            b.root.setOnFocusChangeListener { v, hasFocus ->
                v.alpha = if (hasFocus) 1f else 0.85f
            }
        }
    }
}

class SeasonTabAdapter(
    private val onSelect: (Int) -> Unit
) : ListAdapter<Int, SeasonTabAdapter.VH>(
    object : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(a: Int, b: Int) = a == b
        override fun areContentsTheSame(a: Int, b: Int) = a == b
    }
) {
    private var selectedSeason = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemSeasonTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, pos: Int) = holder.bind(getItem(pos))

    inner class VH(private val b: ItemSeasonTabBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(season: Int) {
            b.tvSeason.text = b.root.context.getString(R.string.season_label, season)
            val selected = season == selectedSeason
            b.tvSeason.isSelected = selected
            b.tvSeason.setTextColor(
                if (selected) b.root.context.getColor(R.color.vltv_blue)
                else b.root.context.getColor(R.color.text_secondary)
            )
            b.root.setOnClickListener {
                selectedSeason = season
                onSelect(season)
                notifyDataSetChanged()
            }
        }
    }
}
