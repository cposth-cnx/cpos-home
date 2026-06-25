package app.lawnchair.provisioning

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.LocaleList
import android.util.Log
import com.android.launcher3.BuildConfig
import java.util.Locale

/**
 * Headless system-locale setter used by the device-provisioning script.
 *
 * Changes the *whole device* language live (no reboot), exactly like adding a language in
 * Settings > System > Languages and moving it to the top. This is the one thing plain adb
 * shell cannot do: it needs the [android.Manifest.permission.CHANGE_CONFIGURATION] permission,
 * which this app declares. CHANGE_CONFIGURATION carries the "development" protection flag, so it
 * can be granted over adb without root:
 *
 *   adb shell pm grant <pkg> android.permission.CHANGE_CONFIGURATION
 *   adb shell am broadcast \
 *     -a <pkg>.SET_LOCALE \
 *     -n <pkg>/app.lawnchair.provisioning.SetLocaleReceiver \
 *     --es language th \
 *     --es country TH        # optional; pairs with language (e.g. th-TH)
 *
 * The requested locale is moved to the front of the existing locale list (others kept as
 * fallbacks), then pushed through the (hidden) IActivityManager via reflection — the same path
 * the system Settings app uses.
 *
 * Result (ordered broadcast): code 1 = success, 0 = failure; data = a human-readable message.
 */
class SetLocaleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SET_LOCALE) return
        val message = runCatching { apply(intent) }.getOrElse { "ERROR: ${it.message}" }
        Log.i(TAG, message)
        if (isOrderedBroadcast) {
            resultCode = if (message.startsWith("OK")) 1 else 0
            resultData = message
        }
    }

    private fun apply(intent: Intent): String {
        val language = intent.getStringExtra(EXTRA_LANGUAGE)?.trim()
            ?: return "ERROR: missing extra '$EXTRA_LANGUAGE'"
        if (language.isEmpty()) return "ERROR: empty extra '$EXTRA_LANGUAGE'"
        val country = intent.getStringExtra(EXTRA_COUNTRY)?.trim().orEmpty()
        val locale = if (country.isEmpty()) Locale(language) else Locale(language, country)

        // IActivityManager via the hidden ActivityManager.getService() (API 26+).
        val am = Class.forName("android.app.ActivityManager")
            .getMethod("getService").invoke(null)
            ?: return "ERROR: ActivityManager.getService() returned null"

        val config = am.javaClass.getMethod("getConfiguration").invoke(am) as Configuration

        // Requested locale first, existing ones kept as fallbacks (mirrors the Settings reorder).
        config.setLocales(withLocaleFirst(locale, config.locales))
        // Mark as a deliberate user choice so the system persists it like a manual change.
        runCatching { Configuration::class.java.getField("userSetLocale").setBoolean(config, true) }

        am.javaClass
            .getMethod("updatePersistentConfiguration", Configuration::class.java)
            .invoke(am, config)
        Locale.setDefault(locale)
        return "OK: locale set to ${locale.toLanguageTag()}"
    }

    private fun withLocaleFirst(primary: Locale, existing: LocaleList): LocaleList {
        val ordered = LinkedHashSet<Locale>()
        ordered.add(primary)
        for (i in 0 until existing.size()) ordered.add(existing[i])
        return LocaleList(*ordered.toTypedArray())
    }

    companion object {
        private const val TAG = "SetLocaleReceiver"
        const val ACTION_SET_LOCALE = "${BuildConfig.APPLICATION_ID}.SET_LOCALE"
        const val EXTRA_LANGUAGE = "language"
        const val EXTRA_COUNTRY = "country"
    }
}
