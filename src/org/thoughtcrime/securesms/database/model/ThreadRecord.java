/**
 * Copyright (C) 2012 Moxie Marlinspike
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
package org.thoughtcrime.securesms.database.model;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Pair;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.database.SmsDatabase;
import org.thoughtcrime.securesms.recipients.Recipients;
import org.thoughtcrime.securesms.util.GroupUtil;
import org.whispersystems.textsecure.push.PushMessageProtos;
import org.whispersystems.textsecure.util.Util;

import java.util.List;

import static org.whispersystems.textsecure.push.PushMessageProtos.PushMessageContent.GroupContext;

/**
 * The message record model which represents thread heading messages.
 *
 * @author Moxie Marlinspike
 *
 */
public class ThreadRecord extends DisplayRecord {

  private final Context context;
  private final long count;
  private final boolean read;
  private final int distributionType;

  public ThreadRecord(Context context, Body body, Recipients recipients, long date,
                      long count, boolean read, long threadId, long snippetType,
                      int distributionType, int groupAction, String groupActionArg)
  {
    super(context, body, recipients, date, date, threadId, snippetType, groupAction, groupActionArg);
    this.context          = context.getApplicationContext();
    this.count            = count;
    this.read             = read;
    this.distributionType = distributionType;
  }

  @Override
  public SpannableString getDisplayBody() {
    // TODO jake is going to fill these in
    if (SmsDatabase.Types.isDecryptInProgressType(type)) {
      return emphasisAdded(context.getString(R.string.MessageDisplayHelper_decrypting_please_wait));
    } else if (getGroupAction() == GroupContext.Type.ADD_VALUE ||
               getGroupAction() == GroupContext.Type.CREATE_VALUE)
    {
      return emphasisAdded(Util.join(GroupUtil.getSerializedArgumentMembers(getGroupActionArguments()), ", ") + " have joined the group");
    } else if (getGroupAction() == GroupContext.Type.QUIT_VALUE) {
      return emphasisAdded(getRecipients().toShortString() + " left the group.");
    } else if (getGroupAction() == GroupContext.Type.MODIFY_VALUE) {
      return emphasisAdded(getRecipients().toShortString() + " modified the group.");
    } else if (isKeyExchange()) {
      return emphasisAdded(context.getString(R.string.ConversationListItem_key_exchange_message));
    } else if (SmsDatabase.Types.isFailedDecryptType(type)) {
      return emphasisAdded(context.getString(R.string.MessageDisplayHelper_bad_encrypted_message));
    } else if (SmsDatabase.Types.isNoRemoteSessionType(type)) {
      return emphasisAdded(context.getString(R.string.MessageDisplayHelper_message_encrypted_for_non_existing_session));
    } else if (!getBody().isPlaintext()) {
      return emphasisAdded(context.getString(R.string.MessageNotifier_encrypted_message));
    } else if (SmsDatabase.Types.isEndSessionType(type)) {
      // TODO jake is going to fix this up
      return emphasisAdded("Session closed!");
    } else {
      if (Util.isEmpty(getBody().getBody())) {
        return new SpannableString(context.getString(R.string.MessageNotifier_no_subject));
      } else {
        return new SpannableString(getBody().getBody());
      }
    }
  }

  private SpannableString emphasisAdded(String sequence) {
    SpannableString spannable = new SpannableString(sequence);
    spannable.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0,
                      sequence.length(),
                      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    return spannable;
  }

  public long getCount() {
    return count;
  }

  public boolean isRead() {
    return read;
  }

  public long getDate() {
    return getDateReceived();
  }

  public int getDistributionType() {
    return distributionType;
  }
}
