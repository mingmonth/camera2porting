package yskim.sample.camera2porting;

import android.view.View;

public interface CameraModule {

    public void init(MainActivity activity, View frame);

    public void onPauseBeforeSuper();

    public void onPauseAfterSuper();

    public void onResumeAfterSuper();

    public void onStop();

    public boolean onBackPressed();

    public void onUserInteraction();

    public void onOrientationChanged(int orientation);
}

