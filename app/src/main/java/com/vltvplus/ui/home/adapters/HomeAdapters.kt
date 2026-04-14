package com.vltvplus.ui.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vltvplus.R
import com.vltvplus.data.database.entities.MovieEntity
import com.vltvplus.data.database.entities.SeriesEntity
import com.vltvplus.data.database.entities.WatchProgressEntity
import com.vltvplus.databinding.ItemContentCardBinding
import com.vltvplus.databinding.ItemContinueWatchingBinding

// ===== Generic content card item =====
data class ContentItem(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val type: String // "movie" or "series"
)

// ===== Content Row Adapter (horizontal scroll) =====
class ContentRowAdapter(
    private val type: String,
    private val onClick: (ContentItem) -> Unit
) : RecyclerView.Adapter<ContentRowAdapter.VH>() {

    private val items = mutableListOf<ContentItem>()

    fun submitMovies(movies: List<MovieEntity>) {
        items.clear()
        items.addAll(movies.map { ContentItem(it.streamId, it.name, it.posterPath ?: it.streamIcon, "movie") })
        notifyDataSetChanged()
    }

    fun submitSeries(series: List<SeriesEntity>) {
        items.clear()
        items.addAll(series.map { ContentItem(it.seriesId, it.name, it.posterPath ?: it.cover, "series") })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemContentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    inner class VH(private val binding: ItemContentCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContentItem) {
            binding.tvTitle.text = item.title
            Glide.with(binding.root)
                .load(item.posterUrl)
                .placeholder(R.drawable.placeholder_poster)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivPoster)
            binding.root.setOnClickListener { onClick(item) }

            // TV focus handling
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                binding.focusHighlight.visibility = if (hasFocus)
                    android.view.View.VISIBLE else android.view.View.INVISIBLE
            }
        }
    }
}

// ===== Featured Banner Adapter =====
class FeaturedBannerAdapter(
    private val onMovieClick: (MovieEntity) -> Unit,
    private val onSeriesClick: (SeriesEntity) -> Unit
) : RecyclerView.Adapter<FeaturedBannerAdapter.VH>() {

    private val movies = mutableListOf<MovieEntity>()

    fun submitMovies(list: List<MovieEntity>) {
        movies.clear()
        movies.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_featured_banner, parent, false)
        return VH(view)
    }

    override fun getItemCount() = movies.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(movies[position])

    inner class VH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val ivBackdrop = itemView.findViewById<android.widget.ImageView>(R.id.iv_backdrop)
        private val tvTitle = itemView.findViewById<android.widget.TextView>(R.id.tv_title)
        private val tvGenre = itemView.findViewById<android.widget.TextView>(R.id.tv_genre)
        private val btnWatch = itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_watch)

        fun bind(movie: MovieEntity) {
            tvTitle.text = movie.name
            tvGenre.text = movie.genres ?: ""
            Glide.with(itemView)
                .load(movie.backdropPath ?: movie.streamIcon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivBackdrop)
            btnWatch.setOnClickListener { onMovieClick(movie) }
            itemView.setOnClickListener { onMovieClick(movie) }
        }
    }
}

// ===== Continue Watching Adapter =====
class ContinueWatchingAdapter(
    private val onClick: (WatchProgressEntity) -> Unit
) : ListAdapter<WatchProgressEntity, ContinueWatchingAdapter.VH>(
    object : DiffUtil.ItemCallback<WatchProgressEntity>() {
        override fun areItemsTheSame(a: WatchProgressEntity, b: WatchProgressEntity) = a.contentId == b.contentId
        override fun areContentsTheSame(a: WatchProgressEntity, b: WatchProgressEntity) = a == b
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemContinueWatchingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val binding: ItemContinueWatchingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WatchProgressEntity) {
            binding.tvTitle.text = item.title ?: ""
            Glide.with(binding.root)
                .load(item.thumbnailUrl)
                .placeholder(R.drawable.placeholder_poster)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivThumbnail)

            val progress = if (item.duration > 0) ((item.position * 100) / item.duration).toInt() else 0
            binding.progressBar.progress = progress

            if (item.seasonNumber != null && item.episodeNumber != null) {
                binding.tvEpisodeBadge.text = "T${item.seasonNumber} E${item.episodeNumber}"
                binding.tvEpisodeBadge.visibility = android.view.View.VISIBLE
            } else {
                binding.tvEpisodeBadge.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
