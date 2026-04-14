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
import com.vltvplus.databinding.ActivityMovieDetailBinding
import com.vltvplus.ui.player.PlayerActivity
import com.vltvplus.utils.extensions.gone
import com.vltvplus.utils.extensions.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private val viewModel: MovieDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val streamId = intent.getIntExtra("streamId", -1)
        val resumePosition = intent.getLongExtra("resumePosition", 0L)

        if (streamId == -1) { finish(); return }

        viewModel.loadMovie(streamId)
        setupToolbar()
        observeData(resumePosition)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnFavorite.setOnClickListener { viewModel.toggleFavorite() }
    }

    private fun observeData(resumePosition: Long) {
        lifecycleScope.launch {
            viewModel.movie.collect { movie ->
                movie ?: return@collect

                // Backdrop
                val backdropUrl = movie.backdropPath ?: movie.streamIcon
                Glide.with(this@MovieDetailActivity)
                    .load(backdropUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivBackdrop)

                // Palette color extraction for gradient tint
                if (!backdropUrl.isNullOrEmpty()) {
                    Glide.with(this@MovieDetailActivity)
                        .asBitmap()
                        .load(backdropUrl)
                        .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                            override fun onResourceReady(resource: android.graphics.Bitmap, t: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?) {
                                Palette.from(resource).generate { palette ->
                                    val dominant = palette?.getDarkVibrantColor(Color.BLACK)
                                        ?: palette?.getDarkMutedColor(Color.BLACK)
                                        ?: Color.BLACK
                                    binding.gradientOverlay.setBackgroundColor(dominant)
                                }
                            }
                            override fun onLoadCleared(p: android.graphics.drawable.Drawable?) {}
                        })
                }

                // Poster
                Glide.with(this@MovieDetailActivity)
                    .load(movie.posterPath ?: movie.streamIcon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(com.vltvplus.R.drawable.placeholder_poster)
                    .into(binding.ivPoster)

                // Info
                binding.tvTitle.text = movie.name
                binding.tvTagline.text = movie.tagline ?: ""
                binding.tvTagline.visibility = if (movie.tagline.isNullOrEmpty()) View.GONE else View.VISIBLE
                binding.tvOverview.text = movie.overview ?: movie.name
                binding.tvYear.text = movie.releaseDate?.take(4) ?: ""
                binding.tvRating.text = movie.voteAverage?.let { String.format("%.1f", it) } ?: ""
                binding.tvRuntime.text = movie.runtime?.let { "${it}min" } ?: ""
                binding.tvGenres.text = movie.genres ?: ""
                binding.tvDirector.text = if (!movie.director.isNullOrEmpty()) "Direção: ${movie.director}" else ""
                binding.tvCast.text = if (!movie.cast.isNullOrEmpty()) "Elenco: ${movie.cast}" else ""

                // Watch / Continue button
                if (resumePosition > 0) {
                    binding.btnPlay.text = "Continuar Assistindo"
                    binding.btnPlay.setIconResource(com.vltvplus.R.drawable.ic_continue)
                } else {
                    binding.btnPlay.text = "Assistir"
                    binding.btnPlay.setIconResource(com.vltvplus.R.drawable.ic_play)
                }

                binding.btnPlay.setOnClickListener {
                    PlayerActivity.startMovie(
                        this@MovieDetailActivity,
                        movie.streamId,
                        movie.name,
                        movie.posterPath,
                        movie.containerExtension ?: "mp4",
                        resumePosition
                    )
                }

                // Trailer button
                if (!movie.trailerKey.isNullOrEmpty()) {
                    binding.btnTrailer.show()
                    binding.btnTrailer.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.youtube.com/watch?v=${movie.trailerKey}"))
                        startActivity(intent)
                    }
                } else {
                    binding.btnTrailer.gone()
                }
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
            viewModel.similarMovies.collect { similar ->
                if (similar.isNotEmpty()) {
                    binding.tvSimilar.show()
                    binding.rvSimilar.show()
                    val adapter = com.vltvplus.ui.movies.adapters.MovieGridAdapter { movie ->
                        // Reload detail for similar movie
                        val intent = Intent(this@MovieDetailActivity, MovieDetailActivity::class.java)
                        intent.putExtra("streamId", movie.streamId)
                        startActivity(intent)
                    }
                    binding.rvSimilar.layoutManager = LinearLayoutManager(
                        this@MovieDetailActivity, LinearLayoutManager.HORIZONTAL, false)
                    binding.rvSimilar.adapter = adapter
                    adapter.submitList(similar)
                }
            }
        }
    }
}
