/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.backup.v2.ui.subscription

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import io.reactivex.rxjava3.processors.PublishProcessor
import org.signal.donations.InAppPaymentType
import org.thoughtcrime.securesms.components.settings.app.subscription.donate.DonationCheckoutDelegate
import org.thoughtcrime.securesms.components.settings.app.subscription.donate.DonationProcessorAction
import org.thoughtcrime.securesms.compose.ComposeFragment
import org.thoughtcrime.securesms.compose.Nav
import org.thoughtcrime.securesms.database.InAppPaymentTable
import org.thoughtcrime.securesms.util.navigation.safeNavigate
import org.thoughtcrime.securesms.util.viewModel

/**
 * Handles the selection, payment, and changing of a user's backup tier.
 */
class MessageBackupsFlowFragment : ComposeFragment(), DonationCheckoutDelegate.Callback {

  private val viewModel: MessageBackupsFlowViewModel by viewModel { MessageBackupsFlowViewModel() }

  private val inAppPaymentIdProcessor = PublishProcessor.create<InAppPaymentTable.InAppPaymentId>()

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun FragmentContent() {
    val state by viewModel.state
    val navController = rememberNavController()

    val checkoutDelegate = remember {
      DonationCheckoutDelegate(this, this, inAppPaymentIdProcessor)
    }

    LaunchedEffect(state.inAppPayment?.id) {
      val inAppPaymentId = state.inAppPayment?.id
      if (inAppPaymentId != null) {
        inAppPaymentIdProcessor.onNext(inAppPaymentId)
      }
    }

    val checkoutSheetState = rememberModalBottomSheetState(
      skipPartiallyExpanded = true
    )

    LaunchedEffect(Unit) {
      navController.setLifecycleOwner(this@MessageBackupsFlowFragment)
      navController.setOnBackPressedDispatcher(requireActivity().onBackPressedDispatcher)
      navController.enableOnBackPressed(true)
    }

    Nav.Host(
      navController = navController,
      startDestination = state.startScreen.name
    ) {
      composable(route = MessageBackupsScreen.EDUCATION.name) {
        MessageBackupsEducationScreen(
          onNavigationClick = viewModel::goToPreviousScreen,
          onEnableBackups = viewModel::goToNextScreen,
          onLearnMore = {}
        )
      }

      composable(route = MessageBackupsScreen.PIN_EDUCATION.name) {
        MessageBackupsPinEducationScreen(
          onNavigationClick = viewModel::goToPreviousScreen,
          onGeneratePinClick = {},
          onUseCurrentPinClick = viewModel::goToNextScreen,
          recommendedPinSize = 16 // TODO [message-backups] This value should come from some kind of config
        )
      }

      composable(route = MessageBackupsScreen.PIN_CONFIRMATION.name) {
        MessageBackupsPinConfirmationScreen(
          pin = state.pin,
          onPinChanged = viewModel::onPinEntryUpdated,
          pinKeyboardType = state.pinKeyboardType,
          onPinKeyboardTypeSelected = viewModel::onPinKeyboardTypeUpdated,
          onNextClick = viewModel::goToNextScreen
        )
      }

      composable(route = MessageBackupsScreen.TYPE_SELECTION.name) {
        MessageBackupsTypeSelectionScreen(
          selectedBackupTier = state.selectedMessageBackupTier,
          availableBackupTypes = state.availableBackupTypes,
          onMessageBackupsTierSelected = viewModel::onMessageBackupTierUpdated,
          onNavigationClick = viewModel::goToPreviousScreen,
          onReadMoreClicked = {},
          onNextClicked = viewModel::goToNextScreen
        )

        if (state.screen == MessageBackupsScreen.CHECKOUT_SHEET) {
          MessageBackupsCheckoutSheet(
            messageBackupsType = state.availableBackupTypes.first { it.tier == state.selectedMessageBackupTier!! },
            availablePaymentMethods = state.availablePaymentMethods,
            sheetState = checkoutSheetState,
            onDismissRequest = {
              viewModel.goToPreviousScreen()
            },
            onPaymentMethodSelected = {
              viewModel.onPaymentMethodUpdated(it)
              viewModel.goToNextScreen()
            }
          )
        }
      }
    }

    LaunchedEffect(state.screen) {
      val route = navController.currentDestination?.route ?: return@LaunchedEffect
      if (route == state.screen.name) {
        return@LaunchedEffect
      }

      if (state.screen == MessageBackupsScreen.COMPLETED) {
        if (!findNavController().popBackStack()) {
          requireActivity().finishAfterTransition()
        }
        return@LaunchedEffect
      }

      if (state.screen == MessageBackupsScreen.PROCESS_PAYMENT) {
        checkoutDelegate.handleGatewaySelectionResponse(state.inAppPayment!!)
        viewModel.goToPreviousScreen()
        return@LaunchedEffect
      }

      if (state.screen == MessageBackupsScreen.CHECKOUT_SHEET) {
        return@LaunchedEffect
      }

      val routeScreen = MessageBackupsScreen.valueOf(route)
      if (routeScreen.isAfter(state.screen)) {
        navController.popBackStack()
      } else {
        navController.navigate(state.screen.name)
      }
    }
  }

  override fun navigateToStripePaymentInProgress(inAppPayment: InAppPaymentTable.InAppPayment) {
    findNavController().safeNavigate(
      MessageBackupsFlowFragmentDirections.actionDonateToSignalFragmentToStripePaymentInProgressFragment(
        DonationProcessorAction.PROCESS_NEW_DONATION,
        inAppPayment,
        inAppPayment.type
      )
    )
  }

  override fun navigateToPayPalPaymentInProgress(inAppPayment: InAppPaymentTable.InAppPayment) {
    findNavController().safeNavigate(
      MessageBackupsFlowFragmentDirections.actionDonateToSignalFragmentToPaypalPaymentInProgressFragment(
        DonationProcessorAction.PROCESS_NEW_DONATION,
        inAppPayment,
        inAppPayment.type
      )
    )
  }

  override fun navigateToCreditCardForm(inAppPayment: InAppPaymentTable.InAppPayment) {
    findNavController().safeNavigate(
      MessageBackupsFlowFragmentDirections.actionDonateToSignalFragmentToCreditCardFragment(inAppPayment)
    )
  }

  override fun navigateToIdealDetailsFragment(inAppPayment: InAppPaymentTable.InAppPayment) {
    findNavController().safeNavigate(
      MessageBackupsFlowFragmentDirections.actionDonateToSignalFragmentToIdealTransferDetailsFragment(inAppPayment)
    )
  }

  override fun navigateToBankTransferMandate(inAppPayment: InAppPaymentTable.InAppPayment) {
    findNavController().safeNavigate(
      MessageBackupsFlowFragmentDirections.actionDonateToSignalFragmentToBankTransferMandateFragment(inAppPayment)
    )
  }

  override fun onPaymentComplete(inAppPayment: InAppPaymentTable.InAppPayment) {
    // TODO [message-backups] What do? probably some kind of success thing?
    if (!findNavController().popBackStack()) {
      requireActivity().finishAfterTransition()
    }
  }

  override fun onSubscriptionCancelled(inAppPaymentType: InAppPaymentType) = error("This view doesn't support cancellation, that is done elsewhere.")

  override fun onProcessorActionProcessed() = Unit

  override fun onUserLaunchedAnExternalApplication() {
    // TODO [message-backups] What do? Are we even supporting bank transfers?
  }

  override fun navigateToDonationPending(inAppPayment: InAppPaymentTable.InAppPayment) {
    // TODO [message-backups] What do? Are we even supporting bank transfers?
  }
}
