package yskim.sample.camera2porting.camera.data;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;

import yskim.sample.camera2porting.camera.ui.FilmStripView;

public interface LocalData extends FilmStripView.ImageData {
    View getView(Activity a, int width, int height, Drawable placeHolder, LocalDataAdapter adapter);
}
