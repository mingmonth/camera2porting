package yskim.sample.camera2porting.data;

import android.view.View;

public class CameraPreviewData extends SimpleViewData {
    /**
     * Constructor.
     *
     * @param v      The {@link android.view.View} for camera preview.
     * @param width  The width of the camera preview.
     * @param height The height of the camera preview.
     */
    public CameraPreviewData(View v, int width, int height) {
        super(v, width, height);
    }
}

