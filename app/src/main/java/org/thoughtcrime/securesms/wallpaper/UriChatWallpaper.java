package org.thoughtcrime.securesms.wallpaper;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import org.thoughtcrime.securesms.database.model.databaseprotos.Wallpaper;
import org.thoughtcrime.securesms.mms.GlideApp;

import java.util.Objects;

final class UriChatWallpaper implements ChatWallpaper, Parcelable {

  private final Uri   uri;
  private final float dimLevelInDarkTheme;

  public UriChatWallpaper(@NonNull Uri uri, float dimLevelInDarkTheme) {
    this.uri                 = uri;
    this.dimLevelInDarkTheme = dimLevelInDarkTheme;
  }

  @Override
  public float getDimLevelForDarkTheme() {
    return dimLevelInDarkTheme;
  }

  @Override
  public void loadInto(@NonNull ImageView imageView) {
    GlideApp.with(imageView)
            .load(uri)
            .into(imageView);
  }

  @Override
  public @NonNull Wallpaper serialize() {
    return Wallpaper.newBuilder()
                    .setFile(Wallpaper.File.newBuilder().setUri(uri.toString()))
                    .setDimLevelInDarkTheme(dimLevelInDarkTheme)
                    .build();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(uri.toString());
    dest.writeFloat(dimLevelInDarkTheme);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UriChatWallpaper that = (UriChatWallpaper) o;
    return Float.compare(that.dimLevelInDarkTheme, dimLevelInDarkTheme) == 0 &&
        uri.equals(that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri, dimLevelInDarkTheme);
  }

  public static final Creator<UriChatWallpaper> CREATOR = new Creator<UriChatWallpaper>() {
    @Override
    public UriChatWallpaper createFromParcel(Parcel in) {
      return new UriChatWallpaper(Uri.parse(in.readString()), in.readFloat());
    }

    @Override
    public UriChatWallpaper[] newArray(int size) {
      return new UriChatWallpaper[size];
    }
  };
}
