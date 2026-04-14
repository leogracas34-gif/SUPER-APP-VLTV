package com.vltvplus.ui.search.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vltvplus.R
import com.vltvplus.data.database.entities.ChannelEntity
import com.vltvplus.data.database.entities.MovieEntity
import com.vltvplus.data.database.entities.SeriesEntity
import com.vltvplus.databinding.ItemContentCardBinding
import com.vltvplus.ui.search.SearchResults

class SearchResultAdapter(
    private val onMovieClick: (MovieEntity) -> Unit,
    private val onSeriesClick: (SeriesEntity) -> Unit,
    private val onChannelClick: (ChannelEntity) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.VH>() {

    private val items = mutableListOf<Any>() // MovieEntity | SeriesEntity | ChannelEntity

    fun submitResults(results: SearchResults) {
        items.clear()
        items.addAll(results.movies)
        items.addAll(results.series)
        items.addAll(results.channels)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemContentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    inner class VH(private val b: ItemContentCardBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Any) {
            when (item) {
                is MovieEntity -> {
                    b.tvTitle.text = item.name
                    Glide.with(b.root).load(item.posterPath ?: item.streamIcon)
                        .placeholder(R.drawable.placeholder_poster)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(b.ivPoster)
                    b.root.setOnClickListener { onMovieClick(item) }
                }
                is SeriesEntity -> {
                    b.tvTitle.text = item.name
                    Glide.with(b.root).load(item.posterPath ?: item.cover)
                        .placeholder(R.drawable.placeholder_poster)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(b.ivPoster)
                    b.root.setOnClickListener { onSeriesClick(item) }
                }
                is ChannelEntity -> {
                    b.tvTitle.text = item.name
                    Glide.with(b.root).load(item.streamIcon)
                        .placeholder(R.drawable.ic_live_tv)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(b.ivPoster)
                    b.root.setOnClickListener { onChannelClick(item) }
                }
            }
            b.root.setOnFocusChangeListener { _, hasFocus ->
                b.focusHighlight.visibility = if (hasFocus) android.view.View.VISIBLE else android.view.View.INVISIBLE
            }
        }
    }
}
