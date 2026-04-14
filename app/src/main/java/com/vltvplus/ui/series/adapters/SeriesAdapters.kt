package com.vltvplus.ui.series.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vltvplus.R
import com.vltvplus.data.database.entities.CategoryEntity
import com.vltvplus.data.database.entities.SeriesEntity
import com.vltvplus.databinding.ItemContentCardBinding
import com.vltvplus.databinding.ItemCategoryBinding

class SeriesGridAdapter(
    private val onClick: (SeriesEntity) -> Unit
) : ListAdapter<SeriesEntity, SeriesGridAdapter.VH>(
    object : DiffUtil.ItemCallback<SeriesEntity>() {
        override fun areItemsTheSame(oldItem: SeriesEntity, newItem: SeriesEntity) = oldItem.seriesId == newItem.seriesId
        override fun areContentsTheSame(oldItem: SeriesEntity, newItem: SeriesEntity) = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemContentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val binding: ItemContentCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(series: SeriesEntity) {
            binding.tvTitle.text = series.name
            Glide.with(binding.root)
                .load(series.posterPath ?: series.cover)
                .placeholder(R.drawable.placeholder_poster)
                .error(R.drawable.placeholder_poster)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivPoster)
            binding.root.setOnClickListener { onClick(series) }
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                binding.focusHighlight.visibility = if (hasFocus)
                    android.view.View.VISIBLE else android.view.View.INVISIBLE
            }
        }
    }
}

class SeriesCategoryAdapter(
    private val onClick: (CategoryEntity?) -> Unit
) : ListAdapter<CategoryEntity, SeriesCategoryAdapter.VH>(
    object : DiffUtil.ItemCallback<CategoryEntity>() {
        // Correção: Removido o '?' dos parâmetros para garantir que o compilador reconheça o override
        override fun areItemsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity) = oldItem.categoryId == newItem.categoryId
        override fun areContentsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity) = oldItem == newItem
    }
) {
    private var selectedId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: CategoryEntity?) {
            binding.tvCategory.text = category?.categoryName ?: "Todas"
            val selected = category?.categoryId == selectedId
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
