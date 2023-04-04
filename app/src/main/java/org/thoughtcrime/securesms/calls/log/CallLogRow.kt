package org.thoughtcrime.securesms.calls.log

import org.thoughtcrime.securesms.database.CallTable
import org.thoughtcrime.securesms.recipients.Recipient

/**
 * A row to be displayed in the call log
 */
sealed class CallLogRow {

  abstract val id: Id

  /**
   * An incoming, outgoing, or missed call.
   */
  data class Call(
    val call: CallTable.Call,
    val peer: Recipient,
    val date: Long,
    override val id: Id = Id.Call(call.callId)
  ) : CallLogRow()

  /**
   * A row which can be used to clear the current filter.
   */
  object ClearFilter : CallLogRow() {
    override val id: Id = Id.ClearFilter
  }

  object CreateCallLink : CallLogRow() {
    override val id: Id = Id.CreateCallLink
  }

  sealed class Id {
    data class Call(val callId: Long) : Id()
    object ClearFilter : Id()
    object CreateCallLink : Id()
  }
}
