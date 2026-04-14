package com.vltvplus.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.vltvplus.R
import com.vltvplus.databinding.FragmentSearchBinding
import com.vltvplus.ui.search.adapters.SearchResultAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()

    private lateinit var resultAdapter: SearchResultAdapter
    private var searchJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-set search type if passed from movies/series fragment
        arguments?.getString("searchType")?.let { viewModel.setSearchType(it) }

        setupAdapter()
        setupSearch()
        observeResults()

        // Auto-focus keyboard
        binding.etSearch.requestFocus()
    }

    private fun setupAdapter() {
        resultAdapter = SearchResultAdapter(
            onMovieClick = { movie ->
                findNavController().navigate(R.id.movieDetailActivity,
                    bundleOf("streamId" to movie.streamId))
            },
            onSeriesClick = { series ->
                findNavController().navigate(R.id.seriesDetailActivity,
                    bundleOf("seriesId" to series.seriesId))
            },
            onChannelClick = { channel ->
                com.vltvplus.ui.player.PlayerActivity.startLive(
                    requireContext(), channel.streamId, channel.name, channel.streamIcon)
            }
        )
        binding.rvResults.apply {
            layoutManager = GridLayoutManager(context, getSpanCount())
            adapter = resultAdapter
        }
    }

    private fun getSpanCount(): Int {
        val dp = resources.displayMetrics.widthPixels / resources.displayMetrics.density
        return when {
            dp >= 840 -> 5
            dp >= 600 -> 4
            else -> 3
        }
    }

    private fun setupSearch() {
        // Filter chips
        binding.chipAll.setOnClickListener { viewModel.setSearchType("all") }
        binding.chipMovies.setOnClickListener { viewModel.setSearchType("movie") }
        binding.chipSeries.setOnClickListener { viewModel.setSearchType("series") }
        binding.chipLive.setOnClickListener { viewModel.setSearchType("live") }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(400) // debounce 400ms
                    viewModel.search(s.toString())
                }
            }
        })

        binding.btnClear.setOnClickListener {
            binding.etSearch.setText("")
            viewModel.search("")
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { results ->
                resultAdapter.submitResults(results)

                val isEmpty = results.movies.isEmpty() && results.series.isEmpty() && results.channels.isEmpty()
                val hasQuery = binding.etSearch.text.isNotEmpty()

                binding.layoutEmpty.visibility = if (isEmpty && hasQuery) View.VISIBLE else View.GONE
                binding.rvResults.visibility = if (!isEmpty) View.VISIBLE else View.GONE
                binding.tvResultCount.text = "${results.totalCount} resultado(s)"
                binding.tvResultCount.visibility = if (!isEmpty) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding.progressSearch.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
