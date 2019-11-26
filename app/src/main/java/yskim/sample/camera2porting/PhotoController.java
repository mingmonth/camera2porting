package yskim.sample.camera2porting;

import android.graphics.Rect;
import android.view.View;

import yskim.sample.camera2porting.ShutterButton.OnShutterButtonListener;

public interface PhotoController extends OnShutterButtonListener {

    public static final int PREVIEW_STOPPED = 0;
    public static final int IDLE = 1;  // preview is active
    // Focus is in progress. The exact focus state is in Focus.java.
    public static final int FOCUSING = 2;
    public static final int SNAPSHOT_IN_PROGRESS = 3;
    // Switching between cameras.
    public static final int SWITCHING_CAMERA = 4;

    // returns the actual set zoom value
    public int onZoomChanged(int requestedZoom);

    public boolean isImageCaptureIntent();

    public boolean isCameraIdle();

    public void onCaptureDone();

    public void onCaptureCancelled();

    public void onCaptureRetake();

    public void cancelAutoFocus();

    public void stopPreview();

    public int getCameraState();

    public void onSingleTapUp(View view, int x, int y);

    public void onCountDownFinished();

    public void onPreviewRectChanged(Rect previewRect);

    public void updateCameraOrientation();

    public void enableRecordingLocation(boolean enable);

    /**
     * This is the callback when the UI or buffer holder for camera preview,
     * such as {@link android.graphics.SurfaceTexture}, is ready to be used.
     * The controller can start the camera preview after or in this callback.
     */
    public void onPreviewUIReady();


    /**
     * This is the callback when the UI or buffer holder for camera preview,
     * such as {@link android.graphics.SurfaceTexture}, is being destroyed.
     * The controller should try to stop the preview in this callback.
     */
    public void onPreviewUIDestroyed();
}

