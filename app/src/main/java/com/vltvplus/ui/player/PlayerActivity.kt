package com.vltvplus.ui.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Rational
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import com.vltvplus.R
import com.vltvplus.databinding.ActivityPlayerBinding
import com.vltvplus.utils.DeviceUtils
import com.vltvplus.utils.RemoteKeyUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@UnstableApi
@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModels()

    private var player: ExoPlayer? = null
    private var isTV = false

    // Auto-hide controls
    private val hideControlsHandler = Handler(Looper.getMainLooper())
    private val hideControlsRunnable = Runnable { hideControls() }
    private val HIDE_DELAY_MS = 3500L

    // Next episode countdown
    private val nextEpHandler = Handler(Looper.getMainLooper())

    // Skip intro threshold (90 seconds)
    private val SKIP_INTRO_THRESHOLD_SECS = 90
    private var introSkipped = false

    companion object {
        private const val EXTRA_TYPE = "type"
        private const val EXTRA_STREAM_ID = "stream_id"
        private const val EXTRA_EPISODE_ID = "episode_id"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_THUMBNAIL = "thumbnail"
        private const val EXTRA_EXT = "ext"
        private const val EXTRA_RESUME = "resume_position"
        private const val EXTRA_SERIES_ID = "series_id"
        private const val EXTRA_SEASON = "season"
        private const val EXTRA_EPISODE_NUM = "episode_num"

        const val TYPE_LIVE = "live"
        const val TYPE_MOVIE = "movie"
        const val TYPE_EPISODE = "episode"

        fun startLive(context: Context, streamId: Int, title: String, thumbnail: String?) {
            context.startActivity(Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_TYPE, TYPE_LIVE)
                putExtra(EXTRA_STREAM_ID, streamId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_THUMBNAIL, thumbnail)
            })
        }

        fun startMovie(context: Context, streamId: Int, title: String, thumbnail: String?,
                       ext: String, resumePosition: Long = 0L) {
            context.startActivity(Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_TYPE, TYPE_MOVIE)
                putExtra(EXTRA_STREAM_ID, streamId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_THUMBNAIL, thumbnail)
                putExtra(EXTRA_EXT, ext)
                putExtra(EXTRA_RESUME, resumePosition)
            })
        }

        fun startEpisode(context: Context, episodeId: String, title: String, thumbnail: String?,
                         ext: String, seriesId: Int, season: Int, episodeNum: Int,
                         resumePosition: Long = 0L) {
            context.startActivity(Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_TYPE, TYPE_EPISODE)
                putExtra(EXTRA_EPISODE_ID, episodeId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_THUMBNAIL, thumbnail)
                putExtra(EXTRA_EXT, ext)
                putExtra(EXTRA_RESUME, resumePosition)
                putExtra(EXTRA_SERIES_ID, seriesId)
                putExtra(EXTRA_SEASON, season)
                putExtra(EXTRA_EPISODE_NUM, episodeNum)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isTV = DeviceUtils.isTV(this)

        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_LIVE
        val streamId = intent.getIntExtra(EXTRA_STREAM_ID, -1)
        val episodeId = intent.getStringExtra(EXTRA_EPISODE_ID)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val ext = intent.getStringExtra(EXTRA_EXT) ?: "ts"
        val resumePosition = intent.getLongExtra(EXTRA_RESUME, 0L)
        val seriesId = intent.getIntExtra(EXTRA_SERIES_ID, -1)
        val season = intent.getIntExtra(EXTRA_SEASON, 1)
        val episodeNum = intent.getIntExtra(EXTRA_EPISODE_NUM, 1)

        binding.tvTitle.text = title

        viewModel.setup(type, streamId, episodeId, ext, seriesId, season, episodeNum)
        initializePlayer(resumePosition)
        setupControls(type)
        observeViewModel()
    }

    private fun initializePlayer(resumePosition: Long) {
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }

        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this))
            .build()
            .also { exo ->
                binding.playerView.player = exo
                binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

                exo.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                binding.progressBuffering.show()
                            }
                            Player.STATE_READY -> {
                                binding.progressBuffering.gone()
                                if (resumePosition > 0) {
                                    exo.seekTo(resumePosition)
                                }
                            }
                            Player.STATE_ENDED -> {
                                viewModel.onPlaybackEnded()
                            }
                            else -> {}
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        binding.btnPlayPause.setIconResource(
                            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        )
                        if (isPlaying) scheduleHideControls()
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        binding.tvError.text = "Erro de reprodução. Tentando novamente..."
                        binding.tvError.show()
                        // Retry after 3 seconds
                        Handler(Looper.getMainLooper()).postDelayed({
                            exo.prepare()
                            binding.tvError.gone()
                        }, 3000)
                    }
                })

                // Position tracking for save progress
                lifecycleScope.launch {
                    while (true) {
                        delay(5000)
                        val pos = exo.currentPosition
                        val dur = exo.duration
                        if (pos > 0 && dur > 0) {
                            viewModel.saveProgress(pos, dur)
                            checkSkipIntro(pos)
                            checkNextEpisode(pos, dur)
                        }
                    }
                }
            }

        lifecycleScope.launch {
            viewModel.streamUrl.collect { url ->
                url ?: return@collect
                val mediaItem = MediaItem.fromUri(url)
                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.playWhenReady = true
            }
        }
    }

    private fun setupControls(type: String) {
        binding.playerView.setOnClickListener { toggleControls() }

        binding.btnPlayPause.setOnClickListener {
            player?.let {
                if (it.isPlaying) it.pause() else it.play()
            }
        }

        binding.btnBack.setOnClickListener { finish() }

        // Only show seek controls for VOD
        val isVod = type == TYPE_MOVIE || type == TYPE_EPISODE
        binding.btnRewind.visibility = if (isVod) View.VISIBLE else View.GONE
        binding.btnForward.visibility = if (isVod) View.VISIBLE else View.GONE
        binding.seekBar.visibility = if (isVod) View.VISIBLE else View.GONE

        if (isVod) {
            binding.btnRewind.setOnClickListener {
                player?.seekTo((player!!.currentPosition - 10_000).coerceAtLeast(0))
                scheduleHideControls()
            }
            binding.btnForward.setOnClickListener {
                player?.seekTo(player!!.currentPosition + 10_000)
                scheduleHideControls()
            }
            binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: android.widget.SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val seekTo = (progress.toLong() * (player?.duration ?: 0)) / 100
                        player?.seekTo(seekTo)
                    }
                }
                override fun onStartTrackingTouch(sb: android.widget.SeekBar) {}
                override fun onStopTrackingTouch(sb: android.widget.SeekBar) { scheduleHideControls() }
            })

            // Update seekbar
            lifecycleScope.launch {
                while (true) {
                    delay(1000)
                    val pos = player?.currentPosition ?: 0
                    val dur = player?.duration ?: 0
                    if (dur > 0) {
                        binding.seekBar.progress = ((pos * 100) / dur).toInt()
                        binding.tvPosition.text = formatTime(pos)
                        binding.tvDuration.text = formatTime(dur)
                    }
                }
            }
        }

        binding.btnSkipIntro.setOnClickListener {
            player?.seekTo((SKIP_INTRO_THRESHOLD_SECS * 1000).toLong())
            binding.btnSkipIntro.gone()
            introSkipped = true
        }

        binding.btnNextEpisode.setOnClickListener {
            viewModel.playNextEpisode()
            binding.btnNextEpisode.gone()
        }

        // PiP button
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.btnPip.show()
            binding.btnPip.setOnClickListener { enterPiP() }
        } else {
            binding.btnPip.gone()
        }
    }

    private fun checkSkipIntro(positionMs: Long) {
        if (!introSkipped && positionMs < SKIP_INTRO_THRESHOLD_SECS * 1000L) {
            binding.btnSkipIntro.post { binding.btnSkipIntro.show() }
        } else {
            binding.btnSkipIntro.post { binding.btnSkipIntro.gone() }
        }
    }

    private fun checkNextEpisode(positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        val remaining = durationMs - positionMs
        // Show "next episode" when 60 seconds remaining
        if (remaining in 1_000..60_000 && viewModel.hasNextEpisode()) {
            binding.btnNextEpisode.post { binding.btnNextEpisode.show() }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.nextEpisodeEvent.collect { nextEp ->
                nextEp ?: return@collect
                // Auto-navigate to next episode
                val intent = Intent(this@PlayerActivity, PlayerActivity::class.java).apply {
                    putExtra(EXTRA_TYPE, TYPE_EPISODE)
                    putExtra(EXTRA_EPISODE_ID, nextEp.episodeId)
                    putExtra(EXTRA_TITLE, nextEp.title ?: "")
                    putExtra(EXTRA_THUMBNAIL, nextEp.movieImage)
                    putExtra(EXTRA_EXT, nextEp.containerExtension ?: "mp4")
                    putExtra(EXTRA_SERIES_ID, viewModel.seriesId)
                    putExtra(EXTRA_SEASON, nextEp.season)
                    putExtra(EXTRA_EPISODE_NUM, nextEp.episodeNum)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun toggleControls() {
        if (binding.layoutControls.visibility == View.VISIBLE) {
            hideControls()
        } else {
            showControls()
        }
    }

    private fun showControls() {
        binding.layoutControls.visibility = View.VISIBLE
        binding.layoutTopBar.visibility = View.VISIBLE
        scheduleHideControls()
    }

    private fun hideControls() {
        binding.layoutControls.visibility = View.GONE
        binding.layoutTopBar.visibility = View.GONE
    }

    private fun scheduleHideControls() {
        hideControlsHandler.removeCallbacks(hideControlsRunnable)
        hideControlsHandler.postDelayed(hideControlsRunnable, HIDE_DELAY_MS)
    }

    // TV Remote Key Handling
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        showControls()
        return when {
            RemoteKeyUtils.isPlayPause(keyCode) -> {
                if (player?.isPlaying == true) player?.pause() else player?.play()
                true
            }
            RemoteKeyUtils.isFastForward(keyCode) -> {
                player?.seekTo((player!!.currentPosition + 10_000))
                true
            }
            RemoteKeyUtils.isRewind(keyCode) -> {
                player?.seekTo((player!!.currentPosition - 10_000).coerceAtLeast(0))
                true
            }
            RemoteKeyUtils.isDpadCenter(keyCode) -> {
                toggleControls()
                true
            }
            RemoteKeyUtils.isDpadRight(keyCode) -> {
                player?.seekTo(player!!.currentPosition + 10_000)
                true
            }
            RemoteKeyUtils.isDpadLeft(keyCode) -> {
                player?.seekTo((player!!.currentPosition - 10_000).coerceAtLeast(0))
                true
            }
            RemoteKeyUtils.isChannelUp(keyCode) -> {
                viewModel.playNextEpisode()
                true
            }
            RemoteKeyUtils.isChannelDown(keyCode) -> {
                viewModel.playPrevEpisode()
                true
            }
            RemoteKeyUtils.isBack(keyCode) -> {
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun enterPiP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isInPictureInPictureMode) player?.pause()
    }

    override fun onStop() {
        super.onStop()
        player?.let {
            viewModel.saveProgress(it.currentPosition, it.duration)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideControlsHandler.removeCallbacks(hideControlsRunnable)
        player?.release()
        player = null
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
    }

    private fun formatTime(ms: Long): String {
        val secs = ms / 1000
        val h = secs / 3600
        val m = (secs % 3600) / 60
        val s = secs % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
        else String.format("%02d:%02d", m, s)
    }

    private fun View.show() { visibility = View.VISIBLE }
    private fun View.gone() { visibility = View.GONE }
}
