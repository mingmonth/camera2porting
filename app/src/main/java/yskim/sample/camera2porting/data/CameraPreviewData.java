package yskim.sample.camera2porting.data;

import android.view.View;

import yskim.sample.camera2porting.ui.FilmStripView.ImageData;

public class CameraPreviewData extends SimpleViewData {

    private boolean mPreviewLocked;

    /**
     * Constructor.
     *
     * @param v      The {@link android.view.View} for camera preview.
     * @param width  The width of the camera preview.
     * @param height The height of the camera preview.
     */
    public CameraPreviewData(View v, int width, int height) {
        super(v, width, height, -1, -1);
        mPreviewLocked = true;
    }

    @Override
    public int getViewType() {
        return ImageData.VIEW_TYPE_STICKY;
    }

    @Override
    public int getLocalDataType() {
        return LOCAL_CAMERA_PREVIEW;
    }

    @Override
    public boolean canSwipeInFullScreen() {
        return !mPreviewLocked;
    }

    /**
     * Locks the camera preview. When the camera preview is locked, swipe
     * to film strip is not allowed. One case is when the video recording
     * is in progress.
     *
     * @param lock {@code true} if the preview should be locked. {@code false}
     *             otherwise.
     */
    public void lockPreview(boolean lock) {
        mPreviewLocked = lock;
    }
}

