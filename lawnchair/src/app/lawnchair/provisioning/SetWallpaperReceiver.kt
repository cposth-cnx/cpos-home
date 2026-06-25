package app.lawnchair.provisioning

import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.launcher3.BuildConfig
import java.io.File

/**
 * Headless wallpaper setter used by the device-provisioning script.
 *
 * Trigger via adb (image must live in this app's own external-media dir, which adb can
 * write to and the app can read without any storage permission):
 *
 *   adb push wp.png /sdcard/Android/media/<pkg>/provision_wallpaper.png
 *   adb shell am broadcast \
 *     -a <pkg>.SET_WALLPAPER \
 *     -n <pkg>/app.lawnchair.provisioning.SetWallpaperReceiver \
 *     --es path /sdcard/Android/media/<pkg>/provision_wallpaper.png \
 *     --es target both        # both | system | lock  (optional, default both)
 *
 * Result (ordered broadcast): code 1 = success, 0 = failure; data = a human-readable message.
 */
class SetWallpaperReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SET_WALLPAPER) return
        val message = runCatching { apply(context, intent) }.getOrElse { "ERROR: ${it.message}" }
        Log.i(TAG, message)
        if (isOrderedBroadcast) {
            resultCode = if (message.startsWith("OK")) 1 else 0
            resultData = message
        }
    }

    private fun apply(context: Context, intent: Intent): String {
        val path = intent.getStringExtra(EXTRA_PATH) ?: return "ERROR: missing extra '$EXTRA_PATH'"
        val file = File(path).canonicalFile

        // Security: only ever read from this app's own private/media dirs.
        val allowedRoots = (
            context.externalMediaDirs.toList() +
                context.getExternalFilesDir(null) +
                context.filesDir
            ).filterNotNull().map { it.canonicalPath }
        if (allowedRoots.none { file.path.startsWith(it) }) {
            return "ERROR: path is not in an app-owned directory: ${file.path}"
        }
        if (!file.exists() || !file.canRead()) return "ERROR: file not readable: ${file.path}"

        val which = when (intent.getStringExtra(EXTRA_TARGET)?.lowercase()) {
            TARGET_SYSTEM -> WallpaperManager.FLAG_SYSTEM
            TARGET_LOCK -> WallpaperManager.FLAG_LOCK
            else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
        }
        val wallpaperManager = WallpaperManager.getInstance(context)
        file.inputStream().use { wallpaperManager.setStream(it, null, true, which) }
        return "OK: wallpaper set from ${file.name}"
    }

    companion object {
        private const val TAG = "SetWallpaperReceiver"
        const val ACTION_SET_WALLPAPER = "${BuildConfig.APPLICATION_ID}.SET_WALLPAPER"
        const val EXTRA_PATH = "path"
        const val EXTRA_TARGET = "target"
        private const val TARGET_SYSTEM = "system"
        private const val TARGET_LOCK = "lock"
    }
}
