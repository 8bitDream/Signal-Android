package org.thoughtcrime.securesms.mediasend;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.util.SingleLiveEvent;
import org.thoughtcrime.securesms.util.Util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class CameraContactSelectionViewModel extends ViewModel {

  static final int MAX_SELECTION_COUNT = 16;

  private final CameraContactsRepository      repository;
  private final MutableLiveData<ContactState> contacts;
  private final SingleLiveEvent<Error>        error;
  private final Set<Recipient>                selected;

  private CameraContactSelectionViewModel(@NonNull CameraContactsRepository repository) {
    this.repository = repository;
    this.contacts   = new MutableLiveData<>();
    this.error      = new SingleLiveEvent<>();
    this.selected   = new LinkedHashSet<>();

    repository.getCameraContacts(cameraContacts -> {
      Util.runOnMain(() -> {
        contacts.postValue(new ContactState(cameraContacts, new ArrayList<>(selected)));
      });
    });
  }

  LiveData<ContactState> getContacts() {
    return contacts;
  }

  LiveData<Error> getError() {
    return error;
  }

  void onSearchClosed() {
    onQueryUpdated("");
  }

  void onQueryUpdated(String query) {
    repository.getCameraContacts(query, cameraContacts -> {
      Util.runOnMain(() -> {
        contacts.postValue(new ContactState(cameraContacts, new ArrayList<>(selected)));
      });
    });
  }

  void onRefresh() {
    repository.getCameraContacts(cameraContacts -> {
      Util.runOnMain(() -> {
        contacts.postValue(new ContactState(cameraContacts, new ArrayList<>(selected)));
      });
    });
  }

  void onContactClicked(@NonNull Recipient recipient) {
    if (selected.contains(recipient)) {
      selected.remove(recipient);
    } else if (selected.size() < MAX_SELECTION_COUNT) {
      selected.add(recipient);
    } else {
      error.postValue(Error.MAX_SELECTION);
    }

    ContactState currentState = contacts.getValue();

    if (currentState != null) {
      contacts.setValue(new ContactState(currentState.getContacts(), new ArrayList<>(selected)));
    }
  }

  static class ContactState {
    private final CameraContacts contacts;
    private final List<Recipient> selected;

    ContactState(CameraContacts contacts, List<Recipient> selected) {
      this.contacts = contacts;
      this.selected = selected;
    }

    public CameraContacts getContacts() {
      return contacts;
    }

    public List<Recipient> getSelected() {
      return selected;
    }
  }

  enum Error {
    MAX_SELECTION
  }

  static class Factory extends ViewModelProvider.NewInstanceFactory {

    private final CameraContactsRepository repository;

    Factory(CameraContactsRepository repository) {
      this.repository = repository;
    }

    @Override
    public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      //noinspection ConstantConditions
      return modelClass.cast(new CameraContactSelectionViewModel(repository));
    }
  }

}
