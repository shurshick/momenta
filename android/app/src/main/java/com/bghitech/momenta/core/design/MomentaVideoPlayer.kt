package com.bghitech.momenta.core.design

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bghitech.momenta.R

import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MomentaVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    previewUrl: String? = null,
    autoPlay: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var hasRenderedFirstFrame by remember(videoUrl) { mutableStateOf(false) }
    var playbackFailed by remember(videoUrl) { mutableStateOf(false) }

    val formattedVideoUrl = remember(videoUrl) {
        normalizeMediaUrl(videoUrl)
    }
    val formattedPreviewUrl = remember(previewUrl) {
        previewUrl?.let { normalizeMediaUrl(it) }
    }
    val fallbackImageUrl = formattedPreviewUrl?.takeIf {
        it.isNotBlank() && !isVideoMediaUrl(it)
    }
    val localFrameSource = when {
        formattedPreviewUrl?.let(::isLocalMediaUrl) == true && isVideoMediaUrl(formattedPreviewUrl) -> formattedPreviewUrl
        isLocalMediaUrl(formattedVideoUrl) -> formattedVideoUrl
        else -> null
    }
    val localPreviewFrame by produceState<Bitmap?>(
        initialValue = null,
        key1 = localFrameSource
    ) {
        value = withContext(Dispatchers.IO) {
            localFrameSource?.let { loadVideoFrame(context, it) }
        }
    }

    val exoPlayer = remember(formattedVideoUrl) {
        val okHttpClient = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        val dataSourceFactory = DefaultDataSource.Factory(
            context,
            OkHttpDataSource.Factory(okHttpClient)
        )
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                val videoUri = if (formattedVideoUrl.startsWith("http://") || formattedVideoUrl.startsWith("https://") || formattedVideoUrl.startsWith("content://") || formattedVideoUrl.startsWith("file://")) {
                    Uri.parse(formattedVideoUrl)
                } else {
                    Uri.fromFile(java.io.File(formattedVideoUrl))
                }
                setMediaItem(MediaItem.fromUri(videoUri))
                repeatMode = Player.REPEAT_MODE_ONE
                volume = if (isMuted) 0f else 1f
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) isPlaying = false
                    }

                    override fun onRenderedFirstFrame() {
                        hasRenderedFirstFrame = true
                        playbackFailed = false
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        playbackFailed = true
                        isPlaying = false
                        Log.e("MomentaVideoPlayer", "Playback failed for $formattedVideoUrl", error)
                    }
                })
                prepare()
                playWhenReady = autoPlay
            }
    }

    DisposableEffect(videoUrl) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                if (onClick != null) {
                    onClick()
                } else {
                    if (playbackFailed) {
                        playbackFailed = false
                        isPlaying = true
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    } else {
                        isPlaying = !isPlaying
                        exoPlayer.playWhenReady = isPlaying
                    }
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                val parent = FrameLayout(ctx)
                (LayoutInflater.from(ctx).inflate(
                    R.layout.momenta_video_player,
                    parent,
                    false
                ) as PlayerView).apply {
                    player = exoPlayer
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setKeepContentOnPlayerReset(true)
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!hasRenderedFirstFrame) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MomentaSurfaceAlt),
                contentAlignment = Alignment.Center
            ) {
                MomentaLoadingMark(size = 48)
                when {
                    fallbackImageUrl != null -> Image(
                        painter = rememberAsyncImagePainter(model = fallbackImageUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    localPreviewFrame != null -> Image(
                        bitmap = localPreviewFrame!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Overlay mute/unmute button
        IconButton(
            onClick = {
                isMuted = !isMuted
                exoPlayer.volume = if (isMuted) 0f else 1f
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .size(36.dp)
        ) {
            Icon(
                imageVector = if (isMuted) {
                    Icons.AutoMirrored.Filled.VolumeMute
                } else {
                    Icons.AutoMirrored.Filled.VolumeUp
                },
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Overlay play indicator if paused
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

private fun normalizeMediaUrl(url: String): String {
    if (url.isBlank()) return url
    if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://") || url.startsWith("content://")) {
        return url
    }
    val localFile = java.io.File(url)
    if (localFile.isAbsolute && localFile.exists()) {
        return Uri.fromFile(localFile).toString()
    }
    val baseUrl = com.bghitech.momenta.BuildConfig.DEFAULT_SERVER_URL.trimEnd('/')
    val path = if (url.startsWith("/")) url else "/$url"
    return "$baseUrl$path"
}

private fun isLocalMediaUrl(url: String): Boolean =
    url.startsWith("file://") || url.startsWith("content://") || !url.contains("://")

private fun isVideoMediaUrl(url: String): Boolean {
    val path = Uri.parse(url).path.orEmpty().lowercase()
    return path.endsWith(".mp4") || path.endsWith(".mov") ||
        path.endsWith(".webm") || path.endsWith(".m4v")
}

private fun loadVideoFrame(context: android.content.Context, url: String): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        if (url.startsWith("file://") || url.startsWith("content://")) {
            retriever.setDataSource(context, Uri.parse(url))
        } else {
            retriever.setDataSource(url)
        }
        retriever.getFrameAtTime(100_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            ?: retriever.frameAtTime
    } catch (error: Exception) {
        Log.w("MomentaVideoPlayer", "Could not extract local video preview", error)
        null
    } finally {
        retriever.release()
    }
}
