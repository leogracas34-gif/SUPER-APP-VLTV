package com.vltvplus.ui.movies

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
import com.vltvplus.databinding.FragmentMoviesBinding
import com.vltvplus.ui.movies.adapters.MovieGridAdapter
import com.vltvplus.ui.movies.adapters.MovieCategoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MoviesFragment : Fragment() {

    private var _binding: FragmentMoviesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MoviesViewModel by viewModels()

    private lateinit var categoryAdapter: MovieCategoryAdapter
    private lateinit var movieAdapter: MovieGridAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        observeData()
        setupSearch()
    }

    private fun setupAdapters() {
        categoryAdapter = MovieCategoryAdapter { category ->
            viewModel.selectCategory(category?.categoryId)
            binding.tvSelectedCategory.text = category?.categoryName ?: "Todos os Filmes"
        }
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
        }

        movieAdapter = MovieGridAdapter { movie ->
            findNavController().navigate(
                R.id.movieDetailActivity,
                bundleOf("streamId" to movie.streamId)
            )
        }
        binding.rvMovies.apply {
            layoutManager = GridLayoutManager(context, getGridSpanCount())
            adapter = movieAdapter
        }
    }

    private fun getGridSpanCount(): Int {
        val displayMetrics = resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return when {
            dpWidth >= 840 -> 5  // TV / tablet landscape
            dpWidth >= 600 -> 4  // tablet portrait
            else -> 3            // phone
        }
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.searchFragment,
                bundleOf("searchType" to "movie"))
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { cats ->
                val allItem = null // null represents "All"
                categoryAdapter.submitList(listOf(allItem) + cats)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movies.collect { movies ->
                movieAdapter.submitList(movies)
                binding.tvMovieCount.text = "${movies.size} filmes"
                binding.shimmerMovies.stopShimmer()
                binding.shimmerMovies.visibility = View.GONE
                binding.rvMovies.visibility = View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                if (loading) {
                    binding.shimmerMovies.startShimmer()
                    binding.shimmerMovies.visibility = View.VISIBLE
                    binding.rvMovies.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
