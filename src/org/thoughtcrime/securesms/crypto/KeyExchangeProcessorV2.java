package org.thoughtcrime.securesms.crypto;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.thoughtcrime.securesms.crypto.protocol.KeyExchangeMessage;
import org.thoughtcrime.securesms.crypto.protocol.KeyExchangeMessageV2;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.service.KeyCachingService;
import org.thoughtcrime.securesms.sms.MessageSender;
import org.thoughtcrime.securesms.sms.OutgoingKeyExchangeMessage;
import org.whispersystems.textsecure.crypto.IdentityKey;
import org.whispersystems.textsecure.crypto.IdentityKeyPair;
import org.whispersystems.textsecure.crypto.InvalidKeyException;
import org.whispersystems.textsecure.crypto.InvalidMessageException;
import org.whispersystems.textsecure.crypto.MasterSecret;
import org.whispersystems.textsecure.crypto.ecc.Curve;
import org.whispersystems.textsecure.crypto.ecc.ECKeyPair;
import org.whispersystems.textsecure.crypto.ecc.ECPublicKey;
import org.whispersystems.textsecure.crypto.protocol.PreKeyWhisperMessage;
import org.whispersystems.textsecure.crypto.ratchet.RatchetingSession;
import org.whispersystems.textsecure.push.PreKeyEntity;
import org.whispersystems.textsecure.storage.InvalidKeyIdException;
import org.whispersystems.textsecure.storage.PreKeyRecord;
import org.whispersystems.textsecure.storage.Session;
import org.whispersystems.textsecure.storage.SessionRecordV2;
import org.whispersystems.textsecure.util.Medium;

/**
 * This class processes key exchange interactions.
 *
 * @author Moxie Marlinspike
 */

public class KeyExchangeProcessorV2 extends KeyExchangeProcessor {

  private Context         context;
  private Recipient       recipient;
  private MasterSecret    masterSecret;
  private SessionRecordV2 sessionRecord;

  public KeyExchangeProcessorV2(Context context, MasterSecret masterSecret, Recipient recipient) {
    this.context         = context;
    this.recipient       = recipient;
    this.masterSecret    = masterSecret;
    this.sessionRecord   = new SessionRecordV2(context, masterSecret, recipient);
  }

  public boolean isTrusted(PreKeyWhisperMessage message) {
    return isTrusted(message.getIdentityKey());
  }

  public boolean isTrusted(KeyExchangeMessage message) {
    return message.hasIdentityKey() && isTrusted(message.getIdentityKey());
  }

  public boolean isTrusted(IdentityKey identityKey) {
    return DatabaseFactory.getIdentityDatabase(context).isValidIdentity(masterSecret, recipient,
                                                                        identityKey);
  }

  public boolean isStale(KeyExchangeMessage m) {
    KeyExchangeMessageV2 message = (KeyExchangeMessageV2)m;
    return
        message.isResponse() &&
            (!sessionRecord.hasPendingKeyExchange() ||
              sessionRecord.getPendingKeyExchangeSequence() != message.getSequence()) &&
        !message.isResponseForSimultaneousInitiate();
  }

  public void processKeyExchangeMessage(PreKeyWhisperMessage message)
      throws InvalidKeyIdException, InvalidKeyException
  {
    int         preKeyId          = message.getPreKeyId();
    ECPublicKey theirBaseKey      = message.getBaseKey();
    ECPublicKey theirEphemeralKey = message.getWhisperMessage().getSenderEphemeral();
    IdentityKey theirIdentityKey  = message.getIdentityKey();

    Log.w("KeyExchangeProcessor", "Received pre-key with local key ID: " + preKeyId);

    if (!PreKeyRecord.hasRecord(context, preKeyId) && Session.hasSession(context, masterSecret, recipient)) {
      Log.w("KeyExchangeProcessor", "We've already processed the prekey part, letting bundled message fall through...");
      return;
    }

    if (!PreKeyRecord.hasRecord(context, preKeyId))
      throw new InvalidKeyIdException("No such prekey: " + preKeyId);

    PreKeyRecord    preKeyRecord    = new PreKeyRecord(context, masterSecret, preKeyId);
    ECKeyPair       ourBaseKey      = preKeyRecord.getKeyPair();
    ECKeyPair       ourEphemeralKey = ourBaseKey;
    IdentityKeyPair ourIdentityKey  = IdentityKeyUtil.getIdentityKeyPair(context, masterSecret, ourBaseKey.getPublicKey().getType());

    sessionRecord.clear();

    RatchetingSession.initializeSession(sessionRecord, ourBaseKey, theirBaseKey, ourEphemeralKey,
                                        theirEphemeralKey, ourIdentityKey, theirIdentityKey);

    sessionRecord.save();

    if (preKeyId != Medium.MAX_VALUE) {
      PreKeyRecord.delete(context, preKeyId);
    }

    DatabaseFactory.getIdentityDatabase(context)
                   .saveIdentity(masterSecret, recipient, theirIdentityKey);
  }

  public void processKeyExchangeMessage(PreKeyEntity message, long threadId)
      throws InvalidKeyException
  {
    ECKeyPair       ourBaseKey        = Curve.generateKeyPairForSession(2);
    ECKeyPair       ourEphemeralKey   = Curve.generateKeyPairForSession(2);
    ECPublicKey     theirBaseKey      = message.getPublicKey();
    ECPublicKey     theirEphemeralKey = theirBaseKey;
    IdentityKey     theirIdentityKey  = message.getIdentityKey();
    IdentityKeyPair ourIdentityKey    = IdentityKeyUtil.getIdentityKeyPair(context, masterSecret,
                                                                           ourBaseKey.getPublicKey()
                                                                                     .getType());

    sessionRecord.clear();

    RatchetingSession.initializeSession(sessionRecord, ourBaseKey, theirBaseKey, ourEphemeralKey,
                                        theirEphemeralKey, ourIdentityKey, theirIdentityKey);

    sessionRecord.setPendingPreKey(message.getKeyId(), ourBaseKey.getPublicKey());
    sessionRecord.save();

    DatabaseFactory.getIdentityDatabase(context)
                   .saveIdentity(masterSecret, recipient, message.getIdentityKey());

    broadcastSecurityUpdateEvent(context, threadId);
  }

  @Override
  public void processKeyExchangeMessage(KeyExchangeMessage _message, long threadId)
      throws InvalidMessageException
  {
    try {
      KeyExchangeMessageV2 message = (KeyExchangeMessageV2)_message;

      Log.w("KeyExchangeProcessorV2", "Received key exchange with sequence: " + message.getSequence());

      if (message.isInitiate()) {
        ECKeyPair       ourBaseKey, ourEphemeralKey;
        IdentityKeyPair ourIdentityKey;

        int flags = KeyExchangeMessageV2.RESPONSE_FLAG;

        Log.w("KeyExchangeProcessorV2", "KeyExchange is an initiate.");

        if (!sessionRecord.hasPendingKeyExchange()) {
          Log.w("KeyExchangeProcessorV2", "We don't have a pending initiate...");
          ourBaseKey      = Curve.generateKeyPairForType(message.getBaseKey().getType());
          ourEphemeralKey = Curve.generateKeyPairForType(message.getBaseKey().getType());
          ourIdentityKey  = IdentityKeyUtil.getIdentityKeyPair(context, masterSecret, message.getBaseKey().getType());

          sessionRecord.setPendingKeyExchange(message.getSequence(), ourBaseKey, ourEphemeralKey,
                                              ourIdentityKey);
        } else {
          Log.w("KeyExchangeProcessorV2", "We alredy have a pending initiate, responding as simultaneous initiate...");
          ourBaseKey      = sessionRecord.getPendingKeyExchangeBaseKey();
          ourEphemeralKey = sessionRecord.getPendingKeyExchangeEphemeralKey();
          ourIdentityKey  = sessionRecord.getPendingKeyExchangeIdentityKey();
          flags          |= KeyExchangeMessageV2.SIMULTAENOUS_INITIATE_FLAG;

          sessionRecord.setPendingKeyExchange(message.getSequence(), ourBaseKey, ourEphemeralKey,
                                              ourIdentityKey);
        }

        KeyExchangeMessageV2 ourMessage = new KeyExchangeMessageV2(message.getSequence(),
                                                                   flags, ourBaseKey.getPublicKey(),
                                                                   ourEphemeralKey.getPublicKey(),
                                                                   ourIdentityKey.getPublicKey());

        OutgoingKeyExchangeMessage textMessage = new OutgoingKeyExchangeMessage(recipient,
                                                                                ourMessage.serialize());
        MessageSender.send(context, masterSecret, textMessage, threadId);
      }

      if (message.getSequence() != sessionRecord.getPendingKeyExchangeSequence()) {
        Log.w("KeyExchangeProcessorV2", "No matching sequence for response. " +
            "Is simultaneous initiate response: " + message.isResponseForSimultaneousInitiate());
        return;
      }

      ECKeyPair       ourBaseKey      = sessionRecord.getPendingKeyExchangeBaseKey();
      ECKeyPair       ourEphemeralKey = sessionRecord.getPendingKeyExchangeEphemeralKey();
      IdentityKeyPair ourIdentityKey  = sessionRecord.getPendingKeyExchangeIdentityKey();

      sessionRecord.clear();

      RatchetingSession.initializeSession(sessionRecord, ourBaseKey, message.getBaseKey(),
                                          ourEphemeralKey, message.getEphemeralKey(),
                                          ourIdentityKey, message.getIdentityKey());

      sessionRecord.setSessionVersion(message.getVersion());
      Session.clearV1SessionFor(context, recipient);
      sessionRecord.save();

      DatabaseFactory.getIdentityDatabase(context)
                     .saveIdentity(masterSecret, recipient, message.getIdentityKey());

      DecryptingQueue.scheduleRogueMessages(context, masterSecret, recipient);

      broadcastSecurityUpdateEvent(context, threadId);
    } catch (InvalidKeyException e) {
      throw new InvalidMessageException(e);
    }
  }

  private static void broadcastSecurityUpdateEvent(Context context, long threadId) {
    Intent intent = new Intent(KeyExchangeProcessorV1.SECURITY_UPDATE_EVENT);
    intent.putExtra("thread_id", threadId);
    intent.setPackage(context.getPackageName());
    context.sendBroadcast(intent, KeyCachingService.KEY_PERMISSION);
  }

}
