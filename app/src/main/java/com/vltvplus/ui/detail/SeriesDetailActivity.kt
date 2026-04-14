package com.vltvplus.ui.detail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vltvplus.databinding.ActivitySeriesDetailBinding
import com.vltvplus.ui.detail.adapters.EpisodeListAdapter
import com.vltvplus.ui.detail.adapters.SeasonTabAdapter
import com.vltvplus.ui.player.PlayerActivity
import com.vltvplus.utils.extensions.gone
import com.vltvplus.utils.extensions.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SeriesDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeriesDetailBinding
    private val viewModel: SeriesDetailViewModel by viewModels()

    private lateinit var episodeAdapter: EpisodeListAdapter
    private lateinit var seasonAdapter: SeasonTabAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val seriesId = intent.getIntExtra("seriesId", -1)
        if (seriesId == -1) { finish(); return }

        viewModel.loadSeries(seriesId)
        setupUI()
        observeData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnFavorite.setOnClickListener { viewModel.toggleFavorite() }

        seasonAdapter = SeasonTabAdapter { season ->
            viewModel.selectSeason(season)
        }
        binding.rvSeasons.apply {
            layoutManager = LinearLayoutManager(this@SeriesDetailActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = seasonAdapter
        }

        episodeAdapter = EpisodeListAdapter { episode ->
            PlayerActivity.startEpisode(
                this,
                episode.episodeId,
                episode.title ?: "Episódio ${episode.episodeNum}",
                episode.movieImage,
                episode.containerExtension ?: "mp4",
                viewModel.series.value?.seriesId ?: 0,
                episode.season,
                episode.episodeNum
            )
        }
        binding.rvEpisodes.apply {
            layoutManager = LinearLayoutManager(this@SeriesDetailActivity)
            adapter = episodeAdapter
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.series.collect { series ->
                series ?: return@collect

                // Backdrop
                val backdrop = series.tmdbBackdropPath ?: series.backdropPath ?: series.cover
                Glide.with(this@SeriesDetailActivity)
                    .load(backdrop)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivBackdrop)

                // Palette color extraction
                if (!backdrop.isNullOrEmpty()) {
                    Glide.with(this@SeriesDetailActivity)
                        .asBitmap()
                        .load(backdrop)
                        .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                            override fun onResourceReady(resource: android.graphics.Bitmap, t: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?) {
                                Palette.from(resource).generate { palette ->
                                    val color = palette?.getDarkVibrantColor(Color.BLACK) ?: Color.BLACK
                                    binding.gradientOverlay.setBackgroundColor(color)
                                }
                            }
                            override fun onLoadCleared(p: android.graphics.drawable.Drawable?) {}
                        })
                }

                // Poster
                Glide.with(this@SeriesDetailActivity)
                    .load(series.posterPath ?: series.cover)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(com.vltvplus.R.drawable.placeholder_poster)
                    .into(binding.ivPoster)

                binding.tvTitle.text = series.name
                binding.tvOverview.text = series.overview ?: series.plot ?: ""
                binding.tvYear.text = series.releaseDate?.take(4) ?: ""
                binding.tvRating.text = series.voteAverage?.let { String.format("%.1f", it) }
                    ?: series.rating?.let { String.format("%.1f", it) } ?: ""
                binding.tvGenre.text = series.genre ?: ""
                binding.tvSeasonCount.text = series.numberOfSeasons?.let { "$it Temporada(s)" } ?: ""

                // Season tabs
                val seasons = (1..(series.numberOfSeasons ?: 1)).toList()
                seasonAdapter.submitList(seasons)

                // Play button
                binding.btnPlay.setOnClickListener {
                    viewModel.getFirstEpisode()?.let { ep ->
                        PlayerActivity.startEpisode(
                            this@SeriesDetailActivity,
                            ep.episodeId, ep.title ?: "Episódio 1",
                            ep.movieImage, ep.containerExtension ?: "mp4",
                            series.seriesId, ep.season, ep.episodeNum
                        )
                    }
                }

                // Trailer
                if (!series.trailerKey.isNullOrEmpty()) {
                    binding.btnTrailer.show()
                    binding.btnTrailer.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.youtube.com/watch?v=${series.trailerKey}"))
                        startActivity(intent)
                    }
                } else {
                    binding.btnTrailer.gone()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.episodes.collect { eps ->
                episodeAdapter.submitList(eps)
                binding.tvEpisodeCount.text = "${eps.size} episódio(s)"
            }
        }

        lifecycleScope.launch {
            viewModel.isFavorite.collect { fav ->
                binding.btnFavorite.setIconResource(
                    if (fav) com.vltvplus.R.drawable.ic_favorite_filled
                    else com.vltvplus.R.drawable.ic_favorite_outline
                )
            }
        }

        lifecycleScope.launch {
            viewModel.continueEpisode.collect { progress ->
                if (progress != null) {
                    binding.btnContinue.show()
                    binding.btnContinue.text = "Continuar E${progress.episodeNumber}"
                    binding.btnContinue.setOnClickListener {
                        progress.episodeId?.let { epId ->
                            val ep = viewModel.getEpisodeById(epId)
                            ep?.let {
                                PlayerActivity.startEpisode(
                                    this@SeriesDetailActivity,
                                    it.episodeId, it.title ?: "", it.movieImage,
                                    it.containerExtension ?: "mp4",
                                    viewModel.series.value?.seriesId ?: 0,
                                    it.season, it.episodeNum,
                                    resumePosition = progress.position
                                )
                            }
                        }
                    }
                } else {
                    binding.btnContinue.gone()
                }
            }
        }
    }
}
