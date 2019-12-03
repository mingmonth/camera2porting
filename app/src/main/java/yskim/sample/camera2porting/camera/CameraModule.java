package yskim.sample.camera2porting.camera;

import android.view.View;

import yskim.sample.camera2porting.CameraActivity;

public interface CameraModule {

    public void init(CameraActivity activity, View frame);

    public void onPauseBeforeSuper();

    public void onPauseAfterSuper();

    public void onResumeAfterSuper();

    public void onStop();

    public boolean onBackPressed();

    public void onUserInteraction();

    public void onOrientationChanged(int orientation);
}

