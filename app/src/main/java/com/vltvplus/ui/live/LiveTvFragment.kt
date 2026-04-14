package com.vltvplus.ui.live

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vltvplus.R
import com.vltvplus.data.database.entities.ChannelEntity
import com.vltvplus.databinding.FragmentLiveTvBinding
import com.vltvplus.ui.live.adapters.CategoryChipAdapter
import com.vltvplus.ui.live.adapters.ChannelAdapter
import com.vltvplus.ui.live.adapters.EpgAdapter
import com.vltvplus.ui.player.PlayerActivity
import com.vltvplus.utils.DeviceUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LiveTvFragment : Fragment() {

    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LiveTvViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryChipAdapter
    private lateinit var channelAdapter: ChannelAdapter
    private lateinit var epgAdapter: EpgAdapter

    private var selectedChannel: ChannelEntity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLiveTvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        observeData()
        setupMiniPlayer()
    }

    private fun setupAdapters() {
        // Category chips (left side)
        categoryAdapter = CategoryChipAdapter { category ->
            viewModel.selectCategory(category.categoryId)
        }
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = categoryAdapter
        }

        // Channels grid
        val isTV = DeviceUtils.isTV(requireContext())
        channelAdapter = ChannelAdapter(
            onChannelClick = { channel -> onChannelSelected(channel) },
            onChannelFocus = { channel ->
                if (isTV) onChannelFocused(channel)
            }
        )
        binding.rvChannels.apply {
            layoutManager = if (isTV)
                GridLayoutManager(context, 4)
            else
                LinearLayoutManager(context)
            adapter = channelAdapter
        }

        // EPG list below mini player
        epgAdapter = EpgAdapter()
        binding.rvEpg.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = epgAdapter
        }
    }

    private fun onChannelSelected(channel: ChannelEntity) {
        selectedChannel = channel
        // Start full player
        PlayerActivity.startLive(requireContext(), channel.streamId, channel.name, channel.streamIcon)
    }

    private fun onChannelFocused(channel: ChannelEntity) {
        // On TV: show mini preview when channel is focused
        selectedChannel = channel
        showMiniPreview(channel)
        loadEpg(channel)
    }

    private fun showMiniPreview(channel: ChannelEntity) {
        binding.layoutMiniPlayer.visibility = View.VISIBLE
        binding.tvChannelName.text = channel.name
        viewModel.startMiniPreview(channel.streamId)
    }

    private fun loadEpg(channel: ChannelEntity) {
        channel.epgChannelId?.let { epgId ->
            viewModel.loadEpg(channel.streamId, epgId)
        }
    }

    private fun setupMiniPlayer() {
        binding.layoutMiniPlayer.setOnClickListener {
            selectedChannel?.let { channel ->
                PlayerActivity.startLive(requireContext(), channel.streamId, channel.name, channel.streamIcon)
            }
        }

        binding.btnExpandPlayer.setOnClickListener {
            selectedChannel?.let { channel ->
                PlayerActivity.startLive(requireContext(), channel.streamId, channel.name, channel.streamIcon)
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { cats ->
                categoryAdapter.submitList(cats)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.channels.collect { channels ->
                channelAdapter.submitList(channels)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.epgList.collect { epgList ->
                epgAdapter.submitList(epgList)
                binding.layoutEpg.visibility = if (epgList.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentProgram.collect { program ->
                program?.let {
                    binding.tvCurrentProgram.text = it.title
                    binding.tvProgramTime.text = viewModel.formatTime(it.startTimestamp, it.stopTimestamp)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopMiniPreview()
        _binding = null
    }
}
