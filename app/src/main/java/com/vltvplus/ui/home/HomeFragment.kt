package com.vltvplus.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.vltvplus.R
import com.vltvplus.databinding.FragmentHomeBinding
import com.vltvplus.ui.home.adapters.ContentRowAdapter
import com.vltvplus.ui.home.adapters.ContinueWatchingAdapter
import com.vltvplus.ui.home.adapters.FeaturedBannerAdapter
import kotlinx.coroutines.launch
// IMPORT NECESSÁRIO ADICIONADO ABAIXO:
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator 

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var featuredAdapter: FeaturedBannerAdapter
    private lateinit var continueAdapter: ContinueWatchingAdapter
    private lateinit var recentMoviesAdapter: ContentRowAdapter
    private lateinit var topRatedAdapter: ContentRowAdapter
    private lateinit var recentSeriesAdapter: ContentRowAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        observeData()
        setupSwipeRefresh()
    }

    private fun setupAdapters() {
        // Featured banner (hero section)
        featuredAdapter = FeaturedBannerAdapter(
            onMovieClick = { movie ->
                findNavController().navigate(R.id.movieDetailActivity,
                    bundleOf("streamId" to movie.streamId, "type" to "movie"))
            },
            onSeriesClick = { series ->
                findNavController().navigate(R.id.seriesDetailActivity,
                    bundleOf("seriesId" to series.seriesId, "type" to "series"))
            }
        )
        binding.viewPagerFeatured.adapter = featuredAdapter
        
        // CORREÇÃO: Conectando o indicador ao ViewPager2 corretamente
        binding.dotsIndicator.attachTo(binding.viewPagerFeatured)

        // Continue Watching
        continueAdapter = ContinueWatchingAdapter { progress ->
            when (progress.contentType) {
                "movie" -> findNavController().navigate(R.id.movieDetailActivity,
                    bundleOf("streamId" to progress.contentId.toIntOrNull(), "resumePosition" to progress.position))
                "series" -> findNavController().navigate(R.id.seriesDetailActivity,
                    bundleOf("seriesId" to progress.seriesId, "episodeId" to progress.episodeId))
            }
        }
        binding.rvContinueWatching.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = continueAdapter
        }

        // Recent Movies
        recentMoviesAdapter = ContentRowAdapter("movie") { movie ->
            findNavController().navigate(R.id.movieDetailActivity,
                bundleOf("streamId" to movie.id))
        }
        binding.rvRecentMovies.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentMoviesAdapter
        }

        // Top Rated
        topRatedAdapter = ContentRowAdapter("movie") { movie ->
            findNavController().navigate(R.id.movieDetailActivity,
                bundleOf("streamId" to movie.id))
        }
        binding.rvTopRated.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = topRatedAdapter
        }

        // Recent Series
        recentSeriesAdapter = ContentRowAdapter("series") { series ->
            findNavController().navigate(R.id.seriesDetailActivity,
                bundleOf("seriesId" to series.id))
        }
        binding.rvRecentSeries.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentSeriesAdapter
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.continueWatching.collect { list ->
                val visible = list.isNotEmpty()
                binding.tvContinueWatching.visibility = if (visible) View.VISIBLE else View.GONE
                binding.rvContinueWatching.visibility = if (visible) View.VISIBLE else View.GONE
                continueAdapter.submitList(list)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentMovies.collect { movies ->
                recentMoviesAdapter.submitMovies(movies)
                if (movies.isNotEmpty()) {
                    featuredAdapter.submitMovies(movies.take(5))
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topRatedMovies.collect { movies ->
                topRatedAdapter.submitMovies(movies)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentSeries.collect { series ->
                recentSeriesAdapter.submitSeries(series)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.syncState.collect { state ->
                binding.swipeRefresh.isRefreshing = state is com.vltvplus.utils.Resource.Loading
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.syncContent(forceRefresh = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
