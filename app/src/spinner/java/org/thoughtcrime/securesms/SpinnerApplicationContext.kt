package org.thoughtcrime.securesms

import android.content.ContentValues
import android.os.Build
import leakcanary.LeakCanary
import org.signal.spinner.Spinner
import org.thoughtcrime.securesms.database.DatabaseMonitor
import org.thoughtcrime.securesms.database.JobDatabase
import org.thoughtcrime.securesms.database.KeyValueDatabase
import org.thoughtcrime.securesms.database.LocalMetricsDatabase
import org.thoughtcrime.securesms.database.LogDatabase
import org.thoughtcrime.securesms.database.MegaphoneDatabase
import org.thoughtcrime.securesms.database.QueryMonitor
import org.thoughtcrime.securesms.database.SignalDatabase
import org.thoughtcrime.securesms.util.AppSignatureUtil
import shark.AndroidReferenceMatchers

class SpinnerApplicationContext : ApplicationContext() {
  override fun onCreate() {
    super.onCreate()

    Spinner.init(
      this,
      Spinner.DeviceInfo(
        name = "${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})",
        packageName = "$packageName (${AppSignatureUtil.getAppSignature(this).or("Unknown")})",
        appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.CANONICAL_VERSION_CODE}, ${BuildConfig.GIT_HASH})"
      ),
      linkedMapOf(
        "signal" to SignalDatabase.rawDatabase,
        "jobmanager" to JobDatabase.getInstance(this).sqlCipherDatabase,
        "keyvalue" to KeyValueDatabase.getInstance(this).sqlCipherDatabase,
        "megaphones" to MegaphoneDatabase.getInstance(this).sqlCipherDatabase,
        "localmetrics" to LocalMetricsDatabase.getInstance(this).sqlCipherDatabase,
        "logs" to LogDatabase.getInstance(this).sqlCipherDatabase,
      )
    )

    DatabaseMonitor.initialize(object : QueryMonitor {
      override fun onSql(sql: String, args: Array<Any>?) {
        Spinner.onSql("signal", sql, args)
      }

      override fun onQuery(distinct: Boolean, table: String, projection: Array<String>?, selection: String?, args: Array<Any>?, groupBy: String?, having: String?, orderBy: String?, limit: String?) {
        Spinner.onQuery("signal", distinct, table, projection, selection, args, groupBy, having, orderBy, limit)
      }

      override fun onDelete(table: String, selection: String?, args: Array<Any>?) {
        Spinner.onDelete("signal", table, selection, args)
      }

      override fun onUpdate(table: String, values: ContentValues, selection: String?, args: Array<Any>?) {
        Spinner.onUpdate("signal", table, values, selection, args)
      }
    })

    LeakCanary.config = LeakCanary.config.copy(
      referenceMatchers = AndroidReferenceMatchers.appDefaults +
        AndroidReferenceMatchers.ignoredInstanceField(
          className = "android.service.media.MediaBrowserService\$ServiceBinder",
          fieldName = "this\$0"
        ) +
        AndroidReferenceMatchers.ignoredInstanceField(
          className = "androidx.media.MediaBrowserServiceCompat\$MediaBrowserServiceImplApi26\$MediaBrowserServiceApi26",
          fieldName = "mBase"
        ) +
        AndroidReferenceMatchers.ignoredInstanceField(
          className = "android.support.v4.media.MediaBrowserCompat",
          fieldName = "mImpl"
        ) +
        AndroidReferenceMatchers.ignoredInstanceField(
          className = "android.support.v4.media.session.MediaControllerCompat",
          fieldName = "mToken"
        ) +
        AndroidReferenceMatchers.ignoredInstanceField(
          className = "android.support.v4.media.session.MediaControllerCompat",
          fieldName = "mImpl"
        ) +
        AndroidReferenceMatchers.ignoredInstanceField(
          className = "org.thoughtcrime.securesms.components.voice.VoiceNotePlaybackService",
          fieldName = "mApplication"
        ) +
        AndroidReferenceMatchers.ignoredInstanceField(
          className = "org.thoughtcrime.securesms.service.GenericForegroundService\$LocalBinder",
          fieldName = "this\$0"
        ) +
        AndroidReferenceMatchers.ignoredInstanceField(
          className = "org.thoughtcrime.securesms.contacts.ContactsSyncAdapter",
          fieldName = "mContext"
        )
    )
  }
}
