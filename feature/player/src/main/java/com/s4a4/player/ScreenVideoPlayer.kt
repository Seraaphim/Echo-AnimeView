import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.s4a4.player.CustomVideoController
import com.s4a4.player.ViewModelVideoPlayer


@Composable
fun ScreenVideoPlayer(id: Int, voice: String, episode: Int) {
    val activity = LocalContext.current as Activity

    val viewModel = hiltViewModel<ViewModelVideoPlayer>()
    LaunchedEffect(key1 = Unit) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        activity.hideSystemUI()
        viewModel.setArgument(id, voice, episode)
    }
    BackHandler {
        viewModel.onBack().let {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            activity.showSystemUI()
        }
    }

    val currentEpisodeUrl by viewModel.currentEpisodeSeriesModel.collectAsState()


    if (currentEpisodeUrl != null) {
        VideoPlayer(viewModel)
    }
}

@SuppressLint("OpaqueUnitKey")
@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun VideoPlayer(viewModel: ViewModelVideoPlayer) {
    val context = LocalContext.current
    val definition by viewModel.currentDefinition.collectAsState()
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = false
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    LaunchedEffect(key1 = definition) {
        val defaultDataSourceFactory = DefaultDataSource.Factory(context)
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
            context,
            defaultDataSourceFactory
        )
        exoPlayer.setMediaSource(
            HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(definition?.url!!))
        )

        if (definition?.url?.split(".")?.last() == "mp4") {
            val progressiveMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(definition?.url!!))

            exoPlayer.setMediaSource(progressiveMediaSource)
        } else {
            val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(definition?.url!!))

            exoPlayer.setMediaSource(hlsMediaSource)
        }
        exoPlayer.prepare()
    }



    Box(modifier = Modifier.fillMaxSize()) {
        DisposableEffect(

            AndroidView(
                factory = {
                    PlayerView(context).apply {
                        hideController()
                        useController = false
                        player = exoPlayer
                        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)

                    }

                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )

        ) {

            onDispose { exoPlayer.release() }
        }
        CustomVideoController(
            exoPlayer, modifier = Modifier
                .fillMaxSize(0.95f)
                .align(Alignment.Center),
            viewModel
        )
    }

}

fun Activity.hideSystemUI() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.decorView.systemUiVisibility = (
            // Скрытие нижней панели навигации
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    // Скрытие статус-бара
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    // Возможность использования жестового навигационного жеста
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
}

fun Activity.showSystemUI() {
    WindowCompat.setDecorFitsSystemWindows(window, true)
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
}
