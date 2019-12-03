package yskim.sample.camera2porting.camera;

public interface PhotoController {
    int PREVIEW_STOPPED = 0;
    int IDLE = 1;  // preview is active
    // Focus is in progress. The exact focus state is in Focus.java.
    int FOCUSING = 2;
    int SNAPSHOT_IN_PROGRESS = 3;
    // Switching between cameras.
    int SWITCHING_CAMERA = 4;
    void stopPreview();
    /**
     * This is the callback when the UI or buffer holder for camera preview,
     * such as {@link android.graphics.SurfaceTexture}, is ready to be used.
     * The controller can start the camera preview after or in this callback.
     */
    void onPreviewUIReady();
    /**
     * This is the callback when the UI or buffer holder for camera preview,
     * such as {@link android.graphics.SurfaceTexture}, is being destroyed.
     * The controller should try to stop the preview in this callback.
     */
    void onPreviewUIDestroyed();
}

