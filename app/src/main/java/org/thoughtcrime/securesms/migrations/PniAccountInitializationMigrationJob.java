package org.thoughtcrime.securesms.migrations;

import androidx.annotation.NonNull;

import org.signal.core.util.logging.Log;
import org.thoughtcrime.securesms.crypto.PreKeyUtil;
import org.thoughtcrime.securesms.crypto.storage.PreKeyMetadataStore;
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies;
import org.thoughtcrime.securesms.jobmanager.Data;
import org.thoughtcrime.securesms.jobmanager.Job;
import org.thoughtcrime.securesms.jobmanager.impl.NetworkConstraint;
import org.thoughtcrime.securesms.jobs.KbsEnclaveMigrationWorkerJob;
import org.thoughtcrime.securesms.keyvalue.SignalStore;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.push.PNI;
import org.whispersystems.signalservice.api.push.ServiceIdType;

import java.io.IOException;
import java.util.List;

/**
 * Initializes various aspects of the PNI identity. Notably:
 * - Creates an identity key
 * - Creates and uploads one-time prekeys
 * - Creates and uploads signed prekeys
 */
public class PniAccountInitializationMigrationJob extends MigrationJob {

  private static final String TAG = Log.tag(PniAccountInitializationMigrationJob.class);

  public static final String KEY = "PniAccountInitializationMigrationJob";

  PniAccountInitializationMigrationJob() {
    this(new Parameters.Builder()
                       .addConstraint(NetworkConstraint.KEY)
                       .build());
  }

  private PniAccountInitializationMigrationJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  public boolean isUiBlocking() {
    return false;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void performMigration() throws IOException {
    PNI pni = SignalStore.account().getPni();

    if (pni == null || SignalStore.account().getAci() == null || !Recipient.self().isRegistered()) {
      Log.w(TAG, "Not yet registered! No need to perform this migration.");
      return;
    }

    SignalStore.account().generatePniIdentityKey();

    SignalServiceAccountManager accountManager = ApplicationDependencies.getSignalServiceAccountManager();
    SignalProtocolStore         protocolStore  = ApplicationDependencies.getProtocolStore().pni();
    PreKeyMetadataStore         metadataStore  = SignalStore.account().pniPreKeys();

    SignedPreKeyRecord signedPreKey   = PreKeyUtil.generateAndStoreSignedPreKey(protocolStore, metadataStore, true);
    List<PreKeyRecord> oneTimePreKeys = PreKeyUtil.generateAndStoreOneTimePreKeys(protocolStore, metadataStore);

    accountManager.setPreKeys(ServiceIdType.PNI, protocolStore.getIdentityKeyPair().getPublicKey(), signedPreKey, oneTimePreKeys);
    metadataStore.setSignedPreKeyRegistered(true);
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return e instanceof IOException;
  }

  public static class Factory implements Job.Factory<PniAccountInitializationMigrationJob> {
    @Override
    public @NonNull PniAccountInitializationMigrationJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new PniAccountInitializationMigrationJob(parameters);
    }
  }
}
