package com.vltvplus.ui.movies.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vltvplus.R
import com.vltvplus.data.database.entities.CategoryEntity
import com.vltvplus.data.database.entities.MovieEntity
import com.vltvplus.databinding.ItemContentCardBinding
import com.vltvplus.databinding.ItemCategoryBinding

// ===== Movie Grid Adapter =====
class MovieGridAdapter(
    private val onClick: (MovieEntity) -> Unit
) : ListAdapter<MovieEntity, MovieGridAdapter.VH>(
    object : DiffUtil.ItemCallback<MovieEntity>() {
        override fun areItemsTheSame(a: MovieEntity, b: MovieEntity) = a.streamId == b.streamId
        override fun areContentsTheSame(a: MovieEntity, b: MovieEntity) = a == b
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemContentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val binding: ItemContentCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: MovieEntity) {
            binding.tvTitle.text = movie.name
            Glide.with(binding.root)
                .load(movie.posterPath ?: movie.streamIcon)
                .placeholder(R.drawable.placeholder_poster)
                .error(R.drawable.placeholder_poster)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivPoster)
            binding.root.setOnClickListener { onClick(movie) }
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                binding.focusHighlight.visibility = if (hasFocus)
                    android.view.View.VISIBLE else android.view.View.INVISIBLE
            }
        }
    }
}

// ===== Movie Category Sidebar Adapter =====
class MovieCategoryAdapter(
    private val onClick: (CategoryEntity?) -> Unit
) : ListAdapter<CategoryEntity?, MovieCategoryAdapter.VH>(
    object : DiffUtil.ItemCallback<CategoryEntity?>() {
        override fun areItemsTheSame(a: CategoryEntity?, b: CategoryEntity?) = a?.categoryId == b?.categoryId
        override fun areContentsTheSame(a: CategoryEntity?, b: CategoryEntity?) = a == b
    }
) {
    private var selectedId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: CategoryEntity?) {
            binding.tvCategory.text = category?.categoryName ?: "Todos"
            val selected = category?.categoryId == selectedId
            binding.tvCategory.isSelected = selected
            binding.tvCategory.setTextColor(
                if (selected) binding.root.context.getColor(R.color.vltv_blue)
                else binding.root.context.getColor(R.color.text_secondary)
            )
            binding.root.setOnClickListener {
                selectedId = category?.categoryId
                onClick(category)
                notifyDataSetChanged()
            }
        }
    }
}
