package com.vltvplus.ui.series

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vltvplus.R
import com.vltvplus.databinding.FragmentSeriesBinding
import com.vltvplus.ui.series.adapters.SeriesGridAdapter
import com.vltvplus.ui.series.adapters.SeriesCategoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SeriesFragment : Fragment() {

    private var _binding: FragmentSeriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SeriesViewModel by viewModels()

    private lateinit var categoryAdapter: SeriesCategoryAdapter
    private lateinit var seriesAdapter: SeriesGridAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSeriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        observeData()
        setupSearch()
    }

    private fun setupAdapters() {
        categoryAdapter = SeriesCategoryAdapter { category ->
            viewModel.selectCategory(category?.categoryId)
            binding.tvSelectedCategory.text = category?.categoryName ?: "Todas as Séries"
        }
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
        }

        seriesAdapter = SeriesGridAdapter { series ->
            findNavController().navigate(
                R.id.seriesDetailActivity,
                bundleOf("seriesId" to series.seriesId)
            )
        }
        binding.rvSeries.apply {
            layoutManager = GridLayoutManager(context, getGridSpanCount())
            adapter = seriesAdapter
        }
    }

    private fun getGridSpanCount(): Int {
        val dp = resources.displayMetrics.widthPixels / resources.displayMetrics.density
        return when {
            dp >= 840 -> 5
            dp >= 600 -> 4
            else -> 3
        }
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.searchFragment,
                bundleOf("searchType" to "series"))
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { cats ->
                categoryAdapter.submitList(listOf(null) + cats)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.series.collect { list ->
                seriesAdapter.submitList(list)
                binding.tvSeriesCount.text = "${list.size} séries"
                binding.shimmerSeries.stopShimmer()
                binding.shimmerSeries.visibility = View.GONE
                binding.rvSeries.visibility = View.VISIBLE
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                if (loading) {
                    binding.shimmerSeries.startShimmer()
                    binding.shimmerSeries.visibility = View.VISIBLE
                    binding.rvSeries.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
