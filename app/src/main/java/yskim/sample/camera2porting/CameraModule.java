package yskim.sample.camera2porting;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.View;

public interface CameraModule {

    public void init(MainActivity activity, View frame);

    //public void onPreviewFocusChanged(boolean previewFocused);

    public void onPauseBeforeSuper();

    public void onPauseAfterSuper();

//    public void onResumeBeforeSuper();

    public void onResumeAfterSuper();

//    public void onConfigurationChanged(Configuration config);

    public void onStop();

//    public void installIntentFilter();

//    public void onActivityResult(int requestCode, int resultCode, Intent data);

    public boolean onBackPressed();

//    public boolean onKeyDown(int keyCode, KeyEvent event);
//
//    public boolean onKeyUp(int keyCode, KeyEvent event);

//    public void onSingleTapUp(View view, int x, int y);

//    public void onPreviewTextureCopied();

//    public void onCaptureTextureCopied();

    public void onUserInteraction();

//    public boolean updateStorageHintOnResume();

    public void onOrientationChanged(int orientation);

    //public void onShowSwitcherPopup();

    //public void onMediaSaveServiceConnected(MediaSaveService s);

    //public boolean arePreviewControlsVisible();
}

