/**
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.google.android.gcm.GCMRegistrar;
import org.thoughtcrime.securesms.contacts.ContactAccessor;
import org.thoughtcrime.securesms.contacts.ContactIdentityManager;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.crypto.MasterSecretUtil;
import org.thoughtcrime.securesms.service.KeyCachingService;
import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.MemoryCleaner;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.Trimmer;
import org.thoughtcrime.securesms.util.Util;
import org.whispersystems.textsecure.push.PushServiceSocket;
import org.whispersystems.textsecure.push.RateLimitException;

import java.io.IOException;

/**
 * The Activity for application preference display and management.
 *
 * @author Moxie Marlinspike
 *
 */

public class ApplicationPreferencesActivity extends PassphraseRequiredSherlockPreferenceActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener
{

  private static final int PICK_IDENTITY_CONTACT        = 1;
  private static final int ENABLE_PASSPHRASE_ACTIVITY   = 2;

<<<<<<< HEAD
  public static final String RINGTONE_PREF                    = "pref_key_ringtone";
  public static final String IN_THREAD_NOTIFICATION_PREF      = "pref_key_inthread_notifications";
  public static final String VIBRATE_PREF                     = "pref_key_vibrate";
  public static final String NOTIFICATION_PREF                = "pref_key_enable_notifications";
  public static final String LED_COLOR_PREF                   = "pref_led_color";
  public static final String LED_BLINK_PREF                   = "pref_led_blink";
  public static final String LED_BLINK_PREF_CUSTOM            = "pref_led_blink_custom";
  public static final String IDENTITY_PREF                    = "pref_choose_identity";
  public static final String ALL_SMS_PREF                     = "pref_all_sms";
  public static final String ALL_MMS_PERF                     = "pref_all_mms";
  public static final String KITKAT_DEFAULT_PREF              = "pref_set_default";
  public static final String PASSPHRASE_TIMEOUT_INTERVAL_PREF = "pref_timeout_interval";
  public static final String PASSPHRASE_TIMEOUT_PREF          = "pref_timeout_passphrase";
  public static final String AUTO_KEY_EXCHANGE_PREF           = "pref_auto_complete_key_exchange";
  public static final String THEME_PREF                       = "pref_theme";
  public static final String LANGUAGE_PREF                    = "pref_language";
  public static final String ENTER_SENDS_PREF                 = "pref_enter_sends";
  public static final String ENTER_PRESENT_PREF               = "pref_enter_key";

  private static final String DISPLAY_CATEGORY_PREF        = "pref_display_category";
  private static final String PUSH_MESSAGING_PREF       = "pref_toggle_push_messaging";

  private static final String CHANGE_PASSPHRASE_PREF	     = "pref_change_passphrase";
  public  static final String DISABLE_PASSPHRASE_PREF      = "pref_disable_passphrase";

  public static final String MMS_PREF               = "pref_mms_preferences";
  public static final String ENABLE_MANUAL_MMS_PREF = "pref_enable_manual_mms";
  public static final String MMSC_HOST_PREF         = "pref_apn_mmsc_host";
  public static final String MMSC_PROXY_HOST_PREF   = "pref_apn_mms_proxy";
  public static final String MMSC_PROXY_PORT_PREF   = "pref_apn_mms_proxy_port";

  public static final String SMS_DELIVERY_REPORT_PREF = "pref_delivery_report_sms";

  public static final String THREAD_TRIM_ENABLED = "pref_trim_threads";
  public static final String THREAD_TRIM_LENGTH  = "pref_trim_length";
  public static final String THREAD_TRIM_NOW     = "pref_trim_now";

  public static final String LOCAL_NUMBER_PREF    = "pref_local_number";
  public static final String VERIFYING_STATE_PREF = "pref_verifying";
  public static final String REGISTERED_GCM_PREF  = "pref_gcm_registered";
  public static final String GCM_PASSWORD_PREF    = "pref_gcm_password";

  private final DynamicTheme    dynamicTheme    = new DynamicTheme();
  private final DynamicLanguage dynamicLanguage = new DynamicLanguage();

  @Override
  protected void onCreate(Bundle icicle) {
    dynamicTheme.onCreate(this);
    dynamicLanguage.onCreate(this);
    super.onCreate(icicle);

    this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    addPreferencesFromResource(R.xml.preferences);

    initializeIdentitySelection();
    initializePlatformSpecificOptions();
    initializePushMessagingToggle();
    initializeEditTextSummaries();

    this.findPreference(TextSecurePreferences.CHANGE_PASSPHRASE_PREF)
      .setOnPreferenceClickListener(new ChangePassphraseClickListener());
    this.findPreference(TextSecurePreferences.THREAD_TRIM_NOW)
      .setOnPreferenceClickListener(new TrimNowClickListener());
    this.findPreference(TextSecurePreferences.THREAD_TRIM_LENGTH)
      .setOnPreferenceChangeListener(new TrimLengthValidationListener());
    this.findPreference(TextSecurePreferences.DISABLE_PASSPHRASE_PREF)
      .setOnPreferenceChangeListener(new DisablePassphraseClickListener());
    this.findPreference(MMS_PREF)
      .setOnPreferenceClickListener(new ApnPreferencesClickListener());
    this.findPreference(LED_COLOR_PREF)
      .setOnPreferenceChangeListener(new ListSummaryListener());
    this.findPreference(LED_BLINK_PREF)
      .setOnPreferenceChangeListener(new ListSummaryListener());
    this.findPreference(RINGTONE_PREF)
      .setOnPreferenceChangeListener(new RingtoneSummaryListener());

    initializeListSummary((ListPreference) findPreference(LED_COLOR_PREF));
    initializeListSummary((ListPreference) findPreference(LED_BLINK_PREF));
    initializeRingtoneSummary((RingtonePreference) findPreference(RINGTONE_PREF));
  }

  @Override
  public void onStart() {
    super.onStart();
    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
    dynamicLanguage.onResume(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onDestroy() {
    MemoryCleaner.clean((MasterSecret) getIntent().getParcelableExtra("master_secret"));
    super.onDestroy();
  }

  @Override
  public void onActivityResult(int reqCode, int resultCode, Intent data) {
    super.onActivityResult(reqCode, resultCode, data);

    Log.w("ApplicationPreferencesActivity", "Got result: " + resultCode + " for req: " + reqCode);

    if (resultCode == Activity.RESULT_OK) {
      switch (reqCode) {
      case PICK_IDENTITY_CONTACT:      handleIdentitySelection(data); break;
      case ENABLE_PASSPHRASE_ACTIVITY: finish();                      break;
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
      Intent intent = new Intent(this, ConversationListActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      finish();
      return true;
    }

    return false;
  }

  private void initializePlatformSpecificOptions() {
    PreferenceGroup generalCategory = (PreferenceGroup)findPreference("general_category");
    Preference defaultPreference = findPreference(KITKAT_DEFAULT_PREF);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      generalCategory.removePreference(findPreference(ALL_SMS_PREF));
      generalCategory.removePreference(findPreference(ALL_MMS_PERF));

      if (Util.isDefaultSmsProvider(this)) {
        generalCategory.removePreference(defaultPreference);
      } else {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());

        defaultPreference.setIntent(intent);
      }
    } else {
      generalCategory.removePreference(defaultPreference);
    }
  }

  private void initializeEditTextSummary(final EditTextPreference preference) {
    if (preference.getText() == null) {
      preference.setSummary("Not set");
    } else {
      preference.setSummary(preference.getText());
    }

    preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        preference.setSummary(newValue == null ? "Not set" : ((String) newValue));
        return true;
      }
    });
  }

  private void initializeEditTextSummaries() {
    initializeEditTextSummary((EditTextPreference)this.findPreference(TextSecurePreferences.MMSC_HOST_PREF));
    initializeEditTextSummary((EditTextPreference)this.findPreference(TextSecurePreferences.MMSC_PROXY_HOST_PREF));
    initializeEditTextSummary((EditTextPreference)this.findPreference(TextSecurePreferences.MMSC_PROXY_PORT_PREF));
  }

  private void initializePushMessagingToggle() {
    CheckBoxPreference preference = (CheckBoxPreference)this.findPreference(PUSH_MESSAGING_PREF);
    preference.setChecked(TextSecurePreferences.isPushRegistered(this));
    preference.setOnPreferenceChangeListener(new PushMessagingClickListener());
  }

  private void initializeIdentitySelection() {
    ContactIdentityManager identity = ContactIdentityManager.getInstance(this);

    if (identity.isSelfIdentityAutoDetected()) {
      Preference preference = this.findPreference(DISPLAY_CATEGORY_PREF);
      this.getPreferenceScreen().removePreference(preference);
    } else {
      Uri contactUri = identity.getSelfIdentityUri();

      if (contactUri != null) {
        String contactName = ContactAccessor.getInstance().getNameFromContact(this, contactUri);
        this.findPreference(TextSecurePreferences.IDENTITY_PREF)
          .setSummary(String.format(getString(R.string.ApplicationPreferencesActivity_currently_s),
                      contactName));
      }

      this.findPreference(TextSecurePreferences.IDENTITY_PREF)
        .setOnPreferenceClickListener(new IdentityPreferenceClickListener());
    }
  }

  private void initializeListSummary(ListPreference pref) {
    pref.setSummary(pref.getEntry());
  }

  private void initializeRingtoneSummary(RingtonePreference pref) {
    RingtoneSummaryListener listener =
      (RingtoneSummaryListener) pref.getOnPreferenceChangeListener();
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    listener.onPreferenceChange(pref, sharedPreferences.getString(pref.getKey(), ""));
  }

  private void handleIdentitySelection(Intent data) {
    Uri contactUri = data.getData();

    if (contactUri != null) {
      TextSecurePreferences.setIdentityContactUri(this, contactUri.toString());
      initializeIdentitySelection();
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(TextSecurePreferences.THEME_PREF)) {
      dynamicTheme.onResume(this);
    } else if (key.equals(TextSecurePreferences.LANGUAGE_PREF)) {
      dynamicLanguage.onResume(this);
    }
  }

  private class PushMessagingClickListener implements Preference.OnPreferenceChangeListener {

    private static final int SUCCESS       = 0;
    private static final int NETWORK_ERROR = 1;

    @Override
    public boolean onPreferenceChange(final Preference preference, Object newValue) {
      if (((CheckBoxPreference)preference).isChecked()) {
        new AsyncTask<Void, Void, Integer>() {
          private ProgressDialog dialog;

          @Override
          protected void onPreExecute() {
            dialog = ProgressDialog.show(ApplicationPreferencesActivity.this,
                                         getString(R.string.ApplicationPreferencesActivity_unregistering),
                                         getString(R.string.ApplicationPreferencesActivity_unregistering_for_data_based_communication),
                                         true, false);
          }

          @Override
          protected void onPostExecute(Integer result) {
            if (dialog != null)
              dialog.dismiss();

            switch (result) {
              case NETWORK_ERROR:
                Toast.makeText(ApplicationPreferencesActivity.this,
                               getString(R.string.ApplicationPreferencesActivity_error_connecting_to_server),
                               Toast.LENGTH_LONG).show();
                break;
              case SUCCESS:
                ((CheckBoxPreference)preference).setChecked(false);
                break;
            }
          }

          @Override
          protected Integer doInBackground(Void... params) {
            try {
              Context context          = ApplicationPreferencesActivity.this;
              String localNumber       = TextSecurePreferences.getLocalNumber(context);
              String pushPassword      = TextSecurePreferences.getPushServerPassword(context);
              PushServiceSocket socket = new PushServiceSocket(context, localNumber, pushPassword);

              socket.unregisterGcmId();
              GCMRegistrar.unregister(context);
              return SUCCESS;
            } catch (IOException e) {
              Log.w("ApplicationPreferencesActivity", e);
              return NETWORK_ERROR;
            } catch (RateLimitException e) {
              Log.w("ApplicationPreferencesActivity", e);
              return NETWORK_ERROR;
            }
          }
        }.execute();
      } else {
        startActivity(new Intent(ApplicationPreferencesActivity.this, RegistrationActivity.class));
      }

      return false;
    }
  }

  private class IdentityPreferenceClickListener implements Preference.OnPreferenceClickListener {
    @Override
    public boolean onPreferenceClick(Preference preference) {
      Intent intent = new Intent(Intent.ACTION_PICK);
      intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
      startActivityForResult(intent, PICK_IDENTITY_CONTACT);
      return true;
    }
  }


  private class ChangePassphraseClickListener implements Preference.OnPreferenceClickListener {
    @Override
    public boolean onPreferenceClick(Preference preference) {
       if (MasterSecretUtil.isPassphraseInitialized(ApplicationPreferencesActivity.this)) {
        startActivity(new Intent(ApplicationPreferencesActivity.this, PassphraseChangeActivity.class));
      } else {
        Toast.makeText(ApplicationPreferencesActivity.this,
                       R.string.ApplicationPreferenceActivity_you_havent_set_a_passphrase_yet,
                       Toast.LENGTH_LONG).show();
      }

      return true;
    }
  }

  private class TrimNowClickListener implements Preference.OnPreferenceClickListener {
    @Override
    public boolean onPreferenceClick(Preference preference) {
      final int threadLengthLimit = TextSecurePreferences.getThreadTrimLength(ApplicationPreferencesActivity.this);
      AlertDialog.Builder builder = new AlertDialog.Builder(ApplicationPreferencesActivity.this);
      builder.setTitle(R.string.ApplicationPreferencesActivity_delete_all_old_messages_now);
      builder.setMessage(String.format(getString(R.string.ApplicationPreferencesActivity_are_you_sure_you_would_like_to_immediately_trim_all_conversation_threads_to_the_s_most_recent_messages),
      		                             threadLengthLimit));
      builder.setPositiveButton(R.string.ApplicationPreferencesActivity_delete,
                                new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          Trimmer.trimAllThreads(ApplicationPreferencesActivity.this, threadLengthLimit);
        }
      });

      builder.setNegativeButton(android.R.string.cancel, null);
      builder.show();

      return true;
    }
  }

  private class DisablePassphraseClickListener implements Preference.OnPreferenceChangeListener {

    @Override
    public boolean onPreferenceChange(final Preference preference, Object newValue) {
      if (!((CheckBoxPreference)preference).isChecked()) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ApplicationPreferencesActivity.this);
        builder.setTitle(R.string.ApplicationPreferencesActivity_disable_storage_encryption);
        builder.setMessage(R.string.ApplicationPreferencesActivity_warning_this_will_disable_storage_encryption_for_all_messages);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(R.string.ApplicationPreferencesActivity_disable, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            MasterSecret masterSecret = getIntent().getParcelableExtra("master_secret");
            MasterSecretUtil.changeMasterSecretPassphrase(ApplicationPreferencesActivity.this,
                                                          masterSecret,
                                                          MasterSecretUtil.UNENCRYPTED_PASSPHRASE);


            TextSecurePreferences.setPasswordDisabled(ApplicationPreferencesActivity.this, true);
            ((CheckBoxPreference)preference).setChecked(true);

            Intent intent = new Intent(ApplicationPreferencesActivity.this, KeyCachingService.class);
            intent.setAction(KeyCachingService.DISABLE_ACTION);
            startService(intent);
          }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
      } else {
        Intent intent = new Intent(ApplicationPreferencesActivity.this,
                                   PassphraseChangeActivity.class);
        startActivityForResult(intent, ENABLE_PASSPHRASE_ACTIVITY);
      }

      return false;
    }
  }

  private class TrimLengthValidationListener implements Preference.OnPreferenceChangeListener {

    public TrimLengthValidationListener() {
      EditTextPreference preference = (EditTextPreference)findPreference(TextSecurePreferences.THREAD_TRIM_LENGTH);
      preference.setSummary(preference.getText() + " " + getString(R.string.ApplicationPreferencesActivity_messages_per_conversation));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      if (newValue == null || ((String)newValue).trim().length() == 0) {
        return false;
      }

      try {
        Integer.parseInt((String)newValue);
      } catch (NumberFormatException nfe) {
        Log.w("ApplicationPreferencesActivity", nfe);
        return false;
      }

      if (Integer.parseInt((String)newValue) < 1) {
        return false;
      }

      preference.setSummary(newValue + " " +
                            getString(R.string.ApplicationPreferencesActivity_messages_per_conversation));
      return true;
    }

  }

  private class ApnPreferencesClickListener implements Preference.OnPreferenceClickListener {

    @Override
    public boolean onPreferenceClick(Preference preference) {
      startActivity(new Intent(ApplicationPreferencesActivity.this, MmsPreferencesActivity.class));
      return true;
    }
  }

  private class ListSummaryListener implements Preference.OnPreferenceChangeListener {
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
      ListPreference asList = (ListPreference) preference;

      int index = 0;
      for (; index < asList.getEntryValues().length; index++) {
        if (value.equals(asList.getEntryValues()[index])) {
          break;
        }
      }

      asList.setSummary(asList.getEntries()[index]);
      return true;
    }
  }

  private class RingtoneSummaryListener implements Preference.OnPreferenceChangeListener {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      String value = (String) newValue;

      if (TextUtils.isEmpty(value)) {
        preference.setSummary(R.string.preferences__default);
      } else {
        Ringtone tone = RingtoneManager.getRingtone(ApplicationPreferencesActivity.this,
          Uri.parse(value));
        if (tone != null) {
          preference.setSummary(tone.getTitle(ApplicationPreferencesActivity.this));
        }
      }

      return true;
    }
  }

  /* http://code.google.com/p/android/issues/detail?id=4611#c35 */
  @SuppressWarnings("deprecation")
  @Override
  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
  {
    super.onPreferenceTreeClick(preferenceScreen, preference);
    if (preference!=null)
      if (preference instanceof PreferenceScreen)
          if (((PreferenceScreen)preference).getDialog()!=null)
            ((PreferenceScreen)preference).getDialog().getWindow().getDecorView().setBackgroundDrawable(this.getWindow().getDecorView().getBackground().getConstantState().newDrawable());
    return false;
  }

}
