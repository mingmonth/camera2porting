package yskim.sample.camera2porting.camera;

import android.app.Activity;
import android.view.View;

public interface CameraModule {

    public void init(Activity activity, View frame, CameraManager.CameraOpenErrorCallback cb);

    public void onPauseBeforeSuper();

    public void onPauseAfterSuper();

    public void onResumeAfterSuper();

    public void onStop();

    public boolean onBackPressed();

    public void onUserInteraction();

    public void onOrientationChanged(int orientation);
}

